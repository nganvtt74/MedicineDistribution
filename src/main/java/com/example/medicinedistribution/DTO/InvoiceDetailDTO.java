package com.example.medicinedistribution.DTO;

import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailDTO {
    private Integer invoiceId;
    private Integer productId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;
}
