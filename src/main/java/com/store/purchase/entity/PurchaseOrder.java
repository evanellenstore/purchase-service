package com.store.purchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.store.purchase.enums.PurchaseStatus;

@Entity
@Table(name = "purchase_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String supplier;
    private LocalDateTime purchaseDate;

    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;
}
