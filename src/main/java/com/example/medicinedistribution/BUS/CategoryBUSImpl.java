package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.CategoryBUS;
import com.example.medicinedistribution.DAO.Interface.CategoryDAO;
import com.example.medicinedistribution.DTO.CategoryDTO;
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
public class CategoryBUSImpl implements CategoryBUS {
    private final CategoryDAO categoryDAO;
    private final UserSession userSession;
    private final DataSource dataSource;
    private final Validator validator ;

    public CategoryBUSImpl(DataSource dataSource, CategoryDAO categoryDAO, UserSession userSession , Validator validator) {
        this.categoryDAO = categoryDAO;
        this.userSession = userSession;
        this.dataSource = dataSource;
        this.validator = validator;
    }


    @Override
    public boolean insert(CategoryDTO categoryDTO) {
        if (userSession.hasPermission("INSERT_CATEGORY")) {
            valid(categoryDTO);
            try(Connection connection = dataSource.getConnection()) {
                Integer result = categoryDAO.insert(categoryDTO, connection);
                if (result > 0) {
                    categoryDTO.setCategoryId(result);
                    log.info("Insert category successful {}" , categoryDTO);
                    return true;
                } else {
                    log.error("Insert category failed {}" , categoryDTO);
                    throw new InsertFailedException("Thêm danh mục thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("User does not have permission to insert category");
            throw new PermissionDeniedException("Bạn không có quyền thêm danh mục");
        }
    }

    @Override
    public boolean update(CategoryDTO categoryDTO) {
        if (userSession.hasPermission("UPDATE_CATEGORY")) {

            valid(categoryDTO);
            try(Connection connection = dataSource.getConnection()) {
                boolean result = categoryDAO.update(categoryDTO, connection);
                if (result) {
                    log.info("Update category successful {}" , categoryDTO);
                    return true;
                } else {
                    log.error("Update category failed {}" , categoryDTO);
                    throw new UpdateFailedException("Cập nhật danh mục thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("User does not have permission to update category");
            throw new PermissionDeniedException("Bạn không có quyền cập nhật danh mục");
        }
    }

    private void valid(CategoryDTO categoryDTO) {
        Set<ConstraintViolation<CategoryDTO>> violations = validator.validate(categoryDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<CategoryDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (userSession.hasPermission("DELETE_CATEGORY")) {

            if(integer == null || integer <= 0){
                log.error("Category ID is null or invalid");
                throw new IllegalArgumentException("ID danh mục không được null hoặc không hợp lệ");
            }

            try(Connection connection = dataSource.getConnection()) {
                boolean result = categoryDAO.delete(integer, connection);
                if (result) {
                    log.info("Delete category successful {}" , integer);
                    return true;
                } else {
                    log.error("Delete category failed {}" , integer);
                    throw new DeleteFailedException("Xóa danh mục thất bại");
                }
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }else {
            log.error("User does not have permission to delete category");
            throw new PermissionDeniedException("Bạn không có quyền xóa danh mục");
        }
    }

    @Override
    public CategoryDTO findById(Integer integer) {
            if(integer == null || integer <= 0){
                log.error("Category ID is null or invalid");
                throw new IllegalArgumentException("ID danh mục không được null hoặc không hợp lệ");
            }
            try(Connection connection = dataSource.getConnection()) {
                return categoryDAO.findById(integer, connection);
            } catch (SQLException e) {
                log.error("Error while getting connection", e);
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
    }

    @Override
    public List<CategoryDTO> findAll() {
        try(Connection connection = dataSource.getConnection()) {
            return categoryDAO.findAll(connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }
}
