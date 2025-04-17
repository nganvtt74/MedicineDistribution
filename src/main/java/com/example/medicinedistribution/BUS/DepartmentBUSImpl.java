package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.DepartmentBUS;
import com.example.medicinedistribution.DAO.Interface.DepartmentDAO;
import com.example.medicinedistribution.DTO.DepartmentDTO;
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
public class DepartmentBUSImpl implements DepartmentBUS {

    private final DepartmentDAO departmentDAO;
    private final UserSession userSession;
    private final DataSource dataSource;

    public DepartmentBUSImpl(DepartmentDAO departmentDAO, UserSession userSession, DataSource dataSource) {
        this.departmentDAO = departmentDAO;
        this.userSession = userSession;
        this.dataSource = dataSource;
    }



    @Override
    public boolean insert(DepartmentDTO departmentDTO) {
        if (userSession.hasPermission("INSERT_DEPARTMENT")) {
            try(Connection conn = dataSource.getConnection()) {
                Integer departmentId = departmentDAO.insert(departmentDTO, conn);
                if (departmentId > 0){
                    departmentDTO.setDepartmentId(departmentId);
                    log.info("Department added successfully");
                    return true;
                } else {
                    log.error("Department not added successfully");
                    throw new InsertFailedException("Thêm bộ phận không thành công");
                }
            } catch (SQLException e){
                log.error(e.getMessage());
                throw new InsertFailedException("Thêm bộ phận không thành công");
            }
        }else {
            log.error("User does not have permission to create department");
            throw new PermissionDeniedException("Bạn không có quyền tạo bộ phận");
        }
    }

    @Override
    public boolean update(DepartmentDTO departmentDTO) {
        if (userSession.hasPermission("UPDATE_DEPARTMENT")) {
            try(Connection conn = dataSource.getConnection()) {
                if (departmentDAO.update(departmentDTO, conn)){
                    log.info("Department updated successfully");
                    return true;
                } else {
                    log.error("Department not updated successfully");
                    throw new UpdateFailedException("Sửa bộ phận không thành công");
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new UpdateFailedException("Sửa bộ phận không thành công");
            }
        } else {
            log.error("User does not have permission to update department");
            throw new PermissionDeniedException("Bạn không có quyền sửa bộ phận");
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (userSession.hasPermission("DELETE_DEPARTMENT")) {
            try(Connection conn = dataSource.getConnection()) {
                if (departmentDAO.delete(integer, conn)){
                    log.info("Department deleted successfully");
                    return true;
                } else {
                    log.error("Department not deleted successfully");
                    throw new DeleteFailedException("Xóa bộ phận không thành công");
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new DeleteFailedException("Xóa bộ phận không thành công");
            }
        } else {
            log.error("User does not have permission to delete department");
            throw new PermissionDeniedException("Bạn không có quyền xóa bộ phận");
        }
    }

    @Override
    public DepartmentDTO findById(Integer integer) {
        if (userSession.hasPermission("VIEW_DEPARTMENT")) {
            try(Connection conn = dataSource.getConnection()) {
                return departmentDAO.findById(integer, conn);
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new RuntimeException("Lấy thông tin bộ phận không thành công");
            }
        } else {
            log.error("User does not have permission to get department");
            throw new PermissionDeniedException("Bạn không có quyền lấy thông tin bộ phận");
        }
    }

    @Override
    public List<DepartmentDTO> findAll() {
        if (userSession.hasPermission("VIEW_DEPARTMENT")) {
            try(Connection conn = dataSource.getConnection()) {
                return departmentDAO.findAll(conn);
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new RuntimeException("Lấy danh sách bộ phận không thành công");
            }
        } else {
            log.error("User does not have permission to get all departments");
            throw new PermissionDeniedException("Bạn không có quyền lấy danh sách bộ phận");
        }
    }

}
