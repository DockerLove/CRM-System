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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "com.example")
public class MainApp extends Application {

    private static ConfigurableApplicationContext springContext;
    private static DatabaseConnection databaseConnection;

    @Override
    public void init() {
        // Запускаем Spring контекст
        springContext = SpringApplication.run(MainApp.class);

        // Получаем бин DatabaseConnection из Spring
        databaseConnection = springContext.getBean(DatabaseConnection.class);
    }

    @Override
    public void start(Stage stage) {
        // Тестируем подключение (теперь через Spring бин)
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

    @Override
    public void stop() {
        // Корректно закрываем Spring контекст
        if (springContext != null) {
            springContext.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}