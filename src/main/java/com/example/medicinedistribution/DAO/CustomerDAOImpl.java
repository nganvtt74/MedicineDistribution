package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.CustomerDAO;
import com.example.medicinedistribution.DTO.CustomerDTO;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public Integer insert(CustomerDTO customerDTO, Connection conn) {
        String sql = "INSERT INTO customer (customerName, phone, email, address) VALUES (?, ?, ?,?)";
        try(PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customerDTO.getCustomerName());
            stmt.setString(2, customerDTO.getPhone());
            stmt.setString(3, customerDTO.getEmail());
            stmt.setString(4, customerDTO.getAddress());
            if (stmt.executeUpdate()>0) {
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
    public boolean update(CustomerDTO customerDTO, Connection conn) {
        String sql = "UPDATE customer SET customerName = ?, phone = ?, email = ?, address = ? WHERE customerId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerDTO.getCustomerName());
            stmt.setString(2, customerDTO.getPhone());
            stmt.setString(3, customerDTO.getEmail());
            stmt.setString(4, customerDTO.getAddress());
            stmt.setInt(5, customerDTO.getCustomerId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean delete(Integer integer, Connection conn) {
        String sql = "DELETE FROM customer WHERE customerId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return false;
    }

    @Override
    public CustomerDTO findById(Integer integer, Connection conn) {
        String sql = "SELECT * FROM customer WHERE customerId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, integer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return CustomerDTO.builder()
                        .customerId(rs.getInt("customerId"))
                        .customerName(rs.getString("customerName"))
                        .phone(rs.getString("phone"))
                        .email(rs.getString("email"))
                        .address(rs.getString("address"))
                        .build();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public List<CustomerDTO> findAll(Connection conn) {
        String sql = "SELECT * FROM customer";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            List<CustomerDTO> customerList = new ArrayList<>();
            while (rs.next()) {
                customerList.add(CustomerDTO.builder()
                        .customerId(rs.getInt("customerId"))
                        .customerName(rs.getString("customerName"))
                        .phone(rs.getString("phone"))
                        .email(rs.getString("email"))
                        .address(rs.getString("address"))
                        .build());
            }
            return customerList;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
