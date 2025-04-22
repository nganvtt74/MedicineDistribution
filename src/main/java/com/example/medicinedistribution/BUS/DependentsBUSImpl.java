package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.DependentsBUS;
import com.example.medicinedistribution.DTO.DependentsDTO;
import com.example.medicinedistribution.DAO.Interface.DependentsDAO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;


import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class DependentsBUSImpl implements DependentsBUS {

    private final DependentsDAO dependentsDAO;
    private final UserSession userSession;
    private final Validator validator;
    private final DataSource dataSource;

    public DependentsBUSImpl(DependentsDAO dependentsDAO, UserSession userSession, DataSource dataSource,
                             Validator validator) {
        this.dependentsDAO = dependentsDAO;
        this.userSession = userSession;
        this.dataSource = dataSource;
        this.validator = validator;
    }


    @Override
    public boolean insert(DependentsDTO dependentsDTO) {
        if (!userSession.hasPermission("INSERT_EMPLOYEE")) {
            log.warn("User does not have permission to insert dependents");
            throw new PermissionDeniedException("Bạn không có quyền thêm mới thân nhân");
        }
        try (var connection = dataSource.getConnection()) {
            boolean result = dependentsDAO.insert(dependentsDTO, connection);
            if (result) {
                log.info("Inserted dependent: {}", dependentsDTO);
                return true;
            } else {
                log.warn("Failed to insert dependent: {}", dependentsDTO);
                throw new InsertFailedException("Thêm mới thân nhân thất bại");
            }
        } catch (SQLException e) {
            log.error("Error inserting dependent: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean update(DependentsDTO dependentsDTO) {

        if (!userSession.hasPermission("UPDATE_EMPLOYEE")) {
            log.warn("User does not have permission to update dependents");
            throw new PermissionDeniedException("Bạn không có quyền cập nhật thân nhân");
        }
        try (var connection = dataSource.getConnection()) {
            boolean result = dependentsDAO.update(dependentsDTO, connection);
            if (result) {
                log.info("Updated dependent: {}", dependentsDTO);
                return true;
            } else {
                log.warn("Failed to update dependent: {}", dependentsDTO);
                return false;
            }
        } catch (SQLException e) {
            log.error("Error updating dependent: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean delete(Integer employeeId, Integer dependentNo) {
        if (!userSession.hasPermission("DELETE_EMPLOYEE")) {
            log.warn("User does not have permission to delete dependents");
            throw new PermissionDeniedException("Bạn không có quyền xóa thân nhân");
        }
        try (var connection = dataSource.getConnection()) {
            boolean result = dependentsDAO.delete(employeeId, dependentNo, connection);
            if (result) {
                log.info("Deleted dependent with EmployeeID: {} and DependentNo: {}", employeeId, dependentNo);
                return true;
            } else {
                log.warn("Failed to delete dependent with EmployeeID: {} and DependentNo: {}", employeeId, dependentNo);
                return false;
            }
        } catch (SQLException e) {
            log.error("Error deleting dependent: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<DependentsDTO> findByEmployeeId(Integer employeeId) {
        if (!userSession.hasPermission("VIEW_EMPLOYEE")) {
            log.warn("User does not have permission to view dependents");
            throw new PermissionDeniedException("Bạn không có quyền xem thân nhân");
        }
        try (var connection = dataSource.getConnection()) {
            List<DependentsDTO> dependents = dependentsDAO.findByEmployeeId(employeeId, connection);
            if (dependents != null && !dependents.isEmpty()) {
                log.info("Found {} dependents for EmployeeID: {}", dependents.size(), employeeId);
                return dependents;
            } else {
                log.warn("No dependents found for EmployeeID: {}", employeeId);
                return List.of();
            }
        } catch (SQLException e) {
            log.error("Error finding dependents by EmployeeID: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public DependentsDTO findById(Integer employeeId, Integer dependentNo) {
        if (!userSession.hasPermission("VIEW_EMPLOYEE")) {
            log.warn("User does not have permission to view dependents");
            throw new PermissionDeniedException("Bạn không có quyền xem thân nhân");
        }
        try (var connection = dataSource.getConnection()){
            DependentsDTO dependent = dependentsDAO.findById(employeeId, dependentNo, connection);
            if (dependent != null) {
                log.info("Found dependent: {}", dependent);
                return dependent;
            } else {
                log.warn("No dependent found with EmployeeID: {} and DependentNo: {}", employeeId, dependentNo);
                return null;
            }
        } catch (SQLException e) {
            log.error("Error finding dependent by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteByEmployeeId(Integer employeeId) {
        if (!userSession.hasPermission("DELETE_EMPLOYEE")) {
            log.warn("User does not have permission to delete dependents");
            throw new PermissionDeniedException("Bạn không có quyền xóa thân nhân");
        }
        try (var connection = dataSource.getConnection()) {
            boolean result = dependentsDAO.deleteByEmployeeId(employeeId, connection);
            if (result) {
                log.info("Deleted all dependents for EmployeeID: {}", employeeId);
                return true;
            } else {
                log.warn("Failed to delete dependents for EmployeeID: {}", employeeId);
                return false;
            }
        } catch (SQLException e) {
            log.error("Error deleting dependents by EmployeeID: {}", e.getMessage(), e);
            return false;
        }
    }
}
