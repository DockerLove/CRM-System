package com.example.util;

import java.util.regex.Pattern;

public class Validator {
    public static boolean isValidClientName(String name) {
        return name != null && !name.trim().isEmpty();
    }
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true;
        }
        String phoneRegex = "^[+0-9\\s\\-()]{5,20}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone.trim()).matches();
    }

    public static boolean isValidDealTitle(String title) {
        return title != null && !title.trim().isEmpty() && title.trim().length() >= 3;
    }
    public static boolean isValidDealAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return true;
        }
        try {
            double amount = Double.parseDouble(amountStr.trim());
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public static boolean isClientSelected(Object client) {
        return client != null;
    }
    public static String getValidationMessage(String fieldName, String errorType) {
        switch (errorType) {
            case "empty":
                return "Заполните поле!";
            case "email":
                return "Неверный email! (например: user@domain.com)";
            case "phone":
                return "Телефон: мин. 5 цифр!";
            case "positive":
                return "Сумма > 0!";
            case "minLength":
                return "Мин. 3 символа!";
            case "client":
                return "Выберите клиента!";
            default:
                return "Ошибка!";
        }
    }
}