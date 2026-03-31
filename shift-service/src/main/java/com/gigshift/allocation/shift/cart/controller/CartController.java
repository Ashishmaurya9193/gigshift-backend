package com.gigshift.allocation.shift.cart.controller;

import com.gigshift.allocation.shift.cart.dto.CartCheckoutResponse;
import com.gigshift.allocation.shift.cart.dto.CartItemResponse;
import com.gigshift.allocation.shift.cart.service.CartWorkflowService;
import com.gigshift.allocation.shift.security.CustomUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartWorkflowService cartWorkflowService;

    public CartController(CartWorkflowService cartWorkflowService) {
        this.cartWorkflowService = cartWorkflowService;
    }

    @PostMapping("/shifts/{shiftId}")
    public CartItemResponse addShift(@AuthenticationPrincipal CustomUserPrincipal principal,
                                     @PathVariable String shiftId) {
        assertWorker(principal);
        return cartWorkflowService.addShift(principal.getUserId(), shiftId);
    }

    @GetMapping
    public List<CartItemResponse> viewCart(@AuthenticationPrincipal CustomUserPrincipal principal) {
        assertWorker(principal);
        return cartWorkflowService.viewCart(principal.getUserId());
    }

    @DeleteMapping("/shifts/{shiftId}")
    public ResponseEntity<Void> removeShift(@AuthenticationPrincipal CustomUserPrincipal principal,
                                            @PathVariable String shiftId) {
        assertWorker(principal);
        cartWorkflowService.removeShift(principal.getUserId(), shiftId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal CustomUserPrincipal principal) {
        assertWorker(principal);
        cartWorkflowService.clearCart(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public CartCheckoutResponse checkout(@AuthenticationPrincipal CustomUserPrincipal principal) {
        assertWorker(principal);
        return cartWorkflowService.checkout(principal.getUserId());
    }

    private void assertWorker(CustomUserPrincipal principal) {
        if (principal == null || principal.getUserId() == null || principal.getUserId().isBlank()) {
            throw new AccessDeniedException("Missing authenticated worker principal");
        }
        if (principal.getRole() == null || !"WORKER".equals(principal.getRole())) {
            throw new AccessDeniedException("Only workers can manage carts");
        }
    }

//    private void assertWorker(CustomUserPrincipal principal) {
//        if (principal == null || !"WORKER".equals(principal.getRole())) {
//            throw new AccessDeniedException("Only workers can manage carts");
//        }
//    }
}

