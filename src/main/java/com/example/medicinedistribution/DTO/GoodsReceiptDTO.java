package com.example.medicinedistribution.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptDTO {
    private Integer goodsReceiptId;
    private Integer employeeId;
    private Integer manufacturerId;
    private LocalDate date;
    private BigDecimal total;
    private List<GoodsReceiptDetailDTO> details;
}