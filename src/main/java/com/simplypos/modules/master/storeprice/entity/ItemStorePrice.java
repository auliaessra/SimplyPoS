package com.simplypos.modules.master.storeprice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.simplypos.entities.Store;
import com.simplypos.modules.master.item.entity.Item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_store_prices", uniqueConstraints = @UniqueConstraint(columnNames = { "item_id", "store_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemStorePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "buy_price", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal buyPrice = BigDecimal.ZERO;

    @Column(name = "sell_price", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal sellPrice = BigDecimal.ZERO;

    @Column(name = "stock_qty_ml", nullable = false, precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal stockQtyMl = BigDecimal.ZERO;

    @Column(name = "min_stock_ml", precision = 15, scale = 3)
    @Builder.Default
    private BigDecimal minStockMl = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}