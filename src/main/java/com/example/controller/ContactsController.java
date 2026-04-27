package com.example.controller;

import com.example.dao.ClientDAO;
import com.example.dao.ContactDAO;
import com.example.dao.DealDAO;
import com.example.enums.DealStage;
import com.example.model.Client;
import com.example.model.Contact;
import com.example.model.Deal;
import com.example.util.DataManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ContactsController {

    private ContactDAO contactDAO = new ContactDAO();
    private ComboBox<Client> clientCombo;
    private TableView<Contact> currentTableView;
    private DealDAO dealDAO = new DealDAO();

    public VBox createContactsPanel() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        HBox clientSelectBox = createClientSelectBox();
        currentTableView = createContactsTable();

        Button deleteBtn = new Button("🗑 Удалить");
        deleteBtn.getStyleClass().add("button-delete-contact");
        deleteBtn.setDisable(true);

        vbox.getChildren().addAll(clientSelectBox, currentTableView, deleteBtn);

        setupEventHandlers(clientSelectBox, currentTableView, deleteBtn);
        DataManager.getInstance().setContactsController(this);

        return vbox;
    }

    public void refreshClientsList() {
        try {
            ClientDAO clientDAO = new ClientDAO();
            Client selected = clientCombo.getValue();
            clientCombo.getItems().clear();
            clientCombo.getItems().addAll(clientDAO.getAllClients());

            if (selected != null) {
                for (Client client : clientCombo.getItems()) {
                    if (client.getId() == selected.getId()) {
                        clientCombo.setValue(client);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Ошибка обновления списка клиентов: " + e.getMessage());
        }
    }

    public void refreshContactsForClient(int clientId) {
        if (currentTableView != null && clientCombo != null &&
                clientCombo.getValue() != null && clientCombo.getValue().getId() == clientId) {
            loadContactsForClient(clientId, currentTableView);
        }
    }

    private HBox createClientSelectBox() {
        HBox clientSelectBox = new HBox(10);
        Label selectLabel = new Label("Клиент:");
        clientCombo = new ComboBox<>();
        clientCombo.setPromptText("Выберите клиента");
        clientCombo.setPrefWidth(250);

        try {
            ClientDAO clientDAO = new ClientDAO();
            clientCombo.getItems().addAll(clientDAO.getAllClients());
        } catch (SQLException e) {
            showAlert("Ошибка загрузки клиентов: " + e.getMessage());
        }

        Button addContactBtn = new Button("➕ Добавить контакт");
        addContactBtn.getStyleClass().add("button-add-contact");
        addContactBtn.setDisable(true);

        clientSelectBox.getChildren().addAll(selectLabel, clientCombo, addContactBtn);

        return clientSelectBox;
    }

    private TableView<Contact> createContactsTable() {
        TableView<Contact> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Contact, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Contact, LocalDateTime> dateCol = new TableColumn<>("Дата и время");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("contactDate"));
        dateCol.setPrefWidth(150);
        dateCol.setCellFactory(column -> new TableCell<Contact, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString().replace("T", " "));
                }
            }
        });

        TableColumn<Contact, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);

        TableColumn<Contact, String> statusCol = new TableColumn<>("Статус сделки");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("dealStatus"));
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(column -> new TableCell<Contact, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("🟢")) {
                        setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                    } else if (item.contains("🔴")) {
                        setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    } else if (item.contains("🔵")) {
                        setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ffc107; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<Contact, String> descriptionCol = new TableColumn<>("Описание");
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        tableView.getColumns().addAll(idCol, dateCol, typeCol, statusCol, descriptionCol);

        descriptionCol.prefWidthProperty().bind(tableView.widthProperty()
                .subtract(idCol.widthProperty())
                .subtract(dateCol.widthProperty())
                .subtract(typeCol.widthProperty())
                .subtract(statusCol.widthProperty())
                .subtract(20));

        tableView.setPlaceholder(new Label("Выберите клиента из списка"));

        return tableView;
    }

    private void setupEventHandlers(HBox clientSelectBox, TableView<Contact> tableView, Button deleteBtn) {
        ComboBox<Client> combo = (ComboBox<Client>) clientSelectBox.getChildren().get(1);
        Button addContactBtn = (Button) clientSelectBox.getChildren().get(2);

        combo.setOnAction(e -> {
            Client selected = combo.getValue();
            if (selected != null) {
                loadContactsForClient(selected.getId(), tableView);
                addContactBtn.setDisable(false);
            } else {
                addContactBtn.setDisable(true);
            }
        });

        addContactBtn.setOnAction(e -> {
            Client selected = combo.getValue();
            if (selected != null) {
                showAddContactDialog(selected, () -> {
                    loadContactsForClient(selected.getId(), tableView);
                });
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            deleteBtn.setDisable(selected == null);
        });

        deleteBtn.setOnAction(e -> {
            Contact selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteContact(selected, () -> {
                    Client selectedClient = combo.getValue();
                    if (selectedClient != null) {
                        loadContactsForClient(selectedClient.getId(), tableView);
                    }
                });
            }
        });
    }

    private void loadContactsForClient(int clientId, TableView<Contact> tableView) {
        try {
            ObservableList<Contact> contacts = FXCollections.observableArrayList();
            contacts.addAll(contactDAO.getContactsByClientId(clientId));
            tableView.setItems(contacts);

            if (contacts.isEmpty()) {
                showAlert("Нет контактов для этого клиента", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException e) {
            showAlert("Ошибка загрузки контактов: " + e.getMessage());
        }
    }

    private void showAddContactDialog(Client client, Runnable onSuccess) {
        Dialog<Contact> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.setTitle("Добавить контакт");
        dialog.setHeaderText("Клиент: " + client.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("звонок", "встреча", "email");
        typeCombo.setValue("звонок");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField timeField = new TextField();
        timeField.setPromptText("Время (HH:MM)");
        timeField.setText(LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        HBox dateTimeBox = new HBox(10);
        dateTimeBox.getChildren().addAll(datePicker, timeField);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Описание контакта...");
        descriptionArea.setPrefRowCount(5);
        descriptionArea.setPrefWidth(400);

        vbox.getChildren().addAll(
                new Label("Тип контакта:"), typeCombo,
                new Label("Дата и время:"), dateTimeBox,
                new Label("Описание:"), descriptionArea
        );

        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK && typeCombo.getValue() != null) {
                Contact contact = new Contact();
                contact.setClientId(client.getId());
                contact.setClientName(client.getName());
                contact.setType(typeCombo.getValue());
                contact.setDescription(descriptionArea.getText());

                String dealStatus = getDealStatusForClient(client.getId());
                contact.setDealStatus(dealStatus);

                LocalDate date = datePicker.getValue();
                String timeStr = timeField.getText();
                LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(timeStr));
                contact.setContactDate(dateTime);

                return contact;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(contact -> {
            try {
                contactDAO.addContact(contact);
                if (onSuccess != null) onSuccess.run();
            } catch (SQLException e) {
                showAlert("Ошибка: " + e.getMessage());
            }
        });
    }

    private String getDealStatusForClient(int clientId) {
        try {
            List<Deal> deals = dealDAO.getDealsByClientId(clientId);
            if (deals.isEmpty()) {
                return "🟡 Новый";
            }

            Deal latestDeal = deals.get(deals.size() - 1);
            return getContactStatusForStage(latestDeal.getStage());
        } catch (SQLException e) {
            return "🟡 Новый";
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

    private void deleteContact(Contact contact, Runnable onSuccess) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удалить контакт?");
        confirm.setContentText("Вы уверены, что хотите удалить этот контакт?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    contactDAO.deleteContact(contact.getId());
                    if (onSuccess != null) onSuccess.run();
                } catch (SQLException e) {
                    showAlert("Ошибка: " + e.getMessage());
                }
            }
        });
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