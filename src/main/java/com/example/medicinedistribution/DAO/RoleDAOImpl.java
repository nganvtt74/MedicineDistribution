package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.RoleDAO;
import com.example.medicinedistribution.DTO.RoleDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class RoleDAOImpl implements RoleDAO {
    @Override
    public Integer insert(RoleDTO roleDTO, Connection conn) {
        String sql = "INSERT INTO role (roleName) VALUES (?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, roleDTO.getRoleName());
            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return 0;
    }

    @Override
    public boolean update(RoleDTO roleDTO, Connection conn) {
        String sql = "UPDATE role SET roleName = ? WHERE roleId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleDTO.getRoleName());
            stmt.setInt(2, roleDTO.getRoleId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Integer integer, Connection conn)  {
        try(PreparedStatement stmt = conn.prepareStatement("DELETE FROM role WHERE roleId = ?")) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public RoleDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM role WHERE roleId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return RoleDTO.builder()
                        .roleId(rs.getInt("roleId"))
                        .roleName(rs.getString("roleName"))
                        .status(rs.getBoolean("status"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                        .build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<RoleDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM role";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<RoleDTO> roles = new ArrayList<>();
            while (rs.next()) {
                roles.add(RoleDTO.builder()
                        .roleId(rs.getInt("roleId"))
                        .roleName(rs.getString("roleName"))
                        .status(rs.getBoolean("status"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                        .build());
            }
            return roles;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public RoleDTO findByName(String roleName, Connection connection) {
        String sql = "SELECT * FROM role WHERE roleName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return RoleDTO.builder()
                        .roleId(rs.getInt("roleId"))
                        .roleName(rs.getString("roleName"))
                        .status(rs.getBoolean("status"))
                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                        .build();
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
