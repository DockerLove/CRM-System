package com.example;

import com.example.controller.ClientsController;
import com.example.controller.ContactsController;
import com.example.controller.DealsController;
import com.example.util.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseConnection.testConnection();

        TabPane tabPane = new TabPane();

        // Вкладка "Клиенты"
        Tab clientsTab = new Tab("👥 Клиенты");
        ClientsController clientsController = new ClientsController();
        clientsTab.setContent(clientsController.createClientsPanel());
        clientsTab.setClosable(false);

        // Вкладка "Сделки"
        Tab dealsTab = new Tab("💼 Сделки");
        DealsController dealsController = new DealsController();
        dealsController.setDealsTab(dealsTab);
        dealsTab.setContent(dealsController.createDealsPanel());
        dealsTab.setClosable(false);

        // Вкладка "Контакты"
        Tab contactsTab = new Tab("📞 Контакты");
        ContactsController contactsController = new ContactsController();
        contactsTab.setContent(contactsController.createContactsPanel());
        contactsTab.setClosable(false);

        tabPane.getTabs().addAll(clientsTab, dealsTab, contactsTab);

        Scene scene = new Scene(tabPane, 1000, 700);

        // Подключаем CSS
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        stage.setTitle("CRM Система - Учет клиентов");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}