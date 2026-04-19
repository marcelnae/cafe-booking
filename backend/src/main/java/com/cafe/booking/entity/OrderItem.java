package com.cafe.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tab_id", nullable = false)
    private Tab tab;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    /** Snapshot of the menu item name at order time (so history survives menu edits). */
    @Column(name = "item_name", nullable = false, length = 120)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity;

    /** The price used for this line (possibly overridden). */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** Original menu price at order time — preserved even if {@code unitPrice} is overridden. */
    @Column(name = "original_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalUnitPrice;

    /** Mandatory reason when {@code unitPrice != originalUnitPrice}. */
    @Column(name = "price_change_reason", length = 255)
    private String priceChangeReason;

    /** Free text note from the waiter (allergy, “no onion”, etc.). */
    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
