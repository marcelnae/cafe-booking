package com.cafe.booking.service;

import com.cafe.booking.dto.Dtos;
import com.cafe.booking.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class Mapper {

    public Dtos.WaiterDto toDto(Waiter w) {
        return new Dtos.WaiterDto(w.getId(), w.getName(), w.isActive());
    }

    public Dtos.TableDto toDto(RestaurantTable t) {
        return new Dtos.TableDto(t.getId(), t.getTableNumber(), t.getCapacity(), t.getLabel());
    }

    public Dtos.MenuItemDto toDto(MenuItem m) {
        return new Dtos.MenuItemDto(m.getId(), m.getName(), m.getCategory(), m.getPrice(), m.isAvailable());
    }

    public Dtos.OrderItemDto toDto(OrderItem o) {
        return new Dtos.OrderItemDto(
                o.getId(),
                o.getMenuItem() == null ? null : o.getMenuItem().getId(),
                o.getItemName(),
                o.getQuantity(),
                o.getUnitPrice(),
                o.getOriginalUnitPrice(),
                o.getPriceChangeReason(),
                o.getNote(),
                o.lineTotal(),
                o.getCreatedAt());
    }

    public Dtos.PaymentDto toDto(Payment p) {
        return new Dtos.PaymentDto(
                p.getId(),
                p.getAmount(),
                p.getTip(),
                p.getMethod(),
                p.getPayerLabel(),
                p.getCreatedAt());
    }

    public Dtos.TabDto toDto(Tab tab) {
        List<Dtos.OrderItemDto> orders = tab.getOrders().stream().map(this::toDto).toList();
        List<Dtos.PaymentDto> payments = tab.getPayments().stream().map(this::toDto).toList();

        BigDecimal total = tab.getOrders().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = tab.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tipTotal = tab.getPayments().stream()
                .map(p -> p.getTip() == null ? BigDecimal.ZERO : p.getTip())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = total.subtract(paid);

        return new Dtos.TabDto(
                tab.getId(),
                toDto(tab.getTable()),
                toDto(tab.getWaiter()),
                tab.getStatus(),
                tab.getGuestName(),
                tab.getCreatedAt(),
                tab.getClosedAt(),
                orders,
                payments,
                total,
                paid,
                tipTotal,
                balance);
    }
}
