package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.ProductDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.sql.Connection;
import java.util.List;

public interface ProductDAO extends BaseDAO<ProductDTO,Integer> {
    boolean updateQuantity(@NotNull(message = "Mã sản phẩm không được để trống") Integer productId, @Positive(message = "Số lượng phải lớn hơn 0") int quantity, Connection conn);

    List<ProductDTO> getAllActiveProducts(Connection conn);

    boolean increaseQuantity(@NotNull(message = "ID sản phẩm không được để trống") Integer productId, @Min(value = 1, message = "Số lượng phải lớn hơn 0") int quantity, Connection connection);
}
