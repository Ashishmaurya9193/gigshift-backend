package com.gigshift.allocation.shift.cart.dto;

import com.gigshift.allocation.shift.selection.dto.ShiftSelectionResponse;

import java.util.List;

public record CartCheckoutResponse(List<ShiftSelectionResponse> confirmed,
                                   List<CartCheckoutError> failed) {
}

