package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.RequestDAO;
import com.example.medicinedistribution.DTO.RequestsDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RequestDAOImpl implements RequestDAO {

    @Override
    public Integer insert(RequestsDTO requestsDTO, Connection conn) {
        String sql = "INSERT INTO requests (type_id, start_date, end_date, duration, employee_id, reason, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, requestsDTO.getType_id());
            stmt.setDate(2, requestsDTO.getStart_date() != null ? Date.valueOf(requestsDTO.getStart_date()) : null);
            stmt.setDate(3, requestsDTO.getEnd_date() != null ? Date.valueOf(requestsDTO.getEnd_date()) : null);
            stmt.setInt(4, requestsDTO.getDuration());
            stmt.setInt(5, requestsDTO.getEmployee_id());
            stmt.setString(6, requestsDTO.getReason());
            stmt.setString(7, requestsDTO.getStatus());
            stmt.setDate(8, requestsDTO.getCreated_at() != null ? Date.valueOf(requestsDTO.getCreated_at()) : null);

            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error inserting request: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(RequestsDTO requestsDTO, Connection conn) {
        String sql = "UPDATE requests SET type_id = ?, start_date = ?, end_date = ?, duration = ?, " +
                     "employee_id = ?, reason = ?, status = ?, approved_by = ?, approved_at = ? " +
                     "WHERE request_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestsDTO.getType_id());
            stmt.setDate(2, requestsDTO.getStart_date() != null ? Date.valueOf(requestsDTO.getStart_date()) : null);
            stmt.setDate(3, requestsDTO.getEnd_date() != null ? Date.valueOf(requestsDTO.getEnd_date()) : null);
            stmt.setInt(4, requestsDTO.getDuration());
            stmt.setInt(5, requestsDTO.getEmployee_id());
            stmt.setString(6, requestsDTO.getReason());
            stmt.setString(7, requestsDTO.getStatus());
            stmt.setObject(8, requestsDTO.getApproved_by());
            stmt.setDate(9, requestsDTO.getApproved_at() != null ? Date.valueOf(requestsDTO.getApproved_at()) : null);
            stmt.setInt(10, requestsDTO.getRequest_id());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error updating request: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer requestId, Connection conn) {
        String sql = "DELETE FROM requests WHERE request_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Error deleting request: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public RequestsDTO findById(Integer requestId, Connection conn) {
        String sql = "SELECT * FROM requests WHERE request_id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToRequest(rs);
            }
        } catch (Exception e) {
            log.error("Error finding request by ID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<RequestsDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM requests";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<RequestsDTO> requestList = new ArrayList<>();
            while (rs.next()) {
                requestList.add(mapResultSetToRequest(rs));
            }
            return requestList;
        } catch (Exception e) {
            log.error("Error finding all requests: {}", e.getMessage());
        }
        return null;
    }

    private RequestsDTO mapResultSetToRequest(ResultSet rs) throws SQLException {
        return RequestsDTO.builder()
                .request_id(rs.getInt("request_id"))
                .type_id(rs.getInt("type_id"))
                .start_date(rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null)
                .end_date(rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null)
                .duration(rs.getInt("duration"))
                .employee_id(rs.getInt("employee_id"))
                .reason(rs.getString("reason"))
                .status(rs.getString("status"))
                .created_at(rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null)
                .approved_by(rs.getObject("approved_by") != null ? rs.getInt("approved_by") : null)
                .approved_at(rs.getDate("approved_at") != null ? rs.getDate("approved_at").toLocalDate() : null)
                .build();
    }
}