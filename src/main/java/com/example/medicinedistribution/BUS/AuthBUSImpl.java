package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.RoleBUS;
import com.example.medicinedistribution.DAO.Interface.AccountDAO;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.PermissionDTO;
import com.example.medicinedistribution.DTO.RoleDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

@Slf4j
public class AuthBUSImpl implements AuthBUS {

    private AccountDAO accountDAO;
    private RoleBUS roleBUS;
    private UserSession userSession;
    private DataSource dataSource;

    public AuthBUSImpl(DataSource dataSource ,AccountDAO accountDAO, RoleBUS roleBUS, UserSession userSession) {
        this.accountDAO = accountDAO;
        this.roleBUS = roleBUS;
        this.userSession = userSession;
        this.dataSource = dataSource;
    }

    @Override
    public boolean login(String username, String password) {
        try(Connection connection = dataSource.getConnection()) {
            AccountDTO account = accountDAO.findByUsername(username,connection);
            if (account != null && PasswordUtil.checkPassword(password, account.getPassword())) {
                RoleDTO role = roleBUS.findById(account.getRoleId());
//                userSession = new UserSession(account, role);
                userSession.setAccount(account);

                HashMap<String, PermissionDTO> permissions = new HashMap<>();
                for (PermissionDTO permission : role.getPermissions()) {
                    permissions.put(permission.getPermissionCode(), permission);
                }
                userSession.setRole(role);
                userSession.setPermissions(permissions);
                return true;
            }else {
                log.error("Invalid username or password");
                throw new RuntimeException("Tài khoản hoặc mật khẩu không hợp lệ");
            }
        }catch (SQLException e){
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public boolean register(String username, String password, int role) {
        try (Connection connection = dataSource.getConnection()) {
            if (accountDAO.findByUsername(username, connection) != null) {
                log.error("Username already exists: {}", username);
                return false;
            }
            AccountDTO account = new AccountDTO();
            account.setUsername(username);
            account.setPassword(PasswordUtil.hashPassword(password));
            RoleDTO roleDTO = roleBUS.findById(role);
            if (roleDTO == null) {
                log.error("Role not found: {}", role);
                return false;
            }
            account.setRoleId(roleDTO.getRoleId());
            return accountDAO.insert(account, connection) > 0;
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        return false;
    }

    @Override
    public boolean resetPassword(String username, String newPassword) {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }
}
