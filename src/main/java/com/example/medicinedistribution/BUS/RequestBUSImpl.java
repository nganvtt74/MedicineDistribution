package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.RequestBUS;
import com.example.medicinedistribution.DAO.Interface.RequestDAO;
import com.example.medicinedistribution.DTO.RequestsDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Slf4j
public class RequestBUSImpl implements RequestBUS {

    private final RequestDAO requestDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;

    public RequestBUSImpl(RequestDAO requestDAO, DataSource dataSource, UserSession userSession, Validator validator) {
        this.requestDAO = requestDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public boolean insert(RequestsDTO requestsDTO) {
        if (!userSession.hasPermission("INSERT_REQUEST")) {
            log.error("User does not have permission to insert request");
            throw new PermissionDeniedException("You don't have permission to create requests");
        }
        valid(requestsDTO);

        try(Connection conn = dataSource.getConnection()) {
            Integer requestId = requestDAO.insert(requestsDTO, conn);
            if (requestId > 0) {
                requestsDTO.setRequest_id(requestId);
                log.info("Inserted request: {}", requestsDTO);
                return true;
            } else {
                log.error("Failed to insert request: {}", requestsDTO);
                throw new InsertFailedException("Failed to create request");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public boolean update(RequestsDTO requestsDTO) {
        if (!userSession.hasPermission("UPDATE_REQUEST")) {
            log.error("User does not have permission to update request");
            throw new PermissionDeniedException("You don't have permission to update requests");
        }
        valid(requestsDTO);
        try(Connection conn = dataSource.getConnection()) {
            boolean result = requestDAO.update(requestsDTO, conn);
            if (result) {
                log.info("Updated request: {}", requestsDTO);
                return true;
            } else {
                log.error("Failed to update request: {}", requestsDTO);
                throw new UpdateFailedException("Failed to update request");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public boolean delete(Integer requestId) {
        if (!userSession.hasPermission("DELETE_REQUEST")) {
            log.error("User does not have permission to delete request");
            throw new PermissionDeniedException("You don't have permission to delete requests");
        }
        try(Connection conn = dataSource.getConnection()) {
            boolean result = requestDAO.delete(requestId, conn);
            if (result) {
                log.info("Deleted request with ID: {}", requestId);
                return true;
            } else {
                log.error("Failed to delete request with ID: {}", requestId);
                throw new DeleteFailedException("Cannot delete this request");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public RequestsDTO findById(Integer requestId) {
        if (!userSession.hasPermission("VIEW_REQUEST")) {
            log.error("User does not have permission to view request");
            throw new PermissionDeniedException("You don't have permission to view requests");
        }
        try(Connection conn = dataSource.getConnection()) {
            return requestDAO.findById(requestId, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public List<RequestsDTO> findAll() {
        if (!userSession.hasPermission("VIEW_REQUEST")) {
            log.error("User does not have permission to view requests");
            throw new PermissionDeniedException("You don't have permission to view request list");
        }
        try(Connection conn = dataSource.getConnection()) {
            return requestDAO.findAll(conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    private void valid(RequestsDTO requestsDTO) {
        Set<ConstraintViolation<RequestsDTO>> violations = validator.validate(requestsDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<RequestsDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}