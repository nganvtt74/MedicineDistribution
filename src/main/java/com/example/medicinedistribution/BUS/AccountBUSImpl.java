package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AccountBUS;
import com.example.medicinedistribution.DAO.Interface.AccountDAO;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.CategoryDTO;
import com.example.medicinedistribution.DTO.RoleDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.AlreadyExistsException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Util.PasswordUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class AccountBUSImpl implements AccountBUS {

    private final AccountDAO accountDAO ;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;

    public AccountBUSImpl(DataSource dataSource, AccountDAO accountDAO, UserSession userSession , Validator validator) {
        this.dataSource = dataSource;
        this.accountDAO = accountDAO;
        this.userSession = userSession;
        this.validator = validator;
    }
    @Override
    public AccountDTO findByUsername(String username) {
        if (userSession.hasPermission("VIEW_ACCOUNT")) {
            try(Connection connection = dataSource.getConnection()) {
                return accountDAO.findByUsername(username, connection);
            } catch (Exception e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền tìm tài khoản");
            throw new RuntimeException("Bạn không có quyền tìm tài khoản");
        }
    }

    @Override
    public void resetPassword(AccountDTO updatedAccount) {
        if (userSession.hasPermission("UPDATE_ACCOUNT")) {
            valid(updatedAccount);
            try(Connection connection = dataSource.getConnection()) {
                AccountDTO existingAccount = accountDAO.findByUsername(updatedAccount.getUsername(), connection);
                if (existingAccount == null) {
                    log.error("Tài khoản không tồn tại: {}", updatedAccount);
                    throw new AlreadyExistsException("Tài khoản không tồn tại");
                }
                updatedAccount.setPassword(PasswordUtil.hashPassword(updatedAccount.getPassword()));
                if (accountDAO.updatePassword(updatedAccount, connection)) {
                    log.info("Cập nhật mật khẩu tài khoản thành công: {}", updatedAccount);
                } else {
                    log.error("Cập nhật mật khẩu tài khoản thất bại: {}", updatedAccount);
                    throw new UpdateFailedException("Cập nhật mật khẩu tài khoản thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền cập nhật mật khẩu tài khoản");
            throw new UpdateFailedException("Bạn không có quyền cập nhật mật khẩu tài khoản");
        }

    }

    @Override
    public ArrayList<AccountDTO> getAccountByRoleId(List<RoleDTO> roleList) {
        if (userSession.hasPermission("VIEW_ACCOUNT")) {
            try(Connection connection = dataSource.getConnection()) {
                return accountDAO.getAccountByRoleId(roleList, connection);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền tìm tài khoản");
            throw new RuntimeException("Bạn không có quyền tìm tài khoản");
        }
    }

    @Override
    public ArrayList<AccountDTO> getAccountByNullRoleId() {
        if (userSession.hasPermission("VIEW_ACCOUNT")) {
            try(Connection connection = dataSource.getConnection()) {
                return accountDAO.getAccountByNullRoleId(connection);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền tìm tài khoản");
            throw new RuntimeException("Bạn không có quyền tìm tài khoản");
        }
    }

    @Override
    public boolean insert(AccountDTO accountDTO){
        if (userSession.hasPermission("INSERT_ACCOUNT")) {
            valid(accountDTO);
            try(Connection connection = dataSource.getConnection()) {
                if (accountDAO.findByUsername(accountDTO.getUsername(), connection) != null) {
                    log.error("Tài khoản đã tồn tại: {}", accountDTO);
                    throw new AlreadyExistsException("Tài khoản đã tồn tại");
                }else{
                    accountDTO.setPassword(PasswordUtil.hashPassword(accountDTO.getPassword()));
                    Integer accountId = accountDAO.insert(accountDTO, connection);
                    if (accountId > 0) {
                        accountDTO.setAccountId(accountId);
                        log.info("Thêm tài khoản thành công: {}", accountDTO);
                        return true;
                    } else {
                        log.error("Thêm tài khoản thất bại: {}", accountDTO);
                        throw new InsertFailedException("Thêm tài khoản thất bại");
                    }
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền thêm tài khoản");
            throw new InsertFailedException("Bạn không có quyền thêm tài khoản");
        }
    }

    @Override
    public boolean update(AccountDTO accountDTO) {

        if (userSession.hasPermission("UPDATE_ACCOUNT")) {
            valid(accountDTO);
            try(Connection connection = dataSource.getConnection()) {
                if (accountDAO.update(accountDTO, connection)) {
                    log.info("Cập nhật tài khoản thành công: {}", accountDTO);
                    return true;
                } else {
                    log.error("Cập nhật tài khoản thất bại: {}", accountDTO);
                    throw new UpdateFailedException("Cập nhật tài khoản thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền cập nhật tài khoản");
            throw new UpdateFailedException("Bạn không có quyền cập nhật tài khoản");
        }
    }

    @Override
    public boolean delete(Integer s) {
        if (userSession.hasPermission("DELETE_ACCOUNT")) {
            try(Connection connection = dataSource.getConnection()) {
                if (accountDAO.delete(s, connection)) {
                    log.info("Xóa tài khoản thành công: {}", s);
                    return true;
                } else {
                    log.error("Xóa tài khoản thất bại: {}", s);
                    throw new DeleteFailedException("Xóa tài khoản thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền xóa tài khoản");
            throw new DeleteFailedException("Bạn không có quyền xóa tài khoản");
        }
    }

    @Override
    public AccountDTO findById(Integer s) {
        if (userSession.hasPermission("VIEW_ACCOUNT")) {
            try (Connection connection = dataSource.getConnection()) {
                return accountDAO.findById(s, connection);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền tìm tài khoản");
            throw new RuntimeException("Bạn không có quyền tìm tài khoản");
        }
    }

    @Override
    public List<AccountDTO> findAll() {
        if (userSession.hasPermission("VIEW_ACCOUNT")) {
            try(Connection connection = dataSource.getConnection()) {
                return accountDAO.findAll(connection);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("Không có quyền tìm tài khoản");
            throw new RuntimeException("Bạn không có quyền tìm tài khoản");
        }
    }

    private void valid(AccountDTO accountDTO) {
        Set<ConstraintViolation<AccountDTO>> violations = validator.validate(accountDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<AccountDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}
