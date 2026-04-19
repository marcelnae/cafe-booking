package com.cafe.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tab")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "waiter_id", nullable = false)
    private Waiter waiter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TabStatus status = TabStatus.OPEN;

    @Column(name = "guest_name", length = 120)
    private String guestName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    @OneToMany(mappedBy = "tab", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orders = new ArrayList<>();

    @OneToMany(mappedBy = "tab", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    public void addOrder(OrderItem order) {
        order.setTab(this);
        this.orders.add(order);
    }

    public void addPayment(Payment payment) {
        payment.setTab(this);
        this.payments.add(payment);
    }
}
