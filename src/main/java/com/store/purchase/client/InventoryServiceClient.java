package com.store.purchase.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.store.purchase.dto.AdjustRequest;
import com.store.purchase.dto.ReserveRequest;

@FeignClient(name = "inventory-service", url = "http://localhost:2011")
public interface InventoryServiceClient {

    @PutMapping("/inventory/{productId}/adjust")
    void adjustStock(@PathVariable Long productId,
                     @RequestBody AdjustRequest request);

     @PutMapping("/inventory/{productId}/release")
    void releaseStock(@PathVariable("productId") Long productId,
                      @RequestBody ReserveRequest request);                
}
