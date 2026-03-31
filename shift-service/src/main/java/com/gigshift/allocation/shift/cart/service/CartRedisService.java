package com.gigshift.allocation.shift.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigshift.allocation.shift.cart.model.CartItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class CartRedisService {

    private static final Logger log = LoggerFactory.getLogger(CartRedisService.class);

    private static final String CART_KEY_PREFIX = "cart:";
    private static final String LOCK_KEY_PREFIX = "lock:shift:";
    private static final Duration CART_TTL = Duration.ofMinutes(30);
    private static final Duration LOCK_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public CartRedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // Add shift to worker's cart
    public CartItem addShift(String workerId, String shiftId) {
        String key = CART_KEY_PREFIX + workerId;

        List<CartItem> existing = getCartItems(workerId);
        boolean alreadyInCart = existing.stream().anyMatch(item -> item.shiftId().equals(shiftId));
        if (alreadyInCart) {
            throw new IllegalStateException("Shift " + shiftId + " is already in your cart");
        }

        CartItem item = new CartItem(shiftId, Instant.now());
        redisTemplate.opsForList().rightPush(key, item);
        redisTemplate.expire(key, CART_TTL);

        log.info("Worker {} added shift {} to cart", workerId, shiftId);
        return item;
    }

    // Get all items in worker's cart
    public List<CartItem> getCartItems(String workerId) {
        String key = CART_KEY_PREFIX + workerId;
        List<Object> raw = redisTemplate.opsForList().range(key, 0, -1);
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }

        return raw.stream()
                .map(this::toCartItem)
                .toList();
    }

    // Remove one shift from cart
    public void removeShift(String workerId, String shiftId) {
        String key = CART_KEY_PREFIX + workerId;

        List<CartItem> updated = getCartItems(workerId).stream()
                .filter(item -> !item.shiftId().equals(shiftId))
                .toList();

        redisTemplate.delete(key);
        if (!updated.isEmpty()) {
            updated.forEach(item -> redisTemplate.opsForList().rightPush(key, item));
            redisTemplate.expire(key, CART_TTL);
        }

        log.info("Worker {} removed shift {} from cart", workerId, shiftId);
    }

    // Clear entire cart
    public void clearCart(String workerId) {
        redisTemplate.delete(CART_KEY_PREFIX + workerId);
        log.info("Cart cleared for worker {}", workerId);
    }

    // Acquire Redis lock on a shift (called during checkout)
    public boolean acquireLock(String shiftId, String workerId) {
        String lockKey = LOCK_KEY_PREFIX + shiftId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, workerId, LOCK_TTL);
        boolean result = Boolean.TRUE.equals(acquired);
        log.info("Lock acquire attempt on shift {} by worker {}: {}", shiftId, workerId, result);
        return result;
    }

    // Release lock after checkout completes or fails
    public void releaseLock(String shiftId, String workerId) {
        String lockKey = LOCK_KEY_PREFIX + shiftId;
        Object owner = redisTemplate.opsForValue().get(lockKey);
        if (owner != null && workerId.equals(owner.toString())) {
            redisTemplate.delete(lockKey);
            log.info("Lock released on shift {} by owner {}", shiftId, workerId);
            return;
        }
        log.info("Skip lock release for shift {}. Current owner: {}", shiftId, owner);
    }

    private CartItem toCartItem(Object raw) {
        if (raw instanceof CartItem item) {
            return item;
        }
        return objectMapper.convertValue(raw, CartItem.class);
    }
}
