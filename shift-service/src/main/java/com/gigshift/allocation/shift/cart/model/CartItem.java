package com.gigshift.allocation.shift.cart.model;

import java.io.Serializable;
import java.time.Instant;

public record CartItem(String shiftId, Instant addedAt) implements Serializable {
}

