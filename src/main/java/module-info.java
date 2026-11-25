module SocialNetwork {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens SocialNetwork to javafx.fxml, javafx.graphics;
    opens SocialNetwork.controller to javafx.fxml;
    opens SocialNetwork.domain to javafx.base, javafx.fxml;

    exports SocialNetwork;
    exports SocialNetwork.main;
    exports SocialNetwork.controller;
    exports SocialNetwork.domain;
    exports SocialNetwork.repository;
    exports SocialNetwork.repository.database;
    exports SocialNetwork.repository.file;
    exports SocialNetwork.repository.memory;
    exports SocialNetwork.repository.util;
    exports SocialNetwork.service;
    exports SocialNetwork.validation;
    exports SocialNetwork.utils.events;
    exports SocialNetwork.utils.observer;
}
