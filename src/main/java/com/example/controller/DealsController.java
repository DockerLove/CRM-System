package com.example.controller;

import com.example.dao.ClientDAO;
import com.example.dao.DealDAO;
import com.example.enums.DealStage;
import com.example.model.Client;
import com.example.model.Deal;
import com.example.util.DataManager;
import com.example.util.Validator;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DealsController {

    private DealDAO dealDAO = new DealDAO();
    private VBox dealsPanel;
    private Tab dealsTab;

    public VBox createDealsPanel() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.setFillWidth(true);

        Button addDealBtn = new Button("➕ Новая сделка");
        addDealBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        addDealBtn.setMaxWidth(Double.MAX_VALUE);

        HBox kanbanBox = new HBox(20);
        kanbanBox.setFillHeight(true);
        kanbanBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(kanbanBox, Priority.ALWAYS);

        VBox newColumn = createKanbanColumn("🟡 НОВЫЕ", "#95a5a6");
        VBox negotiationColumn = createKanbanColumn("🔵 ПЕРЕГОВОРЫ", "#f39c12");
        VBox closedColumn = createClosedColumn(); // Используем новую колонку

        HBox.setHgrow(newColumn, Priority.ALWAYS);
        HBox.setHgrow(negotiationColumn, Priority.ALWAYS);
        HBox.setHgrow(closedColumn, Priority.ALWAYS);

        newColumn.setMinWidth(200);
        negotiationColumn.setMinWidth(200);
        closedColumn.setMinWidth(250);

        kanbanBox.getChildren().addAll(newColumn, negotiationColumn, closedColumn);

        vbox.setVgrow(kanbanBox, Priority.ALWAYS);

        vbox.getChildren().addAll(addDealBtn, kanbanBox);

        // Загружаем данные
        loadDealsToKanbanWithClosedFilter(newColumn, negotiationColumn, closedColumn);

        addDealBtn.setOnAction(e -> showAddDealDialog(() ->
                loadDealsToKanbanWithClosedFilter(newColumn, negotiationColumn, closedColumn)));

        dealsPanel = vbox;
        return vbox;
    }

    private void loadDealsToKanbanWithClosedFilter(VBox newColumn, VBox negotiationColumn, VBox closedColumn) {
        try {
            // Очищаем колонки NEW и NEGOTIATION
            clearColumnCards(newColumn);
            clearColumnCards(negotiationColumn);

            // Загружаем NEW и NEGOTIATION (все)
            for (Deal deal : dealDAO.getDealsByStage(DealStage.NEW)) {
                addCardToColumn(newColumn, createDealCard(deal));
            }
            for (Deal deal : dealDAO.getDealsByStage(DealStage.NEGOTIATION)) {
                addCardToColumn(negotiationColumn, createDealCard(deal));
            }

            // Загружаем последние 5 закрытых сделок
            loadLastClosedDeals(closedColumn);

        } catch (SQLException e) {
            showAlert("Ошибка загрузки сделок: " + e.getMessage());
        }
    }

    private void clearColumnCards(VBox column) {
        try {
            ScrollPane scrollPane = (ScrollPane) column.getChildren().get(1);
            VBox cardsContainer = (VBox) scrollPane.getContent();
            cardsContainer.getChildren().clear();
        } catch (Exception e) {
            // Если структура не подходит, игнорируем
        }
    }

    private void addCardToColumn(VBox column, VBox card) {
        try {
            ScrollPane scrollPane = (ScrollPane) column.getChildren().get(1);
            VBox cardsContainer = (VBox) scrollPane.getContent();
            cardsContainer.getChildren().add(card);
        } catch (Exception e) {
            // Если структура не подходит, игнорируем
        }
    }

    private VBox createClosedColumn() {
        VBox column = new VBox(10);
        column.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-padding: 10; -fx-background-radius: 10; -fx-border-radius: 10;");
        column.setMaxWidth(Double.MAX_VALUE);
        column.setFillWidth(true);

        Label titleLabel = new Label("🟢 УСПЕШНО ЗАКРЫТЫЕ");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(javafx.geometry.Pos.CENTER);

        // Панель фильтра по дате
        HBox filterBox = new HBox(10);
        filterBox.setStyle("-fx-padding: 5 0 10 0;");
        filterBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label filterLabel = new Label("Фильтр по дате закрытия:");
        filterLabel.setStyle("-fx-font-size: 12px;");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Выберите дату");
        datePicker.setPrefWidth(120);

        Button applyFilterBtn = new Button("🔍 Показать");
        applyFilterBtn.setStyle("-fx-background-color: #4361ee; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 12 5 12;");

        Button resetFilterBtn = new Button("🔄 Сброс");
        resetFilterBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 12 5 12;");

        filterBox.getChildren().addAll(filterLabel, datePicker, applyFilterBtn, resetFilterBtn);

        VBox cardsContainer = new VBox(10);
        cardsContainer.setFillWidth(true);
        cardsContainer.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(400);
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        column.setVgrow(scrollPane, Priority.ALWAYS);
        column.getChildren().addAll(titleLabel, filterBox, scrollPane);
        column.getStyleClass().add("kanban-column");

        // Сохраняем компоненты для доступа
        column.getProperties().put("datePicker", datePicker);
        column.getProperties().put("cardsContainer", cardsContainer);

        // Обработчики кнопок
        applyFilterBtn.setOnAction(e -> filterClosedDealsByDate(column, datePicker.getValue()));
        resetFilterBtn.setOnAction(e -> {
            datePicker.setValue(null);
            loadLastClosedDeals(column);
        });

        return column;
    }

    // Загружает последние 5 успешно закрытых сделок
    private void loadLastClosedDeals(VBox closedColumn) {
        try {
            VBox cardsContainer = (VBox) closedColumn.getProperties().get("cardsContainer");

            if (cardsContainer == null) return;

            cardsContainer.getChildren().clear();

            // Получаем все успешно закрытые сделки
            List<Deal> closedDeals = dealDAO.getDealsByStage(DealStage.CLOSED_SUCCESS);

            // Сортируем по фактической дате закрытия (новые сверху)
            closedDeals.sort((d1, d2) -> {
                if (d1.getClosedDate() == null) return 1;
                if (d2.getClosedDate() == null) return -1;
                return d2.getClosedDate().compareTo(d1.getClosedDate());
            });

            // Берем первые 5
            int limit = Math.min(5, closedDeals.size());
            List<Deal> last5Deals = closedDeals.subList(0, limit);

            // Отображаем
            for (Deal deal : last5Deals) {
                cardsContainer.getChildren().add(createDealCard(deal));
            }

        } catch (SQLException e) {
            showAlert("Ошибка загрузки закрытых сделок: " + e.getMessage());
        }
    }

    // Фильтрует сделки по конкретной дате закрытия
    private void filterClosedDealsByDate(VBox closedColumn, LocalDate selectedDate) {
        if (selectedDate == null) {
            showAlert("Пожалуйста, выберите дату");
            return;
        }

        try {
            VBox cardsContainer = (VBox) closedColumn.getProperties().get("cardsContainer");

            if (cardsContainer == null) return;

            cardsContainer.getChildren().clear();

            // Получаем все успешно закрытые сделки
            List<Deal> closedDeals = dealDAO.getDealsByStage(DealStage.CLOSED_SUCCESS);

            // Фильтруем по фактической дате закрытия
            List<Deal> filteredDeals = new ArrayList<>();
            for (Deal deal : closedDeals) {
                if (deal.getClosedDate() != null && deal.getClosedDate().equals(selectedDate)) {
                    filteredDeals.add(deal);
                }
            }

            // Сортируем по дате (новые сверху)
            filteredDeals.sort((d1, d2) -> d2.getClosedDate().compareTo(d1.getClosedDate()));

            // Отображаем
            if (filteredDeals.isEmpty()) {
                Label emptyLabel = new Label("Нет закрытых сделок за " + selectedDate);
                emptyLabel.setStyle("-fx-text-fill: #6c757d; -fx-padding: 20;");
                cardsContainer.getChildren().add(emptyLabel);
            } else {
                for (Deal deal : filteredDeals) {
                    cardsContainer.getChildren().add(createDealCard(deal));
                }
            }

        } catch (SQLException e) {
            showAlert("Ошибка фильтрации: " + e.getMessage());
        }
    }

    public void setDealsTab(Tab tab) {
        this.dealsTab = tab;
    }

    private VBox createKanbanColumn(String title, String color) {
        VBox column = new VBox(10);
        column.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-padding: 10; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Растягиваем колонку по ширине
        column.setMaxWidth(Double.MAX_VALUE);
        column.setFillWidth(true);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(javafx.geometry.Pos.CENTER);

        VBox cardsContainer = new VBox(10);
        cardsContainer.setFillWidth(true);
        cardsContainer.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(400);
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Заставляем scrollPane растягиваться по ширине внутри колонки
        column.setVgrow(scrollPane, Priority.ALWAYS);

        column.getChildren().addAll(titleLabel, scrollPane);
        column.getStyleClass().add("kanban-column");

        return column;
    }

    private VBox createDealCard(Deal deal) {
        VBox card = new VBox(5);
        card.getStyleClass().add("deal-card");
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e9ecef; -fx-border-radius: 8; -fx-padding: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        Label titleLabel = new Label(deal.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        Label clientLabel = new Label("📌 " + deal.getClientName());
        clientLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        Label amountLabel = new Label("💰 " + (deal.getAmount() != null ? deal.getAmount() + " ₽" : "0 ₽"));
        amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #28a745;");

        Label dateLabel = new Label();
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        if (deal.getStage() == DealStage.CLOSED_SUCCESS) {
            if (deal.getClosedDate() != null) {
                dateLabel.setText("📅 Закрыта: " + deal.getClosedDate().toString());
            } else {
                dateLabel.setText("📅 Закрыта: дата неизвестна");
            }
        } else {
            if (deal.getExpectedCloseDate() != null) {
                dateLabel.setText("📅 Ожидаемая дата: " + deal.getExpectedCloseDate().toString());
            } else {
                dateLabel.setText("📅 Дата не указана");
            }
        }

        HBox buttonsBox = new HBox(5);

        if (deal.getStage() == DealStage.NEW) {
            Button startBtn = new Button("▶ Начать");
            startBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10 5 10;");
            startBtn.setOnAction(e -> changeDealStage(deal, DealStage.NEGOTIATION));
            buttonsBox.getChildren().add(startBtn);

        } else if (deal.getStage() == DealStage.NEGOTIATION) {
            Button closeBtn = new Button("✓ Закрыть");
            closeBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10 5 10;");
            closeBtn.setOnAction(e -> changeDealStage(deal, DealStage.CLOSED_SUCCESS));

            Button rejectBtn = new Button("✗ Отказ");
            rejectBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 5 10 5 10;");
            rejectBtn.setOnAction(e -> changeDealStage(deal, DealStage.CLOSED_REJECT));

            buttonsBox.getChildren().addAll(closeBtn, rejectBtn);
        }

        if (deal.getStage() == DealStage.CLOSED_SUCCESS) {
            Button deleteBtn = new Button("🗑 Удалить");
            deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5 10 5 10;");
            deleteBtn.setOnAction(e -> deleteDeal(deal));
            buttonsBox.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(titleLabel, clientLabel, amountLabel, dateLabel, buttonsBox);
        return card;
    }

    private void changeDealStage(Deal deal, DealStage newStage) {
        try {
            // Если сделка успешно закрывается - устанавливаем реальную дату закрытия
            if (newStage == DealStage.CLOSED_SUCCESS) {
                deal.setClosedDate(LocalDate.now());
                dealDAO.updateDealClosedDate(deal.getId(), LocalDate.now());
            }

            dealDAO.updateDealStage(deal.getId(), newStage);

            String contactStatus = getContactStatusForStage(newStage);
            dealDAO.updateContactsStatusByClientId(deal.getClientId(), contactStatus);

            refreshDealsPanel();

            if (DataManager.getInstance().getContactsController() != null) {
                DataManager.getInstance().getContactsController().refreshClientsList();
                DataManager.getInstance().getContactsController().refreshContactsForClient(deal.getClientId());
            }

        } catch (SQLException e) {
            showAlert("Ошибка: " + e.getMessage());
        }
    }

    private String getContactStatusForStage(DealStage stage) {
        switch (stage) {
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

    private void deleteDeal(Deal deal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить сделку?");
        confirm.setContentText("Удалить \"" + deal.getTitle() + "\"?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    dealDAO.deleteDeal(deal.getId());
                    refreshDealsPanel();
                } catch (SQLException e) {
                    showAlert("Ошибка: " + e.getMessage());
                }
            }
        });
    }

    private void showAddDealDialog(Runnable onSuccess) {
        Dialog<Deal> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.setTitle("Новая сделка");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        ComboBox<Client> clientCombo = new ComboBox<>();
        try {
            clientCombo.getItems().addAll(new ClientDAO().getAllClients());
        } catch (SQLException e) {
            showAlert("Ошибка загрузки клиентов: " + e.getMessage());
        }
        clientCombo.setPromptText("Клиент*");

        TextField titleField = new TextField();
        titleField.setPromptText("Название сделки*");

        TextField amountField = new TextField();
        amountField.setPromptText("Сумма");

        DatePicker expectedDatePicker = new DatePicker();
        expectedDatePicker.setPromptText("Ожидаемая дата");

        Label clientError = new Label();
        clientError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        Label titleError = new Label();
        titleError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        Label amountError = new Label();
        amountError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");

        vbox.getChildren().addAll(
                new Label("Клиент:*"), clientCombo, clientError,
                new Label("Название:*"), titleField, titleError,
                new Label("Сумма:"), amountField, amountError,
                new Label("Ожидаемая дата:"), expectedDatePicker
        );

        dialog.getDialogPane().setContent(vbox);

        ButtonType okButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        clientCombo.valueProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateDealFields(clientCombo, titleField, amountField,
                    clientError, titleError, amountError);
            okButton.setDisable(!isValid);
        });

        titleField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateDealFields(clientCombo, titleField, amountField,
                    clientError, titleError, amountError);
            okButton.setDisable(!isValid);
        });

        amountField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateDealFields(clientCombo, titleField, amountField,
                    clientError, titleError, amountError);
            okButton.setDisable(!isValid);
        });

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                Deal deal = new Deal();
                deal.setClientId(clientCombo.getValue().getId());
                deal.setClientName(clientCombo.getValue().getName());
                deal.setStage(DealStage.NEW);
                deal.setTitle(titleField.getText().trim());
                try {
                    if (!amountField.getText().trim().isEmpty()) {
                        deal.setAmount(new java.math.BigDecimal(amountField.getText().trim()));
                    }
                } catch (NumberFormatException e) {
                    deal.setAmount(java.math.BigDecimal.ZERO);
                }
                deal.setExpectedCloseDate(expectedDatePicker.getValue());
                return deal;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(deal -> {
            try {
                dealDAO.addDeal(deal);
                dealDAO.updateContactsStatusByClientId(deal.getClientId(), "🟡 Новый");

                if (onSuccess != null) onSuccess.run();

                if (DataManager.getInstance().getContactsController() != null) {
                    DataManager.getInstance().getContactsController().refreshClientsList();
                }
            } catch (SQLException e) {
                showAlert("Ошибка: " + e.getMessage());
            }
        });
    }

    private boolean validateDealFields(ComboBox<Client> clientCombo, TextField titleField,
                                       TextField amountField, Label clientError,
                                       Label titleError, Label amountError) {
        boolean isValid = true;

        if (clientCombo.getValue() == null) {
            clientError.setText("Выберите клиента!");
            isValid = false;
        } else {
            clientError.setText("");
        }

        if (titleField.getText() == null || titleField.getText().trim().isEmpty() ||
                titleField.getText().trim().length() < 3) {
            titleError.setText("Мин. 3 символа!");
            isValid = false;
        } else {
            titleError.setText("");
        }

        String amountText = amountField.getText();
        if (amountText != null && !amountText.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(amountText.trim());
                if (amount <= 0) {
                    amountError.setText("Сумма > 0!");
                    isValid = false;
                } else {
                    amountError.setText("");
                }
            } catch (NumberFormatException e) {
                amountError.setText("Введите число!");
                isValid = false;
            }
        } else {
            amountError.setText("");
        }

        return isValid;
    }

    private void refreshDealsPanel() {
        if (dealsTab != null) {
            VBox newPanel = createDealsPanel();
            dealsTab.setContent(newPanel);
            dealsPanel = newPanel;
        }
    }

    private void showAlert(String message) {
        showAlert(message, Alert.AlertType.WARNING);
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}