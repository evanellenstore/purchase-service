package com.store.purchase.dto;

import java.time.LocalDateTime;

import com.store.purchase.enums.PurchaseStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseResponse {
    private Long id;
    private Long productId;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String supplier;
    private String remarks;
    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;
    private LocalDateTime purchaseDate;
}
