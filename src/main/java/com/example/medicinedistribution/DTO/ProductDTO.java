package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer productId;
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm không được vượt quá 200 ký tự")
    private String productName;
    
    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;
    
    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;
    
    private boolean status;
    
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private int stockQuantity;
    
    @NotNull(message = "Mã danh mục không được để trống")
    private Integer categoryId;
}
