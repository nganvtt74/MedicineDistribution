package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.DAO.Interface.DependentsDAO;
import com.example.medicinedistribution.DAO.Interface.EmployeeDAO;
import com.example.medicinedistribution.DAO.Interface.PositionHistoryDAO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PositionHistoryDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class EmployeeBUSImpl implements EmployeeBUS {

    private final EmployeeDAO employeeDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;
    private final PositionHistoryDAO positionHistoryDAO;
    private final TransactionManager transactionManager;
    private final DependentsDAO dependentsDAO;

    public EmployeeBUSImpl(EmployeeDAO employeeDAO,
                           UserSession userSession,
                           DataSource dataSource,
                           Validator validator,
                           PositionHistoryDAO positionHistoryDAO,
                           TransactionManager transactionManager,
                           DependentsDAO dependentsDAO) {
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.employeeDAO = employeeDAO;
        this.validator = validator;
        this.positionHistoryDAO = positionHistoryDAO;
        this.transactionManager = transactionManager;
        this.dependentsDAO = dependentsDAO;
    }

    private void valid(EmployeeDTO employeeDTO) {
        Set<ConstraintViolation<EmployeeDTO>> violations = validator.validate(employeeDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<EmployeeDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }

    @Override
    public boolean insert(EmployeeDTO employeeDTO) {
        if (userSession.hasPermission("INSERT_EMPLOYEE")) {
            valid(employeeDTO);
            try(Connection conn = dataSource.getConnection()) {
                Integer employeeId = employeeDAO.insert(employeeDTO, conn);
                if (employeeId>0) {
                    employeeDTO.setEmployeeId(employeeId);
                    log.info("Insert successful: {}", employeeDTO);
                    return true;
                } else {
                    log.error("Insert failed: {}", employeeDTO);
                    throw new InsertFailedException("Thêm nhân viên thất bại");

                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("User does not have permission to insert employee");
            throw new PermissionDeniedException("Bạn không có quyền thêm nhân viên");
        }
    }

    @Override
    public boolean update(EmployeeDTO employeeDTO) {
        if (userSession.hasPermission("UPDATE_EMPLOYEE")) {
            valid(employeeDTO);
            try(Connection conn = dataSource.getConnection()) {
                if (employeeDAO.update(employeeDTO, conn)) {
                    log.info("Update successful: {}", employeeDTO);
                    return true;
                } else {
                    log.error("Update failed: {}", employeeDTO);
                    throw new UpdateFailedException("Cập nhật nhân viên thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to update employee");
            throw new PermissionDeniedException("Bạn không có quyền sửa nhân viên");
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (userSession.hasPermission("DELETE_EMPLOYEE")) {
            try(Connection conn = transactionManager.beginTransaction()) {
                if (!dependentsDAO.findByEmployeeId(integer, conn).isEmpty()) {
                    if (dependentsDAO.deleteByEmployeeId(integer, conn)) {
                        log.info("Delete dependents successful: {}", integer);
                    } else {
                        log.error("Delete dependents failed: {}", integer);
                        throw new DeleteFailedException("Xóa thân nhân thất bại");
                    }
                }
                if (!positionHistoryDAO.findByEmployeeId(integer, conn).isEmpty()) {
                    if (positionHistoryDAO.delete(integer, conn)) {
                        log.info("Delete position history successful: {}", integer);
                    } else {
                        log.error("Delete position history failed: {}", integer);
                        throw new DeleteFailedException("Xóa lịch sử chức vụ thất bại");
                    }
                }
                if (employeeDAO.delete(integer, conn)){
                    log.info("Delete successful: {}", integer);
                    transactionManager.commitTransaction(conn);
                    return true;
                } else {
                    log.error("Delete failed: {}", integer);
                    throw new DeleteFailedException("Xóa nhân viên thất bại");
                }

            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to delete employee");
            throw new PermissionDeniedException("Bạn không có quyền xóa nhân viên");
        }
    }

    @Override
    public EmployeeDTO findById(Integer integer) {
        if (userSession.hasPermission("VIEW_EMPLOYEE")) {
            try(Connection conn = dataSource.getConnection()) {
                return employeeDAO.findById(integer, conn);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to find employee");
            throw new PermissionDeniedException("Bạn không có quyền tìm nhân viên");
        }
    }

    @Override
    public List<EmployeeDTO> findAll() {
        if (userSession.hasPermission("VIEW_EMPLOYEE")) {
            try(Connection conn = dataSource.getConnection()) {
                return employeeDAO.findAll(conn);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to find all employees");
            throw new PermissionDeniedException("Bạn không có quyền tìm tất cả nhân viên");
        }
    }

    @Override
    public List<EmployeeDTO> getEmployeeWithoutAccount() {
        if (userSession.hasPermission("MANAGE_ACCOUNT")) {
            try(Connection conn = dataSource.getConnection()) {
                return employeeDAO.getEmployeeWithoutAccount(conn);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to find employees without account");
            throw new PermissionDeniedException("Bạn không có quyền tìm nhân viên không có tài khoản");
        }
    }

    @Override
    public boolean insertPersonalInfo(EmployeeDTO employee) {
        if (userSession.hasPermission("INSERT_EMPLOYEE")) {
            validInsert(employee);
            try(Connection conn = transactionManager.beginTransaction()) {
                Integer employeeId = employeeDAO.insert(employee, conn);
                if (employeeId>0) {
                    log.info("Insert successful: {}", employeeId);
                    employee.setEmployeeId(employeeId);
                    log.info("Insert personal info successful: {}", employee);
                    transactionManager.commitTransaction(conn);
                    return true;
                } else {
                    log.error("Insert personal info failed: {}", employee);
                    transactionManager.rollbackTransaction(conn);
                    throw new InsertFailedException("Thêm thông tin cá nhân thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to insert personal info");
            throw new PermissionDeniedException("Bạn không có quyền thêm thông tin cá nhân");
        }
    }

    private void validInsert(EmployeeDTO employee) {
        // Create a custom validator that only validates specific properties
        Validator propertyValidator = validator.unwrap(jakarta.validation.Validator.class);

        // Define which properties to validate
        String[] propertiesToValidate = {"firstName", "lastName", "birthday", "gender", "phone", "email"};

        // Validate only selected properties
        for (String property : propertiesToValidate) {
            Set<ConstraintViolation<EmployeeDTO>> violations =
                    propertyValidator.validateProperty(employee, property);

            if (!violations.isEmpty()) {
                for (ConstraintViolation<EmployeeDTO> violation : violations) {
                    log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                    throw new IllegalArgumentException(violation.getMessage());
                }
            }
        }

        // Add custom validations as needed
        if (employee.getBirthday() != null && employee.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không thể là ngày trong tương lai");
        }
    }

    @Override
    public EmployeeDTO findByEmail(String email) {
        return null;
    }

    @Override
    public boolean updatePersonalInfo(EmployeeDTO employee) {
        if (userSession.hasPermission("UPDATE_EMPLOYEE")) {
            try(Connection conn = transactionManager.beginTransaction()) {
                if (employeeDAO.update(employee, conn)) {
                    log.info("Update personal info successful: {}", employee);
                    transactionManager.commitTransaction(conn);
                    return true;
                } else {
                    log.error("Update personal info failed: {}", employee);
                    transactionManager.rollbackTransaction(conn);
                    throw new UpdateFailedException("Cập nhật thông tin cá nhân thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to update personal info");
            throw new PermissionDeniedException("Bạn không có quyền sửa thông tin cá nhân");
        }
    }

    @Override
    public boolean updateEmploymentInfo(EmployeeDTO employee) {
        if (userSession.hasPermission("UPDATE_EMPLOYEE")) {
            try(Connection conn = transactionManager.beginTransaction()) {
                EmployeeDTO existingEmployee = employeeDAO.findById(employee.getEmployeeId(), conn);
                if (existingEmployee == null) {
                    log.error("Employee not found: {}", employee.getEmployeeId());
                    throw new IllegalArgumentException("Nhân viên không tồn tại");
                }
                if(!Objects.equals(existingEmployee.getPositionId(), employee.getPositionId())) {
                    PositionHistoryDTO positionHistory = new PositionHistoryDTO();
                    positionHistory.setEmployeeId(employee.getEmployeeId());
                    positionHistory.setPositionName(employee.getPositionName());
                    positionHistory.setSalaryBefore(existingEmployee.getBasicSalary());
                    positionHistory.setSalaryAfter(employee.getBasicSalary());
                    if (positionHistoryDAO.insert(positionHistory, conn)<0) {
                        transactionManager.rollbackTransaction(conn);
                        log.error("Insert position history failed: {}", positionHistory);
                        throw new InsertFailedException("Thêm lịch sử chức vụ thất bại");
                    }
                }
                if (employeeDAO.updateEmploymentInfo(employee, conn)) {
                    log.info("Update employment info successful: {}", employee);
                    transactionManager.commitTransaction(conn);
                    return true;
                } else {
                    log.error("Update employment info failed: {}", employee);
                    transactionManager.rollbackTransaction(conn);
                    throw new UpdateFailedException("Cập nhật thông tin việc làm thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to update employment info");
            throw new PermissionDeniedException("Bạn không có quyền sửa thông tin việc làm");
        }
    }

    @Override
    public List<PositionHistoryDTO> findHistoryByEmployeeId(Integer employeeId) {
        if (userSession.hasPermission("VIEW_EMPLOYEE")) {
            try(Connection conn = dataSource.getConnection()) {
                return positionHistoryDAO.findByEmployeeId(employeeId, conn);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        } else {
            log.error("User does not have permission to find employee history");
            throw new PermissionDeniedException("Bạn không có quyền tìm lịch sử nhân viên");
        }
    }
}
