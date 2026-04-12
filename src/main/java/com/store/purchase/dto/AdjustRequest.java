package com.store.purchase.dto;

import lombok.Data;

@Data
public class AdjustRequest {
    private int quantity;
    private String type;     // IN / OUT
    private String remarks;
    private String referenceId;  // 🔑 To link OUT to RESERVE transaction
}
