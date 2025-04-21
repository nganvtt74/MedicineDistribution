package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.ProductDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public interface ProductBUS extends BaseBUS<ProductDTO,Integer> {
    boolean checkStock(@NotNull(message = "Mã sản phẩm không được để trống") Integer productId, @Positive(message = "Số lượng phải lớn hơn 0") int quantity);
    List<ProductDTO> getAllActiveProducts();

    BigDecimal getTotalInventoryValue();
}
