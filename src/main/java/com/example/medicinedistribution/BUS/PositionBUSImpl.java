package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.PositionBUS;
import com.example.medicinedistribution.DAO.Interface.PositionDAO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

@Slf4j
public class PositionBUSImpl implements PositionBUS {
    private final PositionDAO positionDAO;
    private final UserSession userSession;
    private final DataSource dataSource;
    private final Validator validator;

    public PositionBUSImpl(PositionDAO positionDAO, UserSession userSession, DataSource dataSource, Validator validator) {
        this.positionDAO = positionDAO;
        this.userSession = userSession;
        this.dataSource = dataSource;
        this.validator = validator;
    }

    private void valid(PositionDTO positionDTO) {
        Set<ConstraintViolation<PositionDTO>> violations = validator.validate(positionDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<PositionDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }

    @Override
    public boolean insert(PositionDTO positionDTO) {
        valid(positionDTO);
        if (userSession.hasPermission("INSERT_POSITION")) {
            try(Connection conn = dataSource.getConnection()) {
                Integer positionId = positionDAO.insert(positionDTO,conn);
                if (positionId > 0) {
                    positionDTO.setPositionId(positionId);
                    log.info("Insert position successful: {}", positionDTO);
                    return true;
                } else {
                    log.error("Insert position failed: {}", positionDTO);
                    throw new InsertFailedException("Thêm chức vụ không thành công");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("Permission denied for inserting position");
            throw new PermissionDeniedException("Bạn không có quyền thêm chức vụ");
        }
    }

    @Override
    public boolean update(PositionDTO positionDTO) {
        valid(positionDTO);
        if (userSession.hasPermission("UPDATE_POSITION")) {
            try(Connection conn = dataSource.getConnection()) {
                if (positionDAO.update(positionDTO,conn)) {
                    log.info("Update position successful: {}", positionDTO);
                    return true;
                } else {
                    log.error("Update position failed: {}", positionDTO);
                    throw new InsertFailedException("Cập nhật chức vụ không thành công");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("Permission denied for updating position");
            throw new PermissionDeniedException("Bạn không có quyền cập nhật chức vụ");
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (userSession.hasPermission("VIEW_POSITION")) {
            try(Connection conn = dataSource.getConnection()) {
                if (positionDAO.delete(integer,conn)) {
                    log.info("Delete position successful: {}", integer);
                    return true;
                } else {
                    log.error("Delete position failed: {}", integer);
                    throw new InsertFailedException("Xóa chức vụ không thành công");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("Permission denied for deleting position");
            throw new PermissionDeniedException("Bạn không có quyền xóa chức vụ");
        }
    }

    @Override
    public PositionDTO findById(Integer integer) {
        if (userSession.hasPermission("VIEW_POSITION")) {
            try(Connection conn = dataSource.getConnection()) {
                return positionDAO.findById(integer,conn);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("Permission denied for finding position");
            throw new PermissionDeniedException("Bạn không có quyền xem chức vụ");
        }
    }

    @Override
    public List<PositionDTO> findAll() {
        if (userSession.hasPermission("VIEW_POSITION")) {
            try(Connection conn = dataSource.getConnection()) {
                return positionDAO.findAll(conn);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("Permission denied for finding all positions");
            throw new PermissionDeniedException("Bạn không có quyền xem tất cả chức vụ");
        }
    }
}
