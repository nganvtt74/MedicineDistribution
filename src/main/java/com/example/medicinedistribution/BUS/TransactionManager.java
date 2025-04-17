package com.example.medicinedistribution.BUS;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection beginTransaction() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);  // Bắt đầu giao dịch
            log.info("Transaction started.");
            return conn;
        } catch (SQLException e) {
            log.error("Failed to start transaction: {}", e.getMessage());
            throw new RuntimeException("Unable to start transaction", e);
        }
    }

    public void commitTransaction(Connection conn) {
        try {
            if (conn != null) {
                conn.commit();  // Commit giao dịch
                log.info("Transaction committed.");
            }
        } catch (SQLException e) {
            log.error("Failed to commit transaction: {}", e.getMessage());
            throw new RuntimeException("Unable to commit transaction", e);
        }
        // Không cần gọi conn.close() ở đây nữa vì try-with-resources sẽ tự động đóng kết nối
    }

    public void rollbackTransaction(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();  // Rollback giao dịch
                log.info("Transaction rolled back.");
            }
        } catch (SQLException e) {
            log.error("Failed to rollback transaction: {}", e.getMessage());
            throw new RuntimeException("Unable to rollback transaction", e);
        }
        // Không cần gọi conn.close() ở đây nữa vì try-with-resources sẽ tự động đóng kết nối
    }
}
