package com.gigshift.allocation.shift.cart.service;

import com.gigshift.allocation.shift.cart.dto.CartCheckoutError;
import com.gigshift.allocation.shift.cart.dto.CartCheckoutResponse;
import com.gigshift.allocation.shift.cart.dto.CartItemResponse;
import com.gigshift.allocation.shift.cart.model.CartItem;
import com.gigshift.allocation.shift.selection.dto.ShiftSelectionResponse;
import com.gigshift.allocation.shift.selection.service.ShiftSelectionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartWorkflowService {

    private final CartRedisService cartRedisService;
    private final ShiftSelectionService shiftSelectionService;

    public CartWorkflowService(CartRedisService cartRedisService,
                               ShiftSelectionService shiftSelectionService) {
        this.cartRedisService = cartRedisService;
        this.shiftSelectionService = shiftSelectionService;
    }

    public CartItemResponse addShift(String workerId, String shiftId) {
        CartItem saved = cartRedisService.addShift(workerId, shiftId);
        return new CartItemResponse(saved.shiftId(), saved.addedAt());
    }

    public List<CartItemResponse> viewCart(String workerId) {
        return cartRedisService.getCartItems(workerId).stream()
                .map(item -> new CartItemResponse(item.shiftId(), item.addedAt()))
                .toList();
    }

    public void removeShift(String workerId, String shiftId) {
        cartRedisService.removeShift(workerId, shiftId);
    }

    public void clearCart(String workerId) {
        cartRedisService.clearCart(workerId);
    }

    public CartCheckoutResponse checkout(String workerId) {
        List<CartItem> items = cartRedisService.getCartItems(workerId);
        if (items.isEmpty()) {
            return new CartCheckoutResponse(List.of(), List.of());
        }

        List<ShiftSelectionResponse> confirmed = new ArrayList<>();
        List<CartCheckoutError> failed = new ArrayList<>();

        for (CartItem item : items) {
            String shiftId = item.shiftId();

            // acquire Redis lock before confirming — prevents double booking
            boolean locked = cartRedisService.acquireLock(shiftId, workerId);
            if (!locked) {
                failed.add(new CartCheckoutError(shiftId,
                        "Shift is currently being claimed by another worker"));
                continue;
            }

            try {
                confirmed.add(shiftSelectionService.selectShift(workerId, shiftId));
            } catch (Exception ex) {
                failed.add(new CartCheckoutError(shiftId, ex.getMessage()));
            } finally {
                // always release — even if selectShift throws
                cartRedisService.releaseLock(item.shiftId(), workerId);

            }
        }

        cartRedisService.clearCart(workerId);
        return new CartCheckoutResponse(confirmed, failed);
    }
}