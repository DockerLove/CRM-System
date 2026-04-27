package com.example.model;

import java.time.LocalDateTime;

public class Contact {
    private int id;
    private int clientId;
    private String clientName;
    private LocalDateTime contactDate;
    private String type;
    private String description;
    private String dealStatus;

    public Contact() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public LocalDateTime getContactDate() { return contactDate; }
    public void setContactDate(LocalDateTime contactDate) { this.contactDate = contactDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDealStatus() { return dealStatus; }
    public void setDealStatus(String dealStatus) { this.dealStatus = dealStatus; }
}