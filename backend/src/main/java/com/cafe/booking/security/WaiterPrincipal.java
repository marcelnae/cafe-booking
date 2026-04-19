package com.cafe.booking.security;

/** Minimal principal populated from a validated JWT and available via @AuthenticationPrincipal. */
public record WaiterPrincipal(Long id, String name) {
}
