package com.example.enums;

public enum DealStage {
    NEW("Новый"),
    NEGOTIATION("Переговоры"),
    CLOSED_SUCCESS("Успешно закрыта"),
    CLOSED_REJECT("Отказ");

    private final String rusName;

    DealStage(String rusName) {
        this.rusName = rusName;
    }

    public String getRusName() {
        return rusName;
    }

    public static DealStage fromRusName(String rusName) {
        for (DealStage dealStage : values()) {
            if (dealStage.rusName.equals(rusName)) {
                return dealStage;
            }
        }
        return NEW;
    }

    @Override
    public String toString() {
        return rusName;
    }
}