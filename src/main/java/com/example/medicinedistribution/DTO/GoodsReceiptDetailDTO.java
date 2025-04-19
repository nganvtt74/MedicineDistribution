package com.example.medicinedistribution.DTO;

import java.math.BigDecimal;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptDetailDTO {
    private Integer goodsReceiptId;
    private Integer productId;
    private BigDecimal price;
    private int quantity;
    private BigDecimal total;
}
