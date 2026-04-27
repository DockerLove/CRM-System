package com.example.model;

import com.example.enums.DealStage;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Deal {
    private int id;
    private int clientId;
    private String clientName;
    private DealStage stage;
    private String title;
    private BigDecimal amount;
    private LocalDate createdDate;
    private LocalDate expectedCloseDate;
    private LocalDate closedDate; // Фактическая дата закрытия сделки

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public Deal() {}
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public DealStage getStage() {
        return stage;
    }

    public void setStage(DealStage stage) {
        this.stage = stage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(LocalDate expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }

    public String getContactStatus() {
        switch (this.stage) {
            case NEW:
                return "🟡 Новый";
            case NEGOTIATION:
                return "🔵 Переговоры";
            case CLOSED_SUCCESS:
                return "🟢 Успешно закрыта";
            case CLOSED_REJECT:
                return "🔴 Отказ";
            default:
                return "🟡 Новый";
        }
    }

    @Override
    public String toString() {
        return title + " - " + clientName;
    }
}