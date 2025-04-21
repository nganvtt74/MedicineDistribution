package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.AccountDAO;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.RoleDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AccountDAOImpl implements AccountDAO {

    @Override
    public Integer insert(AccountDTO account, Connection conn) {
        String sql = "INSERT INTO Account (employeeId, username, password,roleId) VALUES (?, ?, ?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, account.getEmployeeId());
            stmt.setString(2, account.getUsername());
            stmt.setString(3, account.getPassword());
            stmt.setInt(4, account.getRoleId());
//            return stmt.executeUpdate() > 0 ? stmt.getGeneratedKeys().getInt(1) : 0;
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean update(AccountDTO account, Connection conn) {
        String sql = "UPDATE Account SET roleId=? WHERE accountId=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, account.getRoleId());
            stmt.setInt(2, account.getAccountId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage()); return false;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM Account WHERE accountId=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage()); return false;
        }
    }

    @Override
    public AccountDTO findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM Account WHERE accountId=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return AccountDTO.builder()
                        .accountId(rs.getInt("accountId"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .employeeId(rs.getInt("employeeId"))
                        .roleId(rs.getInt("roleId"))
                        .build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<AccountDTO> findAll(Connection conn) {
        List<AccountDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM Account";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(AccountDTO.builder()
                        .accountId(rs.getInt("accountId"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .employeeId(rs.getInt("employeeId"))
                        .roleId(rs.getInt("roleId"))
                        .build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return list;
    }

    @Override
    public AccountDTO findByUsername(String username, Connection conn) {
        String sql = "SELECT * FROM Account WHERE username=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return AccountDTO.builder()
                        .accountId(rs.getInt("accountId"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .employeeId(rs.getInt("employeeId"))
                        .roleId(rs.getInt("roleId"))
                        .build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updatePassword(AccountDTO updatedAccount, Connection connection) {
        String sql = "UPDATE Account SET password=? WHERE accountId=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, updatedAccount.getPassword());
            stmt.setInt(2, updatedAccount.getAccountId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public ArrayList<AccountDTO> getAccountByRoleId(List<RoleDTO> roleList, Connection connection) {
        ArrayList<AccountDTO> accountList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM Account WHERE roleId IN (");
        for (int i = 0; i < roleList.size(); i++) {
            sql.append("?");
            if (i < roleList.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < roleList.size(); i++) {
                stmt.setInt(i + 1, roleList.get(i).getRoleId());
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accountList.add(AccountDTO.builder()
                        .accountId(rs.getInt("accountId"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .employeeId(rs.getInt("employeeId"))
                        .roleId(rs.getInt("roleId"))
                        .build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return accountList;
    }

    @Override
    public ArrayList<AccountDTO> getAccountByNullRoleId(Connection connection) {
        ArrayList<AccountDTO> accountList = new ArrayList<>();
        String sql = "SELECT * FROM Account WHERE roleId IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accountList.add(AccountDTO.builder()
                        .accountId(rs.getInt("accountId"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .employeeId(rs.getInt("employeeId"))
                        .roleId(rs.getInt("roleId"))
                        .build());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return accountList;
    }
}
