package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.RolePermDAO;
import com.example.medicinedistribution.DTO.PermissionDTO;
import com.example.medicinedistribution.DTO.RolePermDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RolePermDAOImpl implements RolePermDAO {
    @Override
    public Integer insert(RolePermDTO rolePermDTO, Connection conn) {
        String sql = "INSERT INTO role_perm (permission_code,roleId) VALUES (?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, rolePermDTO.getPermissionCode());
            stmt.setInt(2, rolePermDTO.getRoleId());
            return stmt.executeUpdate() > 0 ? 1 : 0 ;
        }catch (SQLException e) {
            log.error("Error inserting RolePerm: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(RolePermDTO rolePermDTO, Connection conn) {
        String sql = "UPDATE role_perm SET permission_code = ? WHERE roleId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rolePermDTO.getPermissionCode());
            stmt.setInt(2, rolePermDTO.getRoleId());
            return stmt.executeUpdate() > 0;
        }catch (SQLException e) {
            log.error("Error updating RolePerm: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM role_perm WHERE roleId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }catch (SQLException e) {
            log.error("Error deleting RolePerm: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public RolePermDTO findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM role_perm WHERE roleId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return RolePermDTO.builder()
                            .roleId(rs.getInt("permId"))
                            .permissionCode(rs.getString("permName"))
                            .build();
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching RolePerm by id: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<RolePermDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM role_perm";
        List<RolePermDTO> rolePerms = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rolePerms.add(RolePermDTO.builder()
                        .roleId(rs.getInt("permId"))
                        .permissionCode(rs.getString("permName"))
                        .build());
            }
        } catch (SQLException e) {
            log.error("Error fetching all RolePerm: {}", e.getMessage());
        }
        return rolePerms;
    }

    @Override
    public List<PermissionDTO> findByRoleId(Integer roleId, Connection conn) {
        String sql = "SELECT p.* FROM role_perm rp JOIN permission p ON rp.permission_code = p.permission_code WHERE rp.roleId = ?";
        List<PermissionDTO> permissions = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permissions.add(PermissionDTO.builder()
                            .permissionCode(rs.getString("permission_code"))
                            .permName(rs.getString("permName"))
                            .parentPermissionCode(rs.getString("parent_permission_code"))
                            .editableByPermissionCode(rs.getString("editable_by_permission_code"))
                            .status(rs.getInt("status"))
                            .build());
                }
            }
            return permissions;
        } catch (SQLException e) {
            log.error("Error fetching permissions by roleId: {}", e.getMessage());
        }
        return null;
    }
}