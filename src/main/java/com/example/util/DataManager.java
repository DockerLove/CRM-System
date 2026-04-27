package com.example.util;

import com.example.controller.ClientsController;
import com.example.controller.ContactsController;
import com.example.controller.DealsController;

public class DataManager {

    private static DataManager instance;

    private ClientsController clientsController;
    private DealsController dealsController;
    private ContactsController contactsController;

    private DataManager() {}

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public void setClientsController(ClientsController controller) {
        this.clientsController = controller;
    }

    public void setDealsController(DealsController controller) {
        this.dealsController = controller;
    }

    public void setContactsController(ContactsController controller) {
        this.contactsController = controller;
    }

    public ContactsController getContactsController() {
        return contactsController;
    }
    public void refreshAllData() {
        if (clientsController != null) {
            clientsController.refreshData();
        }
        if (contactsController != null) {
            contactsController.refreshClientsList();
        }
    }
    public void refreshContactsClientsList() {
        if (contactsController != null) {
            contactsController.refreshClientsList();
        }
    }
    public void refreshContactsForClient(int clientId) {
        if (contactsController != null) {
            contactsController.refreshContactsForClient(clientId);
        }
    }
}