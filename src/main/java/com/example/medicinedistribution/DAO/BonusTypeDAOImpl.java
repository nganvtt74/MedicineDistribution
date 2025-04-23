// Implementation for BonusTypeDAOImpl
package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.BonusTypeDAO;
import com.example.medicinedistribution.DTO.BonusTypeDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BonusTypeDAOImpl implements BonusTypeDAO {

    @Override
    public Integer insert(BonusTypeDTO bonusType, Connection conn) {
        String sql = "INSERT INTO bonus_type (name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, bonusType.getName());

            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } catch (SQLException e) {
            log.error("Error inserting bonus type: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean update(BonusTypeDTO bonusType, Connection conn) {
        String sql = "UPDATE bonus_type SET name = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bonusType.getName());
            stmt.setInt(2, bonusType.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error updating bonus type: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id, Connection conn) {
        String sql = "DELETE FROM bonus_type WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Error deleting bonus type: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public BonusTypeDTO findById(Integer id, Connection conn) {
        String sql = "SELECT * FROM bonus_type WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                BonusTypeDTO bonusType = new BonusTypeDTO();
                bonusType.setId(rs.getInt("id"));
                bonusType.setName(rs.getString("name"));
                return bonusType;
            }
        } catch (SQLException e) {
            log.error("Error finding bonus type by ID: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<BonusTypeDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM bonus_type";
        List<BonusTypeDTO> bonusTypes = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BonusTypeDTO bonusType = new BonusTypeDTO();
                bonusType.setId(rs.getInt("id"));
                bonusType.setName(rs.getString("name"));
                bonusTypes.add(bonusType);
            }
        } catch (SQLException e) {
            log.error("Error finding all bonus types: {}", e.getMessage());
        }

        return bonusTypes;
    }
}