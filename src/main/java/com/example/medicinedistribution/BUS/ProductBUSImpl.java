package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.ProductBUS;
import com.example.medicinedistribution.DAO.Interface.ProductDAO;
import com.example.medicinedistribution.DTO.ProductDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.crypto.DecapsulateException;
import javax.sql.DataSource;

@Slf4j
public class ProductBUSImpl implements ProductBUS {

    private final ProductDAO productDAO;
    private final UserSession userSession;
    private final DataSource dataSource;

    public ProductBUSImpl(ProductDAO productDAO, UserSession userSession, DataSource dataSource) {
        this.productDAO = productDAO;
        this.userSession = userSession;
        this.dataSource = dataSource;
    }

    @Override
    public boolean insert(ProductDTO productDTO) {
        if (!userSession.hasPermission("INSERT_PRODUCT")) {
            log.error("User does not have permission to insert product");
            throw new PermissionDeniedException("Bạn không có quyền thêm sản phẩm");
        }
        try(Connection conn = dataSource.getConnection()){
            Integer result = productDAO.insert(productDTO, conn);
            if (result != null) {
                productDTO.setProductId(result);
                log.info("Insert product successful");
                return true;
            } else {
                log.error("Insert product failed");
                throw new InsertFailedException("Thêm sản phẩm thất bại");
            }
        }catch (SQLException e){
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean update(ProductDTO productDTO) {
        if (!userSession.hasPermission("UPDATE_PRODUCT")) {
            log.error("User does not have permission to update product");
            throw new PermissionDeniedException("Bạn không có quyền sửa sản phẩm");
        }
        try(Connection conn = dataSource.getConnection()){
            boolean result = productDAO.update(productDTO, conn);
            if (result) {
                log.info("Update product successful");
                return true;
            } else {
                log.error("Update product failed");
                throw new UpdateFailedException("Cập nhật sản phẩm thất bại");
            }
        }catch (SQLException e){
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (!userSession.hasPermission("DELETE_PRODUCT")) {
            log.error("User does not have permission to delete product");
            throw new PermissionDeniedException("Bạn không có quyền xóa sản phẩm");
        }
        try(Connection conn = dataSource.getConnection()){
            boolean result = productDAO.delete(integer, conn);
            if (result) {
                log.info("Delete product successful");
                return true;
            } else {
                log.error("Delete product failed");
                throw new DeleteFailedException("Xóa sản phẩm thất bại");
            }
        }catch (SQLException e){
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public ProductDTO findById(Integer integer) {
        if (!userSession.hasPermission("VIEW_PRODUCT")) {
            log.error("User does not have permission to find product");
            throw new PermissionDeniedException("Bạn không có quyền tìm sản phẩm");
        }

        try(Connection conn = dataSource.getConnection()){
            return productDAO.findById(integer, conn);
        }catch (SQLException e){
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<ProductDTO> findAll() {
        if (!userSession.hasPermission("VIEW_PRODUCT")) {
            log.error("User does not have permission to find product");
            throw new PermissionDeniedException("Bạn không có quyền tìm sản phẩm");
        }
        try(Connection conn = dataSource.getConnection()){
            return productDAO.findAll(conn);
        }catch (SQLException e){
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }
}
