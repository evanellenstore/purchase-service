package com.store.purchase.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store.purchase.client.InventoryServiceClient;
import com.store.purchase.dto.AdjustRequest;
import com.store.purchase.dto.PurchaseRequest;
import com.store.purchase.dto.PurchaseResponse;
import com.store.purchase.dto.ReserveRequest;
import com.store.purchase.entity.PurchaseOrder;
import com.store.purchase.enums.PurchaseStatus;
import com.store.purchase.enums.TransactionType;
import com.store.purchase.repository.PurchaseOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderRepository purchaseRepo;
    private final InventoryServiceClient inventoryClient;

    /* ==============================
       CREATE PURCHASE → RESERVE STOCK
       ============================== */
    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest req) {

        // 1️⃣ Reserve stock
        inventoryClient.reserve(
                req.getProductId(),
                new ReserveRequest(req.getQuantity(), "PURCHASE_CREATE")
        );

        // 2️⃣ Save purchase
        PurchaseOrder order = PurchaseOrder.builder()
                .productId(req.getProductId())
                .quantity(req.getQuantity())
                .unitPrice(req.getUnitPrice())
                .totalPrice(req.getUnitPrice() * req.getQuantity())
                .supplier(req.getSupplier())
                .purchaseDate(LocalDateTime.now())
                .remarks(req.getRemarks())
                .status(PurchaseStatus.CREATED)
                .build();

        purchaseRepo.save(order);
        return mapToResponse(order);
    }

    /* ==============================
       COMPLETE PURCHASE → ADJUST OUT
       ============================== */
    @Transactional
    public void completePurchase(Long purchaseId) {

        PurchaseOrder purchase = purchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        if (purchase.getStatus() != PurchaseStatus.CREATED) {
            throw new RuntimeException("Only CREATED purchase can be completed");
        }

        AdjustRequest adjust = new AdjustRequest();
        adjust.setQuantity(purchase.getQuantity());
        adjust.setType(TransactionType.OUT.name());
        adjust.setRemarks("PURCHASE_COMPLETE");

        inventoryClient.adjustStock(purchase.getProductId(), adjust);

        purchase.setStatus(PurchaseStatus.COMPLETED);
        purchaseRepo.save(purchase);
    }

    /* ==============================
       CANCEL PURCHASE → RELEASE STOCK
       ============================== */
    @Transactional
    public void cancelPurchase(Long purchaseId) {

        PurchaseOrder purchase = purchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new RuntimeException("Purchase already cancelled");
        }

        if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
            throw new RuntimeException("Completed purchase cannot be cancelled");
        }

        inventoryClient.releaseStock(
                purchase.getProductId(),
                new ReserveRequest(purchase.getQuantity(), "PURCHASE_CANCEL")
        );

        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchaseRepo.save(purchase);
    }

    /* ==============================
       READ OPERATIONS
       ============================== */
    public List<PurchaseResponse> getAllPurchases() {
        return purchaseRepo.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PurchaseResponse getPurchaseById(Long id) {
        return purchaseRepo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));
    }

    /* ==============================
       UPDATE PURCHASE
       ============================== */
    @Transactional
    public PurchaseResponse updatePurchase(Long id, PurchaseRequest req) {

        PurchaseOrder order = purchaseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        if (order.getStatus() != PurchaseStatus.CREATED) {
            throw new RuntimeException("Only CREATED purchase can be updated");
        }

        order.setQuantity(req.getQuantity());
        order.setUnitPrice(req.getUnitPrice());
        order.setTotalPrice(req.getQuantity() * req.getUnitPrice());
        order.setSupplier(req.getSupplier());
        order.setRemarks(req.getRemarks());

        purchaseRepo.save(order);
        return mapToResponse(order);
    }

    /* ==============================
       DELETE (ADMIN ONLY)
       ============================== */
    @Transactional
    public void deletePurchase(Long id) {
        purchaseRepo.deleteById(id);
    }


    private PurchaseResponse mapToResponse(PurchaseOrder order) {
        return PurchaseResponse.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .supplier(order.getSupplier())
                .purchaseDate(order.getPurchaseDate())
                .remarks(order.getRemarks())
                .status(order.getStatus())
                .build();
    }
}
