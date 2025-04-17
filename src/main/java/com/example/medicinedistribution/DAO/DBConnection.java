package com.example.medicinedistribution.DAO;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DBConnection {
    private static DataSource dataSource;

    private DBConnection() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            try {
                Dotenv dotenv = Dotenv.load();
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:mariadb://%s:%s/%s".formatted(
                        dotenv.get("DB_HOST"),
                        dotenv.get("DB_PORT"),
                        dotenv.get("DB_NAME")
                ));
                config.setUsername(dotenv.get("DB_USER"));
                config.setPassword(dotenv.get("DB_PASSWORD"));
                config.setDriverClassName("org.mariadb.jdbc.Driver"); // Or your MySQL driver class name
                config.setMaximumPoolSize(10); // Adjust pool size as needed
                config.setMinimumIdle(5);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                dataSource = new HikariDataSource(config);
            } catch (Exception e) {
                log.error("Error creating Hikari DataSource for MariaDB: {}", e.getMessage());
                throw new RuntimeException("Failed to initialize DataSource.");
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
}