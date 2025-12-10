module com.bitbot.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires static lombok;
    requires java.net.http;
    requires java.prefs;

    opens com.bitbot.client to javafx.fxml;
    opens com.bitbot.client.ui.navigation to javafx.fxml;
    opens com.bitbot.client.ui.dashboard to javafx.fxml;
    opens com.bitbot.client.ui.feed to javafx.fxml;
    opens com.bitbot.client.ui.chart to javafx.fxml;
    opens com.bitbot.client.ui.auth to javafx.fxml;
    opens com.bitbot.client.ui.portfolio to javafx.fxml;
    opens com.bitbot.client.ui.journal to javafx.fxml;
    opens com.bitbot.client.ui.settings to javafx.fxml;
    opens com.bitbot.client.ui.questionnaire to javafx.fxml;
    opens com.bitbot.client.model to com.fasterxml.jackson.databind;
    opens com.bitbot.client.dto to com.fasterxml.jackson.databind;
    
    exports com.bitbot.client;
    exports com.bitbot.client.ui.navigation;
    exports com.bitbot.client.ui.dashboard;
    exports com.bitbot.client.ui.feed;
    exports com.bitbot.client.ui.chart;
    exports com.bitbot.client.ui.auth;
    exports com.bitbot.client.ui.portfolio;
    exports com.bitbot.client.ui.journal;
    exports com.bitbot.client.ui.settings;
    exports com.bitbot.client.ui.components;
    exports com.bitbot.client.ui.questionnaire;
    exports com.bitbot.client.model;
    exports com.bitbot.client.service;
}


