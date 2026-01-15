package com.store.purchase.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
@Data
@AllArgsConstructor
public class ReserveRequest {
    private Integer quantity;
    private String referenceId;
}

