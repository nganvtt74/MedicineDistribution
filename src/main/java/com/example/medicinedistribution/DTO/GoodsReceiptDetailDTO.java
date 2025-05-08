package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptDetailDTO {
    private Integer goodsReceiptId;
    
    @NotNull(message = "ID sản phẩm không được để trống")
    private Integer productId;
    private String productName;
    private String unit;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
    private BigDecimal price;
    
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;
    
    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal total;
}
