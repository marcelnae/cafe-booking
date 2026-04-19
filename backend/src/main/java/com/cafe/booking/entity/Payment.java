package com.cafe.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tab_id", nullable = false)
    private Tab tab;

    /** Amount paid (excluding tip). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Tip amount, may be zero. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tip = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod method;

    /** Optional label identifying who paid (for split payments). */
    @Column(name = "payer_label", length = 80)
    private String payerLabel;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public BigDecimal grandTotal() {
        return amount.add(tip == null ? BigDecimal.ZERO : tip);
    }
}
