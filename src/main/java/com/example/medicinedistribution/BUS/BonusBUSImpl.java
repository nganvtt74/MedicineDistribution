// Implementation for BonusBUSImpl
package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.BonusBUS;
import com.example.medicinedistribution.DAO.Interface.BonusDAO;
import com.example.medicinedistribution.DTO.BonusDTO;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
public class BonusBUSImpl implements BonusBUS {

    private final BonusDAO bonusDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;

    public BonusBUSImpl(BonusDAO bonusDAO, DataSource dataSource, UserSession userSession, Validator validator) {
        this.bonusDAO = bonusDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public boolean insert(BonusDTO bonus) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to insert bonus");
            throw new PermissionDeniedException("Bạn không có quyền thêm khoản thưởng");
        }
        valid(bonus);

        try (Connection conn = dataSource.getConnection()) {
            Integer bonusId = bonusDAO.insert(bonus, conn);
            if (bonusId != null) {
                bonus.setId(bonusId);
                log.info("Inserted bonus: {}", bonus);
                return true;
            } else {
                log.error("Failed to insert bonus: {}", bonus);
                throw new InsertFailedException("Thêm khoản thưởng thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean update(BonusDTO bonus) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to update bonus");
            throw new PermissionDeniedException("Bạn không có quyền sửa khoản thưởng");
        }
        valid(bonus);

        try (Connection conn = dataSource.getConnection()) {
            boolean result = bonusDAO.update(bonus, conn);
            if (result) {
                log.info("Updated bonus: {}", bonus);
                return true;
            } else {
                log.error("Failed to update bonus: {}", bonus);
                throw new UpdateFailedException("Cập nhật khoản thưởng thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to delete bonus");
            throw new PermissionDeniedException("Bạn không có quyền xóa khoản thưởng");
        }

        try (Connection conn = dataSource.getConnection()) {
            boolean result = bonusDAO.delete(id, conn);
            if (result) {
                log.info("Deleted bonus with ID: {}", id);
                return true;
            } else {
                log.error("Failed to delete bonus with ID: {}", id);
                throw new DeleteFailedException("Không thể xóa khoản thưởng này");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public BonusDTO findById(Integer id) {
        if (!userSession.hasPermission("VIEW_PAYROLL")) {
            log.error("User does not have permission to view bonus");
            throw new PermissionDeniedException("Bạn không có quyền xem khoản thưởng");
        }

        try (Connection conn = dataSource.getConnection()) {
            return bonusDAO.findById(id, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<BonusDTO> findAll() {
        if (!userSession.hasPermission("VIEW_PAYROLL")) {
            log.error("User does not have permission to view bonuses");
            throw new PermissionDeniedException("Bạn không có quyền xem danh sách khoản thưởng");
        }

        try (Connection conn = dataSource.getConnection()) {
            return bonusDAO.findAll(conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<BonusDTO> findByEmployeeId(Integer employeeId) {
        if (!userSession.hasPermission("VIEW_PAYROLL")) {
            log.error("User does not have permission to view employee bonuses");
            throw new PermissionDeniedException("Bạn không có quyền xem khoản thưởng của nhân viên");
        }

        try (Connection conn = dataSource.getConnection()) {
            return bonusDAO.findByEmployeeId(employeeId, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<BonusDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (!userSession.hasPermission("VIEW_PAYROLL")) {
            log.error("User does not have permission to view bonuses by date range");
            throw new PermissionDeniedException("Bạn không có quyền xem khoản thưởng theo khoảng thời gian");
        }

        try (Connection conn = dataSource.getConnection()) {
            return bonusDAO.findByDateRange(startDate, endDate, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<BonusDTO> getByMothYear(int monthValue, int year) {
        if (!userSession.hasPermission("VIEW_PAYROLL")) {
            log.error("User does not have permission to view bonuses by month and year");
            throw new PermissionDeniedException("Bạn không có quyền xem khoản thưởng theo tháng và năm");
        }

        try (Connection conn = dataSource.getConnection()) {
            return bonusDAO.getByMothYear(monthValue, year, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    private void valid(BonusDTO bonus) {
        Set<ConstraintViolation<BonusDTO>> violations = validator.validate(bonus);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<BonusDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}