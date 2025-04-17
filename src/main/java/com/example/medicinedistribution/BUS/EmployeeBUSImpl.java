package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.DAO.Interface.EmployeeDAO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class EmployeeBUSImpl implements EmployeeBUS {

    private final EmployeeDAO employeeDAO;
    private final DataSource dataSource;
    private final UserSession userSession;

    public EmployeeBUSImpl(EmployeeDAO employeeDAO,
                           UserSession userSession,
                           DataSource dataSource) {
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.employeeDAO = employeeDAO;
    }

    @Override
    public boolean insert(EmployeeDTO employeeDTO) {
        if (userSession.hasPermission("INSERT_EMPLOYEE")) {
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
                log.error("Error inserting employee: {}", e.getMessage());
                throw new InsertFailedException(e.getMessage());
            }
        }else {
            log.error("User does not have permission to insert employee");
            throw new PermissionDeniedException("Bạn không có quyền thêm nhân viên");
        }
    }

    @Override
    public boolean update(EmployeeDTO employeeDTO) {
        if (userSession.hasPermission("UPDATE_EMPLOYEE")) {
            try(Connection conn = dataSource.getConnection()) {
                if (employeeDAO.update(employeeDTO, conn)) {
                    log.info("Update successful: {}", employeeDTO);
                    return true;
                } else {
                    log.error("Update failed: {}", employeeDTO);
                    throw new UpdateFailedException("Cập nhật nhân viên thất bại");
                }
            } catch (SQLException e) {
                log.error("Error updating employee: {}", e.getMessage());
                throw new UpdateFailedException(e.getMessage());
            }
        } else {
            log.error("User does not have permission to update employee");
            throw new PermissionDeniedException("Bạn không có quyền sửa nhân viên");
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (userSession.hasPermission("DELETE_EMPLOYEE")) {
            try(Connection conn = dataSource.getConnection()) {
                if (employeeDAO.delete(integer, conn)){
                    log.info("Delete successful: {}", integer);
                    return true;
                } else {
                    log.error("Delete failed: {}", integer);
                    throw new DeleteFailedException("Xóa nhân viên thất bại");
                }
            } catch (SQLException e) {
                log.error("Error deleting employee: {}", e.getMessage());
                throw new DeleteFailedException(e.getMessage());
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
                log.error("Error finding employee: {}", e.getMessage());
                throw new PermissionDeniedException("Tìm nhân viên thất bại");
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
                log.error("Error finding all employees: {}", e.getMessage());
            }
        } else {
            log.error("User does not have permission to find all employees");
            throw new PermissionDeniedException("Bạn không có quyền tìm tất cả nhân viên");
        }
        return null;
    }
}
