package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.ManufacturerDAO;
import com.example.medicinedistribution.DTO.ManufacturerDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ManufacturerDAOImpl implements ManufacturerDAO {
    @Override
    public Integer insert(ManufacturerDTO manufacturerDTO, Connection conn) {
        String sql = "INSERT INTO manufacturer (manufacturerName, description, country, email, phone, address) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, manufacturerDTO.getManufacturerName());
            stmt.setString(2, manufacturerDTO.getDescription());
            stmt.setString(3, manufacturerDTO.getCountry());
            stmt.setString(4, manufacturerDTO.getEmail());
            stmt.setString(5, manufacturerDTO.getPhone());
            stmt.setString(6, manufacturerDTO.getAddress());

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
    public boolean update(ManufacturerDTO manufacturerDTO, Connection conn) {
        String sql = "UPDATE manufacturer " +
                "SET manufacturerName = ?, description = ?, country = ?, email = ?, phone = ?, address = ? " +
                "WHERE manufacturerId = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, manufacturerDTO.getManufacturerName());
            stmt.setString(2, manufacturerDTO.getDescription());
            stmt.setString(3, manufacturerDTO.getCountry());
            stmt.setString(4, manufacturerDTO.getEmail());
            stmt.setString(5, manufacturerDTO.getPhone());
            stmt.setString(6, manufacturerDTO.getAddress());
            stmt.setInt(7, manufacturerDTO.getManufacturerId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM manufacturer WHERE manufacturerId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public ManufacturerDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM manufacturer WHERE manufacturerId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return ManufacturerDTO.builder()
                        .manufacturerId(rs.getInt("manufacturerId"))
                        .manufacturerName(rs.getString("manufacturerName"))
                        .phone(rs.getString("phone"))
                        .address(rs.getString("address"))
                        .email(rs.getString("email"))
                        .country(rs.getString("country"))
                        .description(rs.getString("description"))
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<ManufacturerDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM manufacturer";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            ResultSet rs = stmt.executeQuery();
            List<ManufacturerDTO> manufacturerList = new ArrayList<>();
            while (rs.next()) {
                manufacturerList.add(ManufacturerDTO.builder()
                                .manufacturerId(rs.getInt("manufacturerId"))
                                .manufacturerName(rs.getString("manufacturerName"))
                                .phone(rs.getString("phone"))
                                .address(rs.getString("address"))
                                .email(rs.getString("email"))
                                .country(rs.getString("country"))
                                .description(rs.getString("description"))
                                .build());
            }
            return manufacturerList;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
