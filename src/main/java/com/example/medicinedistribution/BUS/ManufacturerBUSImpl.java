package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.ManufacturerBUS;
import com.example.medicinedistribution.DAO.Interface.ManufacturerDAO;
import com.example.medicinedistribution.DTO.ManufacturerDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Slf4j
public class ManufacturerBUSImpl implements ManufacturerBUS {

    private final ManufacturerDAO manufacturerDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;

    public ManufacturerBUSImpl(ManufacturerDAO manufacturerDAO, DataSource dataSource,
                               UserSession userSession , Validator validator) {
        this.manufacturerDAO = manufacturerDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public boolean insert(ManufacturerDTO manufacturerDTO) {

        if(!userSession.hasPermission("INSERT_MANUFACTURER")) {
            log.error("User does not have permission to insert manufacturer");
            throw new PermissionDeniedException("Bạn không có quyền thêm nhà sản xuất");
        }
        valid(manufacturerDTO);
        try(Connection connection = dataSource.getConnection()) {
            Integer result = manufacturerDAO.insert(manufacturerDTO , connection);
            if (result > 0) {
                manufacturerDTO.setManufacturerId(result);
                log.info("Insert manufacturer successfully");
                return true;
            }else {
                log.error("Insert manufacturer failed");
                throw new InsertFailedException("Thêm nhà sản xuất thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean update(ManufacturerDTO manufacturerDTO) {
        if(!userSession.hasPermission("UPDATE_MANUFACTURER")) {
            log.error("User does not have permission to update manufacturer");
            throw new PermissionDeniedException("Bạn không có quyền sửa nhà sản xuất");
        }
        valid(manufacturerDTO);
        try(Connection connection = dataSource.getConnection()) {
            boolean result = manufacturerDAO.update(manufacturerDTO , connection);
            if (result) {
                log.info("Update manufacturer successfully");
                return true;
            }else {
                log.error("Update manufacturer failed");
                throw new UpdateFailedException("Cập nhật nhà sản xuất thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean delete(Integer integer) {

        if(!userSession.hasPermission("DELETE_MANUFACTURER")) {
            log.error("User does not have permission to delete manufacturer");
            throw new PermissionDeniedException("Bạn không có quyền xóa nhà sản xuất");
        }
        try(Connection connection = dataSource.getConnection()) {
            boolean result = manufacturerDAO.delete(integer , connection);
            if (result) {
                log.info("Delete manufacturer successfully");
                return true;
            }else {
                log.error("Delete manufacturer failed");
                throw new DeleteFailedException("Xóa nhà sản xuất thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public ManufacturerDTO findById(Integer integer) {
        if(!userSession.hasPermission("VIEW_MANUFACTURER")) {
            log.error("User does not have permission to find manufacturer");
            throw new PermissionDeniedException("Bạn không có quyền tìm kiếm nhà sản xuất");
        }
        try(Connection connection = dataSource.getConnection()) {
            return manufacturerDAO.findById(integer , connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<ManufacturerDTO> findAll() {
        if(!userSession.hasPermission("VIEW_MANUFACTURER")) {
            log.error("User does not have permission to find manufacturer");
            throw new PermissionDeniedException("Bạn không có quyền tìm kiếm nhà sản xuất");
        }
        try(Connection connection = dataSource.getConnection()) {
            return manufacturerDAO.findAll(connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    private void valid(ManufacturerDTO manufacturerDTO) {
        Set<ConstraintViolation<ManufacturerDTO>> violations = validator.validate(manufacturerDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<ManufacturerDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}
