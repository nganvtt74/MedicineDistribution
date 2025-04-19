package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.CategoryDAO;
import com.example.medicinedistribution.DTO.CategoryDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class CategoryDAOImpl implements CategoryDAO {
    @Override
    public Integer insert(CategoryDTO categoryDTO, Connection conn) {
        String sql = "INSERT INTO category (categoryName) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoryDTO.getCategoryName());
            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(CategoryDTO categoryDTO, Connection conn) {
        String sql = "UPDATE category SET categoryName = ? WHERE categoryId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoryDTO.getCategoryName());
            stmt.setInt(2, categoryDTO.getCategoryId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM category WHERE categoryId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public CategoryDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM category WHERE categoryId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return CategoryDTO.builder()
                        .categoryId(rs.getInt("categoryId"))
                        .categoryName(rs.getString("categoryName"))
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<CategoryDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM category";
        List<CategoryDTO> categories = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(CategoryDTO.builder()
                        .categoryId(rs.getInt("categoryId"))
                        .categoryName(rs.getString("categoryName"))
                        .build());
            }
            return categories;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
