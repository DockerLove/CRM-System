package com.example.controller;

import com.example.dao.ClientDAO;
import com.example.model.Client;
import com.example.util.DataManager;
import com.example.util.ExcelExporter;
import com.example.util.Validator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class ClientsController {

    private ClientDAO clientDAO = new ClientDAO();
    private ObservableList<Client> clientList = FXCollections.observableArrayList();
    private TableView<Client> tableView;

    public VBox createClientsPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        HBox searchBox = createSearchBox();
        tableView = createTableView();
        HBox actionBox = createActionButtons();

        vbox.getChildren().addAll(searchBox, tableView, actionBox);
        loadClients();

        DataManager.getInstance().setClientsController(this);

        return vbox;
    }

    public void refreshData() {
        loadClients();
    }

    private HBox createSearchBox() {
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Поиск по имени, телефону, email...");
        searchField.setPrefWidth(400);

        Button searchBtn = new Button("🔍 Найти");
        Button resetBtn = new Button("Сбросить");
        Button addBtn = new Button("➕ Добавить клиента");

        searchBtn.setOnAction(e -> searchClients(searchField.getText()));
        resetBtn.setOnAction(e -> {
            searchField.clear();
            loadClients();
        });
        addBtn.setOnAction(e -> showAddClientDialog());

        searchBox.getChildren().addAll(searchField, searchBtn, resetBtn, addBtn);
        searchBtn.getStyleClass().add("button-secondary");
        resetBtn.getStyleClass().add("button-secondary");
        addBtn.getStyleClass().add("button-success");
        return searchBox;
    }

    private TableView<Client> createTableView() {
        TableView<Client> tableView = new TableView<>();
        tableView.setItems(clientList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Client, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setResizable(false);

        TableColumn<Client, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Client, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);

        TableColumn<Client, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(150);

        TableColumn<Client, String> addressCol = new TableColumn<>("Адрес");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(200);

        TableColumn<Client, String> dateCol = new TableColumn<>("Дата добавления");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        // Убираем prefWidth, чтобы колонка растягивалась
        dateCol.setPrefWidth(120);

        tableView.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, addressCol, dateCol);

        // Растягиваем последнюю колонку (Дата добавления) на всю ширину
        Platform.runLater(() -> {
            double totalWidth = tableView.getWidth();
            double otherColumnsWidth = idCol.getWidth() + nameCol.getWidth() +
                    phoneCol.getWidth() + emailCol.getWidth() +
                    addressCol.getWidth();
            double remainingWidth = totalWidth - otherColumnsWidth - 20; // отступ на скролл
            if (remainingWidth > dateCol.getMinWidth()) {
                dateCol.setPrefWidth(remainingWidth);
            }
        });

        // Слушатель изменения ширины таблицы
        tableView.widthProperty().addListener((obs, oldVal, newVal) -> {
            double totalWidth = newVal.doubleValue();
            double otherColumnsWidth = idCol.getWidth() + nameCol.getWidth() +
                    phoneCol.getWidth() + emailCol.getWidth() +
                    addressCol.getWidth();
            double remainingWidth = totalWidth - otherColumnsWidth - 20;
            if (remainingWidth > dateCol.getMinWidth()) {
                dateCol.setPrefWidth(remainingWidth);
            }
        });

        return tableView;
    }

    private HBox createActionButtons() {
        HBox actionBox = new HBox(10);
        Button editBtn = new Button("Редактировать");
        Button deleteBtn = new Button("Удалить");
        Button exportBtn = new Button("Отчет Excel");

        editBtn.setOnAction(e -> {
            Client selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditClientDialog(selected);
            } else {
                showAlert("Выберите клиента для редактирования");
            }
        });

        deleteBtn.setOnAction(e -> {
            Client selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteClient(selected);
            } else {
                showAlert("Выберите клиента для удаления");
            }
        });

        exportBtn.setOnAction(e -> exportToExcel());

        actionBox.getChildren().addAll(editBtn, deleteBtn, exportBtn);
        editBtn.getStyleClass().add("button-warning");
        deleteBtn.getStyleClass().add("button-danger");
        exportBtn.getStyleClass().add("button-success");

        return actionBox;
    }

    private void exportToExcel() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Сохранить отчет Excel");
        fileChooser.setInitialFileName(ExcelExporter.getDefaultFileName());
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Excel файлы", "*.xlsx")
        );

        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            // Экспортируем все данные (не только отфильтрованные)
            try {
                List<Client> allClients = clientDAO.getAllClients();
                boolean success = ExcelExporter.exportClientsToExcel(allClients, file.getAbsolutePath());

                if (success) {
                    showAlert("Отчет успешно сохранен!\n" + file.getAbsolutePath(),
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Ошибка при сохранении отчета!");
                }
            } catch (SQLException e) {
                showAlert("Ошибка загрузки данных: " + e.getMessage());
            }
        }
    }

    private void loadClients() {
        try {
            clientList.clear();
            clientList.addAll(clientDAO.getAllClients());
        } catch (Exception e) {
            showAlert("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void searchClients(String text) {
        if (text == null || text.trim().isEmpty()) {
            loadClients();
            return;
        }
        try {
            clientList.clear();
            clientList.addAll(clientDAO.searchClients(text));
        } catch (Exception e) {
            showAlert("Ошибка поиска: " + e.getMessage());
        }
    }

    private void showAddClientDialog() {
        Dialog<Client> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("dialog-pane");
        dialog.setTitle("Добавить клиента");
        dialog.setHeaderText("Введите данные нового клиента");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        TextField nameField = new TextField();
        nameField.setPromptText("Название/ФИО*");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Телефон");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField addressField = new TextField();
        addressField.setPromptText("Адрес");

        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        Label phoneError = new Label();
        phoneError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        Label emailError = new Label();
        emailError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");

        vbox.getChildren().addAll(
                new Label("Название:*"), nameField, nameError,
                new Label("Телефон:"), phoneField, phoneError,
                new Label("Email:"), emailField, emailError,
                new Label("Адрес:"), addressField
        );

        dialog.getDialogPane().setContent(vbox);

        ButtonType okButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        nameField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateClientFields(nameField, phoneField, emailField,
                    nameError, phoneError, emailError);
            okButton.setDisable(!isValid);
        });

        phoneField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateClientFields(nameField, phoneField, emailField,
                    nameError, phoneError, emailError);
            okButton.setDisable(!isValid);
        });

        emailField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateClientFields(nameField, phoneField, emailField,
                    nameError, phoneError, emailError);
            okButton.setDisable(!isValid);
        });

        dialog.setResultConverter(button -> {
            if (button == okButtonType) {
                Client client = new Client();
                client.setName(nameField.getText().trim());
                client.setPhone(phoneField.getText().trim());
                client.setEmail(emailField.getText().trim());
                client.setAddress(addressField.getText().trim());
                return client;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(client -> {
            try {
                clientDAO.addClient(client);
                loadClients();
                DataManager.getInstance().refreshAllData();
            } catch (Exception e) {
                showAlert("Ошибка: " + e.getMessage());
            }
        });
    }

    private void showEditClientDialog(Client client) {
        Dialog<Client> dialog = new Dialog<>();
        dialog.setTitle("Редактировать клиента");
        dialog.setHeaderText("Редактирование: " + client.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        TextField nameField = new TextField(client.getName());
        TextField phoneField = new TextField(client.getPhone());
        TextField emailField = new TextField(client.getEmail());
        TextField addressField = new TextField(client.getAddress());

        Label nameError = new Label();
        nameError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        Label phoneError = new Label();
        phoneError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        Label emailError = new Label();
        emailError.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");

        vbox.getChildren().addAll(
                new Label("Название:*"), nameField, nameError,
                new Label("Телефон:"), phoneField, phoneError,
                new Label("Email:"), emailField, emailError,
                new Label("Адрес:"), addressField
        );

        dialog.getDialogPane().setContent(vbox);

        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        nameField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateClientFields(nameField, phoneField, emailField,
                    nameError, phoneError, emailError);
            saveButton.setDisable(!isValid);
        });

        phoneField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateClientFields(nameField, phoneField, emailField,
                    nameError, phoneError, emailError);
            saveButton.setDisable(!isValid);
        });

        emailField.textProperty().addListener((obs, old, newVal) -> {
            boolean isValid = validateClientFields(nameField, phoneField, emailField,
                    nameError, phoneError, emailError);
            saveButton.setDisable(!isValid);
        });

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                client.setName(nameField.getText().trim());
                client.setPhone(phoneField.getText().trim());
                client.setEmail(emailField.getText().trim());
                client.setAddress(addressField.getText().trim());
                return client;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedClient -> {
            try {
                clientDAO.updateClient(updatedClient);
                loadClients();
                DataManager.getInstance().refreshAllData();
            } catch (Exception e) {
                showAlert("Ошибка: " + e.getMessage());
            }
        });
    }

    private boolean validateClientFields(TextField nameField, TextField phoneField,
                                         TextField emailField, Label nameError,
                                         Label phoneError, Label emailError) {
        boolean isValid = true;

        if (!Validator.isValidClientName(nameField.getText())) {
            nameError.setText(Validator.getValidationMessage("Название", "empty"));
            isValid = false;
        } else {
            nameError.setText("");
        }

        if (!Validator.isValidPhone(phoneField.getText())) {
            phoneError.setText(Validator.getValidationMessage("Телефон", "phone"));
            isValid = false;
        } else {
            phoneError.setText("");
        }

        if (!Validator.isValidEmail(emailField.getText())) {
            emailError.setText(Validator.getValidationMessage("Email", "email"));
            isValid = false;
        } else {
            emailError.setText("");
        }

        return isValid;
    }

    private void deleteClient(Client client) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Вы уверены, что хотите удалить " + client.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    clientDAO.deleteClient(client.getId());
                    loadClients();
                    DataManager.getInstance().refreshAllData();
                } catch (Exception e) {
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