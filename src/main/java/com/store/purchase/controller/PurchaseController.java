package com.store.purchase.controller;

import com.store.purchase.dto.*;
import com.store.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public PurchaseResponse createPurchase(@RequestBody PurchaseRequest request) {
        return purchaseService.createPurchase(request);
    }

    @PutMapping("/{id}/cancel")
    public void cancelPurchase(@PathVariable Long id) {
    purchaseService.cancelPurchase(id);
    }

    @GetMapping
    public List<PurchaseResponse> getAllPurchases() {
        return purchaseService.getAllPurchases();
    }

    @GetMapping("/report")
    public List<?> getProductReports() {
        return purchaseService.getProductReports();
    }

    @GetMapping("/{id}")
    public PurchaseResponse getPurchase(@PathVariable Long id) {
        return purchaseService.getPurchaseById(id);
    }

    @PutMapping("/{id}")
    public PurchaseResponse updatePurchase(@PathVariable Long id,
                                           @RequestBody PurchaseRequest request) {
        return purchaseService.updatePurchase(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
    }
}
