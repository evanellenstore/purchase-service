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
import com.store.purchase.enums.PurchasedBy;
import com.store.purchase.enums.TransactionType;
import com.store.purchase.repository.PurchaseOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderRepository purchaseRepo;
    private final InventoryServiceClient inventoryClient; // Call Inventory Service

    /**
     * 
     * @param req
     * @return
     */
    // Create Purchase → Update Inventory
    @Transactional
    public PurchaseResponse createPurchase(PurchaseRequest req, String purchasedBy) {

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

        // Update Inventory
        AdjustRequest adjustRequest = new AdjustRequest();
        adjustRequest.setQuantity(req.getQuantity());

        if(PurchasedBy.CUSTOMER.name().equalsIgnoreCase(purchasedBy)) {
            adjustRequest.setType(TransactionType.OUT.name());
        } else {
            adjustRequest.setType(TransactionType.IN.name());
        }
        adjustRequest.setRemarks("Purchase: " + req.getRemarks());
        inventoryClient.adjustStock(req.getProductId(), adjustRequest);

        return mapToResponse(order);
    }

@Transactional
public void cancelPurchase(Long purchaseId) {

    PurchaseOrder purchase = purchaseRepo.findById(purchaseId)
            .orElseThrow(() -> new RuntimeException("Purchase not found"));

    if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
        throw new RuntimeException("Purchase already cancelled");
    }

    inventoryClient.releaseStock(
        purchase.getProductId(),
        new ReserveRequest(purchase.getQuantity(), "PURCHASE_CANCEL")
    );

    purchase.setStatus(PurchaseStatus.CANCELLED);
    purchaseRepo.save(purchase);
}




    /**
     * Get all purchases
     * @return
     */
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

    @Transactional
    public PurchaseResponse updatePurchase(Long id, PurchaseRequest req) {
        PurchaseOrder order = purchaseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase not found"));

        order.setQuantity(req.getQuantity());
        order.setUnitPrice(req.getUnitPrice());
        order.setTotalPrice(req.getQuantity() * req.getUnitPrice());
        order.setSupplier(req.getSupplier());
        order.setRemarks(req.getRemarks());

        purchaseRepo.save(order);
        return mapToResponse(order);
    }

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
                .build();
    }
}
