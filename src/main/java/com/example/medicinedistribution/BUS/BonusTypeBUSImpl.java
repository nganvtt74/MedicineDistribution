// Implementation for BonusTypeBUSImpl
package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.BonusTypeBUS;
import com.example.medicinedistribution.DAO.Interface.BonusTypeDAO;
import com.example.medicinedistribution.DTO.BonusTypeDTO;
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
public class BonusTypeBUSImpl implements BonusTypeBUS {

    private final BonusTypeDAO bonusTypeDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;

    public BonusTypeBUSImpl(BonusTypeDAO bonusTypeDAO, DataSource dataSource, UserSession userSession, Validator validator) {
        this.bonusTypeDAO = bonusTypeDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public boolean insert(BonusTypeDTO bonusType) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to insert bonus type");
            throw new PermissionDeniedException("Bạn không có quyền thêm loại thưởng");
        }
        valid(bonusType);

        try (Connection conn = dataSource.getConnection()) {
            Integer bonusTypeId = bonusTypeDAO.insert(bonusType, conn);
            if (bonusTypeId != null) {
                bonusType.setId(bonusTypeId);
                log.info("Inserted bonus type: {}", bonusType);
                return true;
            } else {
                log.error("Failed to insert bonus type: {}", bonusType);
                throw new InsertFailedException("Thêm loại thưởng thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean update(BonusTypeDTO bonusType) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to update bonus type");
            throw new PermissionDeniedException("Bạn không có quyền sửa loại thưởng");
        }
        valid(bonusType);

        try (Connection conn = dataSource.getConnection()) {
            boolean result = bonusTypeDAO.update(bonusType, conn);
            if (result) {
                log.info("Updated bonus type: {}", bonusType);
                return true;
            } else {
                log.error("Failed to update bonus type: {}", bonusType);
                throw new UpdateFailedException("Cập nhật loại thưởng thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (!userSession.hasPermission("MANAGE_PAYROLL")) {
            log.error("User does not have permission to delete bonus type");
            throw new PermissionDeniedException("Bạn không có quyền xóa loại thưởng");
        }

        try (Connection conn = dataSource.getConnection()) {
            boolean result = bonusTypeDAO.delete(id, conn);
            if (result) {
                log.info("Deleted bonus type with ID: {}", id);
                return true;
            } else {
                log.error("Failed to delete bonus type with ID: {}", id);
                throw new DeleteFailedException("Không thể xóa loại thưởng này");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public BonusTypeDTO findById(Integer id) {
        if (!userSession.hasPermission("VIEW_PAYROLL")) {
            log.error("User does not have permission to view bonus type");
            throw new PermissionDeniedException("Bạn không có quyền xem loại thưởng");
        }

        try (Connection conn = dataSource.getConnection()) {
            return bonusTypeDAO.findById(id, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<BonusTypeDTO> findAll() {
        if (!userSession.hasPermission("VIEW_PAYROLL")) {
            log.error("User does not have permission to view bonus types");
            throw new PermissionDeniedException("Bạn không có quyền xem danh sách loại thưởng");
        }

        try (Connection conn = dataSource.getConnection()) {
            return bonusTypeDAO.findAll(conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    private void valid(BonusTypeDTO bonusType) {
        Set<ConstraintViolation<BonusTypeDTO>> violations = validator.validate(bonusType);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<BonusTypeDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}