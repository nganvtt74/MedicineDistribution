package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.ProductDAO;
import com.example.medicinedistribution.DTO.ProductDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class ProductDAOImpl implements ProductDAO {
    @Override
    public Integer insert(ProductDTO productDTO, Connection conn) {
        String sql = "INSERT INTO product (productName, price, unit, status, stock_quantity, categoryId) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, productDTO.getProductName());
            stmt.setBigDecimal(2, productDTO.getPrice());
            stmt.setString(3, productDTO.getUnit());
            stmt.setBoolean(4, productDTO.isStatus());
            stmt.setInt(5, productDTO.getStockQuantity());
            stmt.setInt(6, productDTO.getCategoryId());
            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(ProductDTO productDTO, Connection conn) {
        String sql = "UPDATE product SET productName = ?, price = ?, unit = ?, status = ?, stock_quantity = ?, categoryId = ? WHERE productId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productDTO.getProductName());
            stmt.setBigDecimal(2, productDTO.getPrice());
            stmt.setString(3, productDTO.getUnit());
            stmt.setBoolean(4, productDTO.isStatus());
            stmt.setInt(5, productDTO.getStockQuantity());
            stmt.setInt(6, productDTO.getCategoryId());
            stmt.setInt(7, productDTO.getProductId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM product WHERE productId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public ProductDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM product WHERE productId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return ProductDTO.builder()
                        .productId(rs.getInt("productId"))
                        .productName(rs.getString("productName"))
                        .price(rs.getBigDecimal("price"))
                        .unit(rs.getString("unit"))
                        .status(rs.getBoolean("status"))
                        .stockQuantity(rs.getInt("stock_quantity"))
                        .categoryId(rs.getInt("categoryId"))
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<ProductDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM product";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<ProductDTO> productList = new ArrayList<>();
            while (rs.next()) {
                ProductDTO productDTO = ProductDTO.builder()
                        .productId(rs.getInt("productId"))
                        .productName(rs.getString("productName"))
                        .price(rs.getBigDecimal("price"))
                        .unit(rs.getString("unit"))
                        .status(rs.getBoolean("status"))
                        .stockQuantity(rs.getInt("stock_quantity"))
                        .categoryId(rs.getInt("categoryId"))
                        .build();
                productList.add(productDTO);
            }
            return productList;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateQuantity(Integer productId, int quantity, Connection conn) {
        String sql = "UPDATE product SET stock_quantity = stock_quantity - ? WHERE productId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public List<ProductDTO> getAllActiveProducts(Connection conn) {
        String sql = "SELECT * FROM product WHERE status = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<ProductDTO> productList = new ArrayList<>();
            while (rs.next()) {
                ProductDTO productDTO = ProductDTO.builder()
                        .productId(rs.getInt("productId"))
                        .productName(rs.getString("productName"))
                        .price(rs.getBigDecimal("price"))
                        .unit(rs.getString("unit"))
                        .status(rs.getBoolean("status"))
                        .stockQuantity(rs.getInt("stock_quantity"))
                        .categoryId(rs.getInt("categoryId"))
                        .build();
                productList.add(productDTO);
            }
            return productList;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean increaseQuantity(Integer productId, int quantity, Connection connection) {
        String sql = "UPDATE product SET stock_quantity = stock_quantity + ? WHERE productId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }
}
