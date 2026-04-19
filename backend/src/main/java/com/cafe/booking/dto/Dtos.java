package com.cafe.booking.dto;

import com.cafe.booking.entity.PaymentMethod;
import com.cafe.booking.entity.TabStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** All request/response DTOs, grouped in a single file to keep the scaffold concise. */
public final class Dtos {
    private Dtos() {}

    // ---------- Auth ----------

    public record LoginRequest(
            @NotBlank String name,
            @NotBlank @Pattern(regexp = "\\d{4}", message = "PIN must be 4 digits") String pin) {}

    public record LoginResponse(String token, Long waiterId, String waiterName, long expiresInMs) {}

    // ---------- Waiter ----------

    public record WaiterDto(Long id, String name, boolean active) {}

    // ---------- Table ----------

    public record TableDto(Long id, Integer tableNumber, Integer capacity, String label) {}

    // ---------- Menu ----------

    public record MenuItemDto(Long id, String name, String category, BigDecimal price, boolean available) {}

    // ---------- Tab ----------

    public record OpenTabRequest(
            @NotNull Long tableId,
            @Size(max = 120) String guestName) {}

    public record MoveTabRequest(
            @NotNull Long newTableId) {}

    public record TabDto(
            Long id,
            TableDto table,
            WaiterDto waiter,
            TabStatus status,
            String guestName,
            Instant createdAt,
            Instant closedAt,
            List<OrderItemDto> orders,
            List<PaymentDto> payments,
            BigDecimal total,
            BigDecimal paid,
            BigDecimal tipTotal,
            BigDecimal balance) {}

    // ---------- Order ----------

    public record AddOrderRequest(
            @NotNull Long menuItemId,
            @NotNull @Min(1) Integer quantity,
            @Size(max = 500) String note) {}

    public record ChangePriceRequest(
            @NotNull @DecimalMin(value = "0.00") BigDecimal newUnitPrice,
            @NotBlank @Size(max = 255) String reason) {}

    public record OrderItemDto(
            Long id,
            Long menuItemId,
            String itemName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal originalUnitPrice,
            String priceChangeReason,
            String note,
            BigDecimal lineTotal,
            Instant createdAt) {}

    // ---------- Payment ----------

    public record AddPaymentRequest(
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @DecimalMin(value = "0.00") BigDecimal tip,
            @NotNull PaymentMethod method,
            @Size(max = 80) String payerLabel) {}

    public record PaymentDto(
            Long id,
            BigDecimal amount,
            BigDecimal tip,
            PaymentMethod method,
            String payerLabel,
            Instant createdAt) {}

    // ---------- Generic error ----------

    public record ApiError(String error, String message, Instant timestamp) {}
}
