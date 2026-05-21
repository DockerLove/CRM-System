package com.example.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private static String url;
    private static String username;
    private static String password;
    private static Connection connection = null;

    @Value("${spring.datasource.url}")
    public void setUrl(String url) {
        DatabaseConnection.url = url;
    }

    @Value("${spring.datasource.username}")
    public void setUsername(String username) {
        DatabaseConnection.username = username;
    }

    @Value("${spring.datasource.password}")
    public void setPassword(String password) {
        DatabaseConnection.password = password;
    }

    @PostConstruct
    public void init() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL Driver загружен");
            System.out.println("📁 URL: " + url);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL Driver not found!");
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Подключение к PostgreSQL успешно!");
        }
        return connection;
    }

    @PreDestroy
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("🔌 Соединение с БД закрыто");
            } catch (SQLException e) {
                System.err.println("Ошибка при закрытии: " + e.getMessage());
            }
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✅ База данных: " + conn.getCatalog());
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Ошибка подключения: " + e.getMessage());
            return false;
        }
    }
}