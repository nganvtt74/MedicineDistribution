package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailDTO {
    private Integer invoiceId;
    
    @NotNull(message = "Mã sản phẩm không được để trống")
    private Integer productId;
    private String productName;
    private String unit;
    
    @Positive(message = "Số lượng phải lớn hơn 0")
    private int quantity;
    
    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;
    
    @NotNull(message = "Tổng tiền không được để trống")
    @Min(value = 0, message = "Tổng tiền không được âm")
    private BigDecimal total;
}
