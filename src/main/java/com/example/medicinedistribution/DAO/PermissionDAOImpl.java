package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.PermissionDAO;
import com.example.medicinedistribution.DTO.PermissionDTO;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PermissionDAOImpl implements PermissionDAO {
    @Override
    public String insert(PermissionDTO permissionDTO, Connection conn) {
        return null;
    }

    @Override
    public boolean update(PermissionDTO permissionDTO, Connection conn) {
        return false;
    }

    @Override
    public boolean delete(String s, Connection conn) {
        return false;
    }

    @Override
    public PermissionDTO findById(String permissionCode, Connection conn) {
        String sql = "SELECT * FROM permission WHERE permission_code = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, permissionCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return PermissionDTO.builder()
                            .permissionCode(rs.getString("permission_code"))
                            .permName(rs.getString("permName"))
                            .parentPermissionCode(rs.getString("parent_permission_code"))
                            .editableByPermissionCode(rs.getString("editable_by_permission_code"))
                            .status(rs.getBoolean("status"))
                            .build();
                }
            }
        } catch (SQLException e) {
            log.error("Error finding permission by ID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<PermissionDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM permission";
        List<PermissionDTO> permissions = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                permissions.add(PermissionDTO.builder()
                        .permissionCode(rs.getString("permission_code"))
                        .permName(rs.getString("permName"))
                        .parentPermissionCode(rs.getString("parent_permission_code"))
                        .editableByPermissionCode(rs.getString("editable_by_permission_code"))
                        .status(rs.getBoolean("status"))
                        .build());
            }
        } catch (SQLException e) {
            log.error("Error finding all permissions: {}", e.getMessage());
        }
        return permissions;
    }
}
