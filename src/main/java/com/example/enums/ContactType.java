package com.example.enums;

public enum ContactType {
    CALL("звонок"),
    MEETING("встреча"),
    EMAIL("email");

    private final String rusName;

    ContactType(String rusName) {
        this.rusName = rusName;
    }

    public String getRusName() {
        return rusName;
    }

    public static ContactType fromRusName(String rusName) {
        for (ContactType type : values()) {
            if (type.rusName.equals(rusName)) {
                return type;
            }
        }
        return CALL;
    }

    @Override
    public String toString() {
        return rusName;
    }
}