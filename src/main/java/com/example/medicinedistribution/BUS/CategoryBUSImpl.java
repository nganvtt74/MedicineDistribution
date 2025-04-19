package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.CategoryBUS;
import com.example.medicinedistribution.DAO.Interface.CategoryDAO;
import com.example.medicinedistribution.DTO.CategoryDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.InsertFailedException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class CategoryBUSImpl implements CategoryBUS {
    private final CategoryDAO categoryDAO;
    private final UserSession userSession;
    private final DataSource dataSource;

    public CategoryBUSImpl(DataSource dataSource, CategoryDAO categoryDAO, UserSession userSession) {
        this.categoryDAO = categoryDAO;
        this.userSession = userSession;
        this.dataSource = dataSource;
    }


    @Override
    public boolean insert(CategoryDTO categoryDTO) {
        if (userSession.hasPermission("INSERT_CATEGORY")) {
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
                log.error(e.getMessage());
                throw new InsertFailedException("Thêm danh mục thất bại");
            }
        }else {
            log.error("User does not have permission to insert category");
            throw new InsertFailedException("Bạn không có quyền thêm danh mục");
        }
    }

    @Override
    public boolean update(CategoryDTO categoryDTO) {
        if (userSession.hasPermission("UPDATE_CATEGORY")) {
            try(Connection connection = dataSource.getConnection()) {
                boolean result = categoryDAO.update(categoryDTO, connection);
                if (result) {
                    log.info("Update category successful {}" , categoryDTO);
                    return true;
                } else {
                    log.error("Update category failed {}" , categoryDTO);
                    throw new InsertFailedException("Cập nhật danh mục thất bại");
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new InsertFailedException("Cập nhật danh mục thất bại");
            }
        }else {
            log.error("User does not have permission to update category");
            throw new InsertFailedException("Bạn không có quyền cập nhật danh mục");
        }
    }

    @Override
    public boolean delete(Integer integer) {
        if (userSession.hasPermission("DELETE_CATEGORY")) {
            try(Connection connection = dataSource.getConnection()) {
                boolean result = categoryDAO.delete(integer, connection);
                if (result) {
                    log.info("Delete category successful {}" , integer);
                    return true;
                } else {
                    log.error("Delete category failed {}" , integer);
                    throw new InsertFailedException("Xóa danh mục thất bại");
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new InsertFailedException("Xóa danh mục thất bại");
            }
        }else {
            log.error("User does not have permission to delete category");
            throw new InsertFailedException("Bạn không có quyền xóa danh mục");
        }
    }

    @Override
    public CategoryDTO findById(Integer integer) {
        if (userSession.hasPermission("VIEW_CATEGORY")) {
            try(Connection connection = dataSource.getConnection()) {
                return categoryDAO.findById(integer, connection);
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new InsertFailedException("Tìm danh mục thất bại");
            }
        }else {
            log.error("User does not have permission to view category");
            throw new InsertFailedException("Bạn không có quyền xem danh mục");
        }
    }

    @Override
    public List<CategoryDTO> findAll() {
        if (userSession.hasPermission("VIEW_CATEGORY")) {
            try(Connection connection = dataSource.getConnection()) {
                return categoryDAO.findAll(connection);
            } catch (SQLException e) {
                log.error(e.getMessage());
                throw new InsertFailedException("Tìm tất cả danh mục thất bại");
            }
        }else {
            log.error("User does not have permission to view all categories");
            throw new InsertFailedException("Bạn không có quyền xem tất cả danh mục");
        }
    }
}
