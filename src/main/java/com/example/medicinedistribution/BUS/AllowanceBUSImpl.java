package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AllowanceBUS;
import com.example.medicinedistribution.DAO.Interface.AllowanceDAO;
import com.example.medicinedistribution.DTO.AllowanceDTO;
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
public class AllowanceBUSImpl implements AllowanceBUS {

    private final AllowanceDAO allowanceDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;

    public AllowanceBUSImpl(AllowanceDAO allowanceDAO, DataSource dataSource, UserSession userSession, Validator validator) {
        this.allowanceDAO = allowanceDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public boolean insert(AllowanceDTO allowance) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to insert allowance");
            throw new PermissionDeniedException("Bạn không có quyền thêm phụ cấp");
        }
        validateAllowance(allowance);

        try (Connection conn = dataSource.getConnection()) {
            Integer id = allowanceDAO.insert(allowance, conn);
            if (id != null) {
                allowance.setId(id);
                log.info("Inserted allowance: {}", allowance);
                return true;
            } else {
                log.error("Failed to insert allowance");
                throw new InsertFailedException("Thêm phụ cấp thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while inserting allowance", e);
            throw new RuntimeException("Lỗi khi thêm phụ cấp", e);
        }
    }

    @Override
    public boolean update(AllowanceDTO allowance) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to update allowance");
            throw new PermissionDeniedException("Bạn không có quyền cập nhật phụ cấp");
        }
        validateAllowance(allowance);

        try (Connection conn = dataSource.getConnection()) {
            boolean result = allowanceDAO.update(allowance, conn);
            if (result) {
                log.info("Updated allowance: {}", allowance);
                return true;
            } else {
                log.error("Failed to update allowance");
                throw new UpdateFailedException("Cập nhật phụ cấp thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while updating allowance", e);
            throw new RuntimeException("Lỗi khi cập nhật phụ cấp", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to delete allowance");
            throw new PermissionDeniedException("Bạn không có quyền xóa phụ cấp");
        }

        try (Connection conn = dataSource.getConnection()) {
            boolean result = allowanceDAO.delete(id, conn);
            if (result) {
                log.info("Deleted allowance with ID: {}", id);
                return true;
            } else {
                log.error("Failed to delete allowance with ID: {}", id);
                throw new DeleteFailedException("Xóa phụ cấp thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while deleting allowance", e);
            throw new RuntimeException("Lỗi khi xóa phụ cấp", e);
        }
    }

    @Override
    public AllowanceDTO findById(Integer id) {
        try (Connection conn = dataSource.getConnection()) {
            AllowanceDTO allowance = allowanceDAO.findById(id, conn);
            if (allowance == null) {
                log.info("No allowance found with ID: {}", id);
            }
            return allowance;
        } catch (SQLException e) {
            log.error("Error while getting allowance by ID", e);
            throw new RuntimeException("Lỗi khi lấy thông tin phụ cấp", e);
        }
    }

    @Override
    public List<AllowanceDTO> findAll() {
        try (Connection conn = dataSource.getConnection()) {
            return allowanceDAO.findAll(conn);
        } catch (SQLException e) {
            log.error("Error while getting all allowances", e);
            throw new RuntimeException("Lỗi khi lấy danh sách phụ cấp", e);
        }
    }

    private void validateAllowance(AllowanceDTO allowance) {
        Set<ConstraintViolation<AllowanceDTO>> violations = validator.validate(allowance);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<AllowanceDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }

        if (allowance.getName() == null || allowance.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phụ cấp không được để trống");
        }

        if (allowance.getAmount() == null || allowance.getAmount().doubleValue() < 0) {
            throw new IllegalArgumentException("Số tiền phụ cấp phải lớn hơn hoặc bằng 0");
        }

        if (allowance.getIs_insurance_included() == null) {
            throw new IllegalArgumentException("Cần xác định phụ cấp có tính bảo hiểm hay không");
        }
    }
}