package com.gigshift.allocation.shift.cart.dto;

import java.time.Instant;

public record CartItemResponse(String shiftId, Instant addedAt) {
}

