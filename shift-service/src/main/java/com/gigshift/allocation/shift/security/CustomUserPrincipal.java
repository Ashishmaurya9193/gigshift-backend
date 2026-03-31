package com.gigshift.allocation.shift.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal {

    private final String userId;
    private final String role;
}
