package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.RoleBUS;
import com.example.medicinedistribution.DAO.Interface.PermissionDAO;
import com.example.medicinedistribution.DAO.Interface.RoleDAO;
import com.example.medicinedistribution.DAO.Interface.RolePermDAO;
import com.example.medicinedistribution.DTO.PermissionDTO;
import com.example.medicinedistribution.DTO.RoleDTO;
import com.example.medicinedistribution.DTO.RolePermDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.*;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
public class RoleBUSImpl implements RoleBUS{

    private final RoleDAO roleDao;
    private final DataSource dataSource;
    private final RolePermDAO rolePermDAO;
    private final TransactionManager transactionManager;
    private final UserSession userSession;
    private final PermissionDAO permissionDAO;

    public RoleBUSImpl(DataSource dataSource, RoleDAO roleDao, RolePermDAO rolePermDAO,
                       TransactionManager transactionManager , UserSession userSession ,
                       PermissionDAO permissionDAO) {
        this.dataSource = dataSource;
        this.roleDao = roleDao;
        this.rolePermDAO = rolePermDAO;
        this.transactionManager = transactionManager;
        this.userSession = userSession;
        this.permissionDAO = permissionDAO;
    }

    @Override
    public boolean insert(RoleDTO roleDTO) {
        if (userSession.hasPermission("INSERT_ROLE")) {
            try(Connection connection = transactionManager.beginTransaction()) {
                // Check if the role name already exists
                if (roleDao.findByName(roleDTO.getRoleName(), connection) != null) {
                    log.error("Role name already exists: {}", roleDTO.getRoleName());
                    throw new AlreadyExistsException("Tên quyền đã tồn tại");
                }

                Integer roleId = roleDao.insert(roleDTO, connection);
                if (roleId > 0) {
                    log.info("Inserted role: {}", roleDTO);
                    roleDTO.setRoleId(roleId);
                    // Insert permissions
                    for (PermissionDTO permission : roleDTO.getPermissions()) {
                        if (userSession.hasPermission(permission.getEditableByPermissionCode())) {
                            RolePermDTO rolePermDTO = new RolePermDTO();
                            rolePermDTO.setPermissionCode(permission.getPermissionCode());
                            rolePermDTO.setRoleId(roleId);
                            if (rolePermDAO.insert(rolePermDTO, connection) <= 0) {
                                log.error("Failed to insert permission for role: {}", roleDTO);
                                transactionManager.rollbackTransaction(connection);
                                throw new InsertFailedException("Thêm quyền thất bại");
                            }
                            log.info("Inserted permission for role: {}", rolePermDTO);
                        }else {
                            log.error("Permission not found: {}", permission.getPermissionCode());
                            transactionManager.rollbackTransaction(connection);
                            throw new InsertFailedException("Bạn không có quyền thêm quyền này");
                        }
                    }

                    transactionManager.commitTransaction(connection);
                    return true;
                } else {
                    log.error("Failed to insert role: {}", roleDTO);
                    transactionManager.rollbackTransaction(connection);
                    throw new InsertFailedException("Thêm vai trò thất bại");
                }

            }  catch (SQLException e) {
                log.error("Error while update role with ID: {}" , roleDTO.getRoleId(), e);
                throw new DeleteFailedException("Lỗi hệ thống, không thể xóa vai trò");
            }
        }else {
            log.error("User does not have permission to insert role");
            throw new PermissionDeniedException("Bạn không có quyền thêm vai trò");
        }
    }

