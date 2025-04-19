package com.example.medicinedistribution.DTO;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private String unit;
    private boolean status;
    private int stockQuantity;
    private String categoryId;
}