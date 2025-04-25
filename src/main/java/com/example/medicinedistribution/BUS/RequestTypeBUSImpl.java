package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.RequestTypeBUS;
import com.example.medicinedistribution.DAO.Interface.RequestTypeDAO;
import com.example.medicinedistribution.DTO.RequestTypeDTO;
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
public class RequestTypeBUSImpl implements RequestTypeBUS {

    private final RequestTypeDAO requestTypeDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final Validator validator;

    public RequestTypeBUSImpl(RequestTypeDAO requestTypeDAO, DataSource dataSource, UserSession userSession, Validator validator) {
        this.requestTypeDAO = requestTypeDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public boolean insert(RequestTypeDTO requestTypeDTO) {
        if (!userSession.hasPermission("INSERT_REQUEST_TYPE")) {
            log.error("User does not have permission to insert request type");
            throw new PermissionDeniedException("You don't have permission to create request types");
        }
        valid(requestTypeDTO);

        try(Connection conn = dataSource.getConnection()) {
            Integer typeId = requestTypeDAO.insert(requestTypeDTO, conn);
            if (typeId > 0) {
                requestTypeDTO.setType_id(typeId);
                log.info("Inserted request type: {}", requestTypeDTO);
                return true;
            } else {
                log.error("Failed to insert request type: {}", requestTypeDTO);
                throw new InsertFailedException("Failed to create request type");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public boolean update(RequestTypeDTO requestTypeDTO) {
        if (!userSession.hasPermission("UPDATE_REQUEST_TYPE")) {
            log.error("User does not have permission to update request type");
            throw new PermissionDeniedException("You don't have permission to update request types");
        }
        valid(requestTypeDTO);
        try(Connection conn = dataSource.getConnection()) {
            boolean result = requestTypeDAO.update(requestTypeDTO, conn);
            if (result) {
                log.info("Updated request type: {}", requestTypeDTO);
                return true;
            } else {
                log.error("Failed to update request type: {}", requestTypeDTO);
                throw new UpdateFailedException("Failed to update request type");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public boolean delete(Integer typeId) {
        if (!userSession.hasPermission("DELETE_REQUEST_TYPE")) {
            log.error("User does not have permission to delete request type");
            throw new PermissionDeniedException("You don't have permission to delete request types");
        }
        try(Connection conn = dataSource.getConnection()) {
            boolean result = requestTypeDAO.delete(typeId, conn);
            if (result) {
                log.info("Deleted request type with ID: {}", typeId);
                return true;
            } else {
                log.error("Failed to delete request type with ID: {}", typeId);
                throw new DeleteFailedException("Cannot delete this request type");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public RequestTypeDTO findById(Integer typeId) {
        if (!userSession.hasPermission("VIEW_REQUEST_TYPE")) {
            log.error("User does not have permission to view request type");
            throw new PermissionDeniedException("You don't have permission to view request types");
        }
        try(Connection conn = dataSource.getConnection()) {
            return requestTypeDAO.findById(typeId, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    @Override
    public List<RequestTypeDTO> findAll() {
        if (!userSession.hasPermission("VIEW_REQUEST_TYPE")) {
            log.error("User does not have permission to view request types");
            throw new PermissionDeniedException("You don't have permission to view request type list");
        }
        try(Connection conn = dataSource.getConnection()) {
            return requestTypeDAO.findAll(conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    private void valid(RequestTypeDTO requestTypeDTO) {
        Set<ConstraintViolation<RequestTypeDTO>> violations = validator.validate(requestTypeDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<RequestTypeDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }
}