    @Override
    public boolean update(RoleDTO roleDTO) {
        if (userSession.hasPermission("UPDATE_ROLE")) {
            try(Connection connection = transactionManager.beginTransaction()) {
                // Check if the role name already exists
                if (roleDao.findById(roleDTO.getRoleId(), connection) == null) {
                    log.error("Role name does not exist: {}", roleDTO.getRoleId());
                    throw new NotExistsException("Tên quyền không tồn tại");
                }

                if (roleDao.update(roleDTO, connection)) {
                    log.info("Updated role: {}", roleDTO);

                    // Insert permissions
                    if (rolePermDAO.delete(roleDTO.getRoleId(), connection)) {
                        log.info("Deleted old permissions for role: {}", roleDTO);
                    } else {
                        log.error("Failed to delete old permissions for role: {}", roleDTO);
                        transactionManager.rollbackTransaction(connection);
                        throw new UpdateFailedException("Cập nhật quyền thất bại");
                    }
                    for (PermissionDTO permission : roleDTO.getPermissions()) {
                        RolePermDTO rolePermDTO = new RolePermDTO();
                        rolePermDTO.setPermissionCode(permission.getPermissionCode());
                        rolePermDTO.setRoleId(roleDTO.getRoleId());
                        if (rolePermDAO.insert(rolePermDTO, connection) <= 0) {
                            log.error("Failed to update permission for role: {}", roleDTO);
                            transactionManager.rollbackTransaction(connection);
                            throw new UpdateFailedException("Cập nhật quyền thất bại");
                        }
                        log.info("Updated permission for role: {}", rolePermDTO);
                    }

                    transactionManager.commitTransaction(connection);
                    return true;
                } else {
                    log.error("Failed to update role: {}", roleDTO);
                    throw new UpdateFailedException("Thêm vai trò thất bại");
                }

            } catch (SQLException e) {
                log.error("Error while update role with ID: {}" , roleDTO.getRoleId(), e);
                throw new DeleteFailedException("Lỗi hệ thống, không thể xóa vai trò");
            }
        }else {
            log.error("User does not have permission to update role");
            throw new PermissionDeniedException("Bạn không có quyền sửa vai trò");
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (userSession.hasPermission("DELETE_ROLE")) {
            try(Connection connection = transactionManager.beginTransaction()) {

                // Kiểm tra vai trò có tồn tại không
                if (roleDao.findById(integer, connection) == null) {
                    log.error("Role does not exist: {}", integer);
                    transactionManager.rollbackTransaction(connection);
                    throw new NotExistsException("Vai trò không tồn tại");  // Lỗi nghiệp vụ, ném lên trên
                }

                // Xóa permissions của vai trò
                if (rolePermDAO.delete(integer, connection)) {
                    log.info("Deleted permissions for role with ID: {}", integer);
                } else {
                    log.error("Failed to delete permissions for role with ID: {}", integer);
                    transactionManager.rollbackTransaction(connection);
                    throw new DeleteFailedException("Xóa quyền thất bại");  // Lỗi truy vấn, ném lên trên
                }

                // Xóa role
                if (roleDao.delete(integer, connection)) {
                    log.info("Deleted role with ID: {}", integer);
                    transactionManager.commitTransaction(connection);  // Commit giao dịch
                    return true;
                } else {
                    log.error("Failed to delete role with ID: {}", integer);
                    transactionManager.rollbackTransaction(connection);
                    throw new DeleteFailedException("Xóa vai trò thất bại");  // Lỗi truy vấn, ném lên trên
                }

            }catch (SQLException e) {
                log.error("Error while deleting role with ID: {}", integer, e);
                throw new DeleteFailedException("Lỗi hệ thống, không thể xóa vai trò");
            }
        }else {
            log.error("User does not have permission to delete role");
            throw new PermissionDeniedException("Bạn không có quyền xóa vai trò");
        }
    }


    @Override
    public RoleDTO findById(Integer integer) {
        RoleDTO roleDTO;
        try(Connection connection = dataSource.getConnection()) {
            roleDTO = roleDao.findById(integer, connection);
            if (roleDTO == null) {
                log.error("Role not found with ID: {}", integer);
                throw new NotExistsException("Vai trò không tồn tại");
            }else {
                List<PermissionDTO> permissions = rolePermDAO.findByRoleId(integer, connection);
                roleDTO.setPermissions(permissions);
                return roleDTO;
            }
        } catch (SQLException e) {
            log.error("Error while finding role with ID: {}", integer, e);
            throw new RuntimeException("Lỗi hệ thống, không thể kết nối đến cơ sở dữ liệu");
        }
    }

    @Override
    public List<RoleDTO> findAll() {
        List<RoleDTO> roles ;
        try(Connection connection = dataSource.getConnection()) {
            roles =roleDao.findAll(connection);
            if (roles.isEmpty()) {
                log.error("No roles found");
                throw new NotExistsException("Không tìm thấy vai trò nào");
            }
            return roles;
        } catch (SQLException e) {
            log.error("Error while finding all roles", e);
            throw new RuntimeException("Lỗi hệ thống, không thể kết nối đến cơ sở dữ liệu");
        }
    }


    @Override
    public RoleDTO getRoleForEdit(Integer roleId) {
        if (userSession.hasPermission("MANAGE_ROLE")) {
            RoleDTO roleDTO;
            try(Connection connection = dataSource.getConnection()) {
                roleDTO = roleDao.findById(roleId, connection);
                if (roleDTO == null) {
                    log.error("Role not found with ID: {}", roleId);
                    throw new NotExistsException("Vai trò không tồn tại");
                }else {
                    List<PermissionDTO> permissions = rolePermDAO.findByRoleId(roleId, connection);
                    List<PermissionDTO> allPermissions = permissionDAO.findAll(connection);

                    HashSet<String> permissionCodes = permissions.stream()
                            .map(PermissionDTO::getPermissionCode)
                            .collect(Collectors.toCollection(HashSet::new));

                    for (PermissionDTO perm : allPermissions) {
                        boolean existsInRole = permissionCodes.contains(perm.getPermissionCode());
                        perm.setChecked(existsInRole);
                        perm.setEditable(userSession.hasPermission(perm.getEditableByPermissionCode()));
                    }

                    roleDTO.setPermissions(allPermissions);
                    return roleDTO;
                }
            } catch (SQLException e) {
                log.error("Error while finding role with ID: {}", roleId, e);
                throw new RuntimeException("Lỗi hệ thống, không thể kết nối đến cơ sở dữ liệu");
            }
        }else {
            log.error("User does not have permission to edit role");
            throw new PermissionDeniedException("Bạn không có quyền chỉnh sửa vai trò");
        }
    }
}
