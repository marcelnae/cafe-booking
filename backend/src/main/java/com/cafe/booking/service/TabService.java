package com.cafe.booking.service;

import com.cafe.booking.dto.Dtos;
import com.cafe.booking.entity.*;
import com.cafe.booking.exception.BusinessException;
import com.cafe.booking.exception.NotFoundException;
import com.cafe.booking.repository.*;
import com.cafe.booking.security.WaiterPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class TabService {

    private final TabRepository tabRepository;
    private final RestaurantTableRepository tableRepository;
    private final WaiterRepository waiterRepository;
    private final MenuItemRepository menuItemRepository;
    private final Mapper mapper;

    public TabService(TabRepository tabRepository,
                      RestaurantTableRepository tableRepository,
                      WaiterRepository waiterRepository,
                      MenuItemRepository menuItemRepository,
                      Mapper mapper) {
        this.tabRepository = tabRepository;
        this.tableRepository = tableRepository;
        this.waiterRepository = waiterRepository;
        this.menuItemRepository = menuItemRepository;
        this.mapper = mapper;
    }

    // ---------- queries ----------

    @Transactional(readOnly = true)
    public List<Dtos.TabDto> search(TabStatus status, Long tableId, Long waiterId, String q) {
        String trimmed = (q == null || q.isBlank()) ? null : q.trim();
        return tabRepository.search(status, tableId, waiterId, trimmed).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Dtos.TabDto get(Long tabId) {
        return mapper.toDto(loadTab(tabId));
    }

    // ---------- tab lifecycle ----------

    public Dtos.TabDto openTab(Dtos.OpenTabRequest request, WaiterPrincipal principal) {
        RestaurantTable table = tableRepository.findById(request.tableId())
                .orElseThrow(() -> new NotFoundException("Table not found: " + request.tableId()));

        // Business rule: at most one OPEN tab per table.
        tabRepository.findByTableIdAndStatus(table.getId(), TabStatus.OPEN).ifPresent(t -> {
            throw new BusinessException("Table " + table.getTableNumber() + " already has an open tab");
        });

        Waiter waiter = waiterRepository.findById(principal.id())
                .orElseThrow(() -> new NotFoundException("Waiter not found"));

        Tab tab = Tab.builder()
                .table(table)
                .waiter(waiter)
                .status(TabStatus.OPEN)
                .guestName(request.guestName())
                .createdAt(Instant.now())
                .build();

        return mapper.toDto(tabRepository.save(tab));
    }

    public Dtos.TabDto moveTab(Long tabId, Dtos.MoveTabRequest request) {
        Tab tab = loadOpenTab(tabId);
        if (Objects.equals(tab.getTable().getId(), request.newTableId())) {
            return mapper.toDto(tab);
        }
        RestaurantTable newTable = tableRepository.findById(request.newTableId())
                .orElseThrow(() -> new NotFoundException("Table not found: " + request.newTableId()));

        tabRepository.findByTableIdAndStatus(newTable.getId(), TabStatus.OPEN).ifPresent(t -> {
            throw new BusinessException(
                    "Target table " + newTable.getTableNumber() + " already has an open tab (id " + t.getId() + ")");
        });

        tab.setTable(newTable);
        return mapper.toDto(tab);
    }

    /** Merge {@code otherTabId} into {@code tabId}: all orders and payments are transferred, then the source is closed. */
    public Dtos.TabDto mergeTab(Long tabId, Long otherTabId) {
        if (Objects.equals(tabId, otherTabId)) {
            throw new BusinessException("Cannot merge a tab with itself");
        }
        Tab target = loadOpenTab(tabId);
        Tab source = loadOpenTab(otherTabId);

        // Move orders.
        for (OrderItem o : List.copyOf(source.getOrders())) {
            source.getOrders().remove(o);
            target.addOrder(o);
        }
        // Move payments (the customer may already have partially paid).
        for (Payment p : List.copyOf(source.getPayments())) {
            source.getPayments().remove(p);
            target.addPayment(p);
        }
        source.setStatus(TabStatus.CLOSED);
        source.setClosedAt(Instant.now());

        return mapper.toDto(target);
    }

    /** Close a tab once the balance is zero (or negative — overpayment treated as tip). */
    public Dtos.TabDto closeTab(Long tabId) {
        Tab tab = loadOpenTab(tabId);

        BigDecimal total = tab.getOrders().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = tab.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (paid.compareTo(total) < 0) {
            throw new BusinessException("Tab is not fully paid (balance " + total.subtract(paid) + ")");
        }

        tab.setStatus(TabStatus.CLOSED);
        tab.setClosedAt(Instant.now());
        return mapper.toDto(tab);
    }

    // ---------- orders ----------

    public Dtos.TabDto addOrder(Long tabId, Dtos.AddOrderRequest request) {
        Tab tab = loadOpenTab(tabId);
        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .orElseThrow(() -> new NotFoundException("Menu item not found: " + request.menuItemId()));
        if (!menuItem.isAvailable()) {
            throw new BusinessException("Menu item is not available: " + menuItem.getName());
        }

        OrderItem order = OrderItem.builder()
                .menuItem(menuItem)
                .itemName(menuItem.getName())
                .quantity(request.quantity())
                .unitPrice(menuItem.getPrice())
                .originalUnitPrice(menuItem.getPrice())
                .note(request.note())
                .createdAt(Instant.now())
                .build();
        tab.addOrder(order);
        return mapper.toDto(tab);
    }

    public Dtos.TabDto removeOrder(Long tabId, Long orderId) {
        Tab tab = loadOpenTab(tabId);
        OrderItem order = tab.getOrders().stream()
                .filter(o -> Objects.equals(o.getId(), orderId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found on tab " + tabId));
        tab.getOrders().remove(order); // orphanRemoval deletes it
        return mapper.toDto(tab);
    }

    public Dtos.TabDto changePrice(Long tabId, Long orderId, Dtos.ChangePriceRequest request) {
        Tab tab = loadOpenTab(tabId);
        OrderItem order = tab.getOrders().stream()
                .filter(o -> Objects.equals(o.getId(), orderId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found on tab " + tabId));

        if (request.reason() == null || request.reason().isBlank()) {
            throw new BusinessException("A reason is required when changing the price");
        }
        order.setUnitPrice(request.newUnitPrice());
        order.setPriceChangeReason(request.reason());
        return mapper.toDto(tab);
    }

    // ---------- payments (split-friendly) ----------

    public Dtos.TabDto addPayment(Long tabId, Dtos.AddPaymentRequest request) {
        Tab tab = loadOpenTab(tabId);

        BigDecimal tip = request.tip() == null ? BigDecimal.ZERO : request.tip();
        if (tip.signum() < 0) throw new BusinessException("Tip cannot be negative");
        if (request.amount().signum() <= 0) throw new BusinessException("Amount must be positive");

        // Guard against overpaying the principal amount. Tip is unrestricted.
        BigDecimal total = tab.getOrders().stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal alreadyPaid = tab.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = total.subtract(alreadyPaid);
        if (request.amount().compareTo(remaining) > 0) {
            throw new BusinessException("Payment (" + request.amount() + ") exceeds remaining balance (" + remaining + ")");
        }

        Payment payment = Payment.builder()
                .amount(request.amount())
                .tip(tip)
                .method(request.method())
                .payerLabel(request.payerLabel())
                .createdAt(Instant.now())
                .build();
        tab.addPayment(payment);
        return mapper.toDto(tab);
    }

    // ---------- helpers ----------

    private Tab loadTab(Long id) {
        return tabRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tab not found: " + id));
    }

    private Tab loadOpenTab(Long id) {
        Tab tab = loadTab(id);
        if (tab.getStatus() != TabStatus.OPEN) {
            throw new BusinessException("Tab " + id + " is not open");
        }
        return tab;
    }
}
