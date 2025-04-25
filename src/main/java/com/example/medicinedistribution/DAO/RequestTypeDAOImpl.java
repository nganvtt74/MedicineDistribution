package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.RequestTypeDAO;
import com.example.medicinedistribution.DTO.RequestTypeDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RequestTypeDAOImpl implements RequestTypeDAO {

    @Override
    public Integer insert(RequestTypeDTO requestTypeDTO, Connection conn) {
        String sql = "INSERT INTO request_types (type_name, description, is_active) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, requestTypeDTO.getType_name());
            stmt.setString(2, requestTypeDTO.getDescription());
            stmt.setBoolean(3, requestTypeDTO.getIs_active() != null ? requestTypeDTO.getIs_active() : true);

            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error inserting request type: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(RequestTypeDTO requestTypeDTO, Connection conn) {
        String sql = "UPDATE request_types SET type_name = ?, description = ?, is_active = ? WHERE type_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, requestTypeDTO.getType_name());
            stmt.setString(2, requestTypeDTO.getDescription());
            stmt.setBoolean(3, requestTypeDTO.getIs_active() != null ? requestTypeDTO.getIs_active() : true);
            stmt.setInt(4, requestTypeDTO.getType_id());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error updating request type: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer typeId, Connection conn) {
        String sql = "DELETE FROM request_types WHERE type_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, typeId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error deleting request type: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public RequestTypeDTO findById(Integer typeId, Connection conn) {
        String sql = "SELECT * FROM request_types WHERE type_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, typeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToRequestType(rs);
            }
        } catch (Exception e) {
            log.error("Error finding request type by ID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<RequestTypeDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM request_types";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<RequestTypeDTO> requestTypeList = new ArrayList<>();
            while (rs.next()) {
                requestTypeList.add(mapResultSetToRequestType(rs));
            }
            return requestTypeList;
        } catch (Exception e) {
            log.error("Error finding all request types: {}", e.getMessage());
        }
        return null;
    }

    private RequestTypeDTO mapResultSetToRequestType(ResultSet rs) throws SQLException {
        return RequestTypeDTO.builder()
                .type_id(rs.getInt("type_id"))
                .type_name(rs.getString("type_name"))
                .description(rs.getString("description"))
                .is_active(rs.getBoolean("is_active"))
                .build();
    }
}