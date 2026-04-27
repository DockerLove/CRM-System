package com.example.dao;

import com.example.model.Contact;
import com.example.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    public List<Contact> getAllContacts() throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT ct.*, c.name as client_name FROM contacts ct " +
                "JOIN clients c ON ct.client_id = c.id ORDER BY ct.contact_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                contacts.add(mapResultSetToContact(rs));
            }
        }
        return contacts;
    }

    public List<Contact> getContactsByClientId(int clientId) throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT ct.*, c.name as client_name FROM contacts ct " +
                "JOIN clients c ON ct.client_id = c.id WHERE ct.client_id = ? " +
                "ORDER BY ct.contact_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                contacts.add(mapResultSetToContact(rs));
            }
        }
        return contacts;
    }

    public void addContact(Contact contact) throws SQLException {
        String sql = "INSERT INTO contacts (client_id, contact_date, type, description, deal_status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, contact.getClientId());
            pstmt.setTimestamp(2, Timestamp.valueOf(contact.getContactDate()));
            pstmt.setString(3, contact.getType());
            pstmt.setString(4, contact.getDescription());
            pstmt.setString(5, contact.getDealStatus());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                contact.setId(rs.getInt(1));
            }
        }
    }

    public void deleteContact(int id) throws SQLException {
        String sql = "DELETE FROM contacts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Contact mapResultSetToContact(ResultSet rs) throws SQLException {
        Contact contact = new Contact();
        contact.setId(rs.getInt("id"));
        contact.setClientId(rs.getInt("client_id"));
        contact.setClientName(rs.getString("client_name"));
        contact.setContactDate(rs.getTimestamp("contact_date").toLocalDateTime());
        contact.setType(rs.getString("type"));
        contact.setDescription(rs.getString("description"));
        contact.setDealStatus(rs.getString("deal_status"));
        return contact;
    }
}