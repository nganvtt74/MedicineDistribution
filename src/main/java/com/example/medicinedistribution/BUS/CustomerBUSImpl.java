package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
import com.example.medicinedistribution.DAO.Interface.CustomerDAO;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.CustomerDTO;
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
public class CustomerBUSImpl implements CustomerBUS {

    private final CustomerDAO customerDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;


    public CustomerBUSImpl(CustomerDAO customerDAO, DataSource dataSource,UserSession userSession , Validator validator) {
        this.dataSource = dataSource;
        this.customerDAO = customerDAO;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public boolean insert(CustomerDTO customerDTO) {
        if (!userSession.hasPermission("INSERT_CUSTOMER")) {
            log.error("User does not have permission to insert customer");
            throw new PermissionDeniedException("Bạn không có quyền thêm khách hàng");
        }
        valid(customerDTO);

        try(Connection conn = dataSource.getConnection()) {
            Integer customerId = customerDAO.insert(customerDTO, conn);
            if (customerId > 0) {
                customerDTO.setCustomerId(customerId);
                log.info("Inserted customer: {}", customerDTO);
                return true;

            }else {
                log.error("Failed to insert customer: {}", customerDTO);
                throw new InsertFailedException("Thêm khách hàng thất bại");}
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean update(CustomerDTO customerDTO) {
        if (!userSession.hasPermission("UPDATE_CUSTOMER")) {
            log.error("User does not have permission to update customer");
            throw new PermissionDeniedException("Bạn không có quyền sửa khách hàng");
        }
        valid(customerDTO);
        try(Connection conn = dataSource.getConnection()) {
            boolean result = customerDAO.update(customerDTO, conn);
            if (result) {
                log.info("Updated customer: {}", customerDTO);
                return true;
            } else {
                log.error("Failed to update customer: {}", customerDTO);
                throw new UpdateFailedException("Cập nhật khách hàng thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (!userSession.hasPermission("DELETE_CUSTOMER")) {
            log.error("User does not have permission to delete customer");
            throw new PermissionDeniedException("Bạn không có quyền xóa khách hàng");
        }
        try(Connection conn = dataSource.getConnection()) {
            boolean result = customerDAO.delete(integer, conn);
            if (result) {
                log.info("Deleted customer with ID: {}", integer);
                return true;
            } else {
                log.error("Failed to delete customer with ID: {}", integer);
                throw new DeleteFailedException("Xóa khách hàng thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public CustomerDTO findById(Integer integer) {
        if (!userSession.hasPermission("VIEW_CUSTOMER")) {
            log.error("User does not have permission to view customer");
            throw new PermissionDeniedException("Bạn không có quyền xem khách hàng");
        }
        try(Connection conn = dataSource.getConnection()) {
            return customerDAO.findById(integer, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<CustomerDTO> findAll() {
        if (!userSession.hasPermission("VIEW_CUSTOMER")) {
            log.error("User does not have permission to view customers");
            throw new PermissionDeniedException("Bạn không có quyền xem danh sách khách hàng");
        }
        try(Connection conn = dataSource.getConnection()) {
            return customerDAO.findAll(conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    private void valid(CustomerDTO customerDTO) {
        Set<ConstraintViolation<CustomerDTO>> violations = validator.validate(customerDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<CustomerDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}
