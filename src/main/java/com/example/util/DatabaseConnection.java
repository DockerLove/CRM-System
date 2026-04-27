package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/crm_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "50432";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Подключение к PostgreSQL успешно!");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ PostgreSQL Driver not found!");
                throw new SQLException("PostgreSQL Driver not found", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
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