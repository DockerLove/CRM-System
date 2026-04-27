package com.example.dao;

import com.example.enums.DealStage;
import com.example.model.Deal;
import com.example.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DealDAO {

    public List<Deal> getAllDeals() throws SQLException {
        List<Deal> deals = new ArrayList<>();
        String sql = "SELECT d.*, c.name as client_name FROM deals d " +
                "JOIN clients c ON d.client_id = c.id ORDER BY d.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                deals.add(mapResultSetToDeal(rs));
            }
        }
        return deals;
    }

    public List<Deal> getDealsByStage(DealStage dealStage) throws SQLException {
        List<Deal> deals = new ArrayList<>();
        String sql = "SELECT d.*, c.name as client_name FROM deals d " +
                "JOIN clients c ON d.client_id = c.id WHERE d.stage = ? ORDER BY d.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dealStage.getRusName());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                deals.add(mapResultSetToDeal(rs));
            }
        }
        return deals;
    }

    public List<Deal> getDealsByClientId(int clientId) throws SQLException {
        List<Deal> deals = new ArrayList<>();
        String sql = "SELECT d.*, c.name as client_name FROM deals d " +
                "JOIN clients c ON d.client_id = c.id WHERE d.client_id = ? ORDER BY d.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                deals.add(mapResultSetToDeal(rs));
            }
        }
        return deals;
    }

    public void addDeal(Deal deal) throws SQLException {
        String sql = "INSERT INTO deals (client_id, stage, title, amount, expected_close_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, deal.getClientId());
            pstmt.setString(2, deal.getStage().getRusName());
            pstmt.setString(3, deal.getTitle());
            pstmt.setBigDecimal(4, deal.getAmount());
            pstmt.setDate(5, deal.getExpectedCloseDate() != null ?
                    Date.valueOf(deal.getExpectedCloseDate()) : null);

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                deal.setId(rs.getInt(1));
            }
        }
    }

    public void updateDealStage(int dealId, DealStage newDealStage) throws SQLException {
        String sql = "UPDATE deals SET stage = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newDealStage.getRusName());
            pstmt.setInt(2, dealId);
            pstmt.executeUpdate();
        }
    }

    public void updateDeal(Deal deal) throws SQLException {
        String sql = "UPDATE deals SET client_id = ?, stage = ?, title = ?, amount = ?, expected_close_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, deal.getClientId());
            pstmt.setString(2, deal.getStage().getRusName());
            pstmt.setString(3, deal.getTitle());
            pstmt.setBigDecimal(4, deal.getAmount());
            pstmt.setDate(5, deal.getExpectedCloseDate() != null ?
                    Date.valueOf(deal.getExpectedCloseDate()) : null);
            pstmt.setInt(6, deal.getId());

            pstmt.executeUpdate();
        }
    }

    public void deleteDeal(int id) throws SQLException {
        String sql = "DELETE FROM deals WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public void updateContactsStatusByClientId(int clientId, String dealStatus) throws SQLException {
        String sql = "UPDATE contacts SET deal_status = ? WHERE client_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dealStatus);
            pstmt.setInt(2, clientId);
            pstmt.executeUpdate();
        }
    }

    private Deal mapResultSetToDeal(ResultSet rs) throws SQLException {
        Deal deal = new Deal();
        deal.setId(rs.getInt("id"));
        deal.setClientId(rs.getInt("client_id"));
        deal.setClientName(rs.getString("client_name"));
        deal.setStage(DealStage.fromRusName(rs.getString("stage")));
        deal.setTitle(rs.getString("title"));
        deal.setAmount(rs.getBigDecimal("amount"));
        deal.setCreatedDate(rs.getDate("created_date").toLocalDate());

        Date expectedDate = rs.getDate("expected_close_date");
        if (expectedDate != null) {
            deal.setExpectedCloseDate(expectedDate.toLocalDate());
        }

        // Добавляем фактическую дату закрытия
        Date closedDate = rs.getDate("closed_date");
        if (closedDate != null) {
            deal.setClosedDate(closedDate.toLocalDate());
        }

        return deal;
    }

    public void updateDealClosedDate(int dealId, LocalDate closedDate) throws SQLException {
        String sql = "UPDATE deals SET closed_date = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(closedDate));
            pstmt.setInt(2, dealId);
            pstmt.executeUpdate();
        }
    }
}