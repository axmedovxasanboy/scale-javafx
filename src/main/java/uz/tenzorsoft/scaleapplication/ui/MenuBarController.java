package uz.tenzorsoft.scaleapplication.ui;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.service.ConfigUtilsService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;

import static uz.tenzorsoft.scaleapplication.domain.Instances.configurations;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;

@Component
@RequiredArgsConstructor
public class MenuBarController implements BaseController{

    private final ConfigUtilsService configUtilsService;
    private final PrintCheck printCheck;

    @FXML
    private void onDatabaseMenuSelected() {
        showDatabasePopup();
    }

    @FXML
    private void onCameraMenuSelected() {
        showCameraPopup();
    }

    @FXML
    private void onControllerMenuSelected() {
        showControllerPopup();
    }

    @FXML
    private void onShlagbaumMenuSelected() {
        showShlagbaumPopup();
    }

    @FXML
    private void onTaroziMenuSelected() {
        showTaroziPopup();
    }

    @FXML
    private void onPrinterMenuSelected() {
        showPrinterPopup();
    }


    private void showDatabasePopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Ma'lumotlar bazasi");

        // Form Elements
        Label nameLabel = new Label("Ma'lumotlar bazasi nomi:");
        TextField nameField = new TextField(DATABASE_NAME);

        Label loginLabel = new Label("Login:");
        TextField loginField = new TextField(USERNAME);

        Label passwordLabel = new Label("Parol:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(PASSWORD);

        TextField visiblePasswordField = new TextField(PASSWORD);
        visiblePasswordField.setVisible(false);

        ImageView openEye = new ImageView(new Image("/images/opened-eye.png"));
        ImageView closedEye = new ImageView(new Image("/images/closed-eye.png"));
        openEye.setFitWidth(20);
        openEye.setFitHeight(20);
        closedEye.setFitWidth(20);
        closedEye.setFitHeight(20);

        Button eyeButton = new Button("", closedEye);

        eyeButton.setOnAction(event -> {
            boolean isPasswordVisible = visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(!isPasswordVisible);
            passwordField.setVisible(isPasswordVisible);
            eyeButton.setGraphic(isPasswordVisible ? closedEye : openEye);
            if (isPasswordVisible) {
                passwordField.setText(visiblePasswordField.getText());
            } else {
                visiblePasswordField.setText(passwordField.getText());
            }
        });

        HBox passwordBox = new HBox(passwordField, visiblePasswordField, eyeButton);
        passwordBox.setSpacing(5);
        passwordBox.setAlignment(Pos.CENTER_LEFT);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    nameField.getText().equals(DATABASE_NAME) &&
                            loginField.getText().equals(USERNAME) &&
                            passwordField.getText().equals(PASSWORD)
            );
        };

        nameField.textProperty().addListener(changeListener);
        loginField.textProperty().addListener(changeListener);
        passwordField.textProperty().addListener(changeListener);
        visiblePasswordField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            DATABASE_NAME = nameField.getText();
            USERNAME = loginField.getText();
            PASSWORD = passwordField.getText();
            configurations.setDatabaseName(nameField.getText());
            configurations.setUsername(loginField.getText());
            configurations.setPassword(passwordField.getText());
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });
        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(10, nameLabel, nameField, loginLabel, loginField, passwordLabel, passwordBox, buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 350, 300);
        popupStage.setScene(popupScene);
        popupStage.setResizable(false);
        popupStage.show();
    }


    private void showCameraPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Kamera Sozlamalari");

        // Kirish Kamerasi
        Label camera1Label = new Label("1-kamera (Kirish kamerasi) IP manzili:");
        TextField camera1Field = new TextField(CAMERA_1);

        // Yuk Kamerasi
        Label camera2Label = new Label("2-kamera (Yuk kamerasi) IP manzili:");
        TextField camera2Field = new TextField(CAMERA_2);

        // Chiqish Kamerasi
        Label camera3Label = new Label("3-kamera (Chiqish kamerasi) IP manzili:");
        TextField camera3Field = new TextField(CAMERA_3);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    camera1Field.getText().equals(CAMERA_1) &&
                            camera2Field.getText().equals(CAMERA_2) &&
                            camera3Field.getText().equals(CAMERA_3)
            );
        };

        camera1Field.textProperty().addListener(changeListener);
        camera2Field.textProperty().addListener(changeListener);
        camera3Field.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            CAMERA_1 = camera1Field.getText();
            CAMERA_2 = camera2Field.getText();
            CAMERA_3 = camera3Field.getText();
            configurations.setCamera1(camera1Field.getText());
            configurations.setCamera2(camera2Field.getText());
            configurations.setCamera3(camera3Field.getText());
            configUtilsService.saveConfig(configurations);
            // Add save logic for all three cameras here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(10,
                camera1Label, camera1Field,
                camera2Label, camera2Field,
                camera3Label, camera3Field,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 400, 300);
        popupStage.setResizable(false);
        popupStage.setScene(popupScene);
        popupStage.show();
    }


    private void showControllerPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Controller Sozlamalari");

        // Form Elements
        Label controllerAddressLabel = new Label("Controller addresi:");
        TextField controllerAddressField = new TextField(CONTROLLER_IP);

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField(CONTROLLER_PORT.toString());

        Label timeoutLabel = new Label("Time out (ms):");
        TextField timeoutField = new TextField(CONTROLLER_CONNECT_TIMEOUT.toString());

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    controllerAddressField.getText().equals(CONTROLLER_IP) &&
                            portField.getText().equals(CONTROLLER_PORT.toString()) &&
                            timeoutField.getText().equals("" + CONTROLLER_CONNECT_TIMEOUT)
            );
        };


        controllerAddressField.textProperty().addListener(changeListener);
        portField.textProperty().addListener(changeListener);
        timeoutField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            CONTROLLER_IP = controllerAddressField.getText();
            CONTROLLER_PORT = Integer.parseInt(portField.getText());
            CONTROLLER_CONNECT_TIMEOUT = Integer.parseInt(timeoutField.getText());
            configurations.setControllerIp(controllerAddressField.getText());
            configurations.setControllerPort(Integer.parseInt(portField.getText()));
            configurations.setControllerConnectTimeout(Integer.parseInt(timeoutField.getText()));
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(10,
                controllerAddressLabel, controllerAddressField,
                portLabel, portField,
                timeoutLabel, timeoutField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 400, 300);
        popupStage.setScene(popupScene);
        popupStage.show();
    }


    private void showShlagbaumPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Shlagbaum Sozlamalari");

        // Form Elements
        Label firstShlagbaumLabel = new Label("1-shlagbaum yopilish vaqti (ms):");
        TextField firstShlagbaumField = new TextField("" + CLOSE_GATE1_TIMEOUT);

        Label secondShlagbaumLabel = new Label("2-shlagbaum yopilish vaqti (ms):");
        TextField secondShlagbaumField = new TextField("" + CLOSE_GATE2_TIMEOUT);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    firstShlagbaumField.getText().equals("" + CLOSE_GATE1_TIMEOUT) &&
                            secondShlagbaumField.getText().equals("" + CLOSE_GATE2_TIMEOUT)
            );
        };

        firstShlagbaumField.textProperty().addListener(changeListener);
        secondShlagbaumField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            CLOSE_GATE1_TIMEOUT = Integer.parseInt(firstShlagbaumField.getText());
            CLOSE_GATE2_TIMEOUT = Integer.parseInt(secondShlagbaumField.getText());
            configurations.setCloseGate1Timeout(Integer.parseInt(firstShlagbaumField.getText()));
            configurations.setCloseGate2Timeout(Integer.parseInt(secondShlagbaumField.getText()));
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(10,
                firstShlagbaumLabel, firstShlagbaumField,
                secondShlagbaumLabel, secondShlagbaumField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 400, 220);
        popupStage.setScene(popupScene);
        popupStage.setResizable(false);
        popupStage.show();
    }


    private void showTaroziPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Tarozi Sozlamalari");

        // Form Elements
        Label massTimeLabel = new Label("Tarozi massasini aniqlash vaqti (ms):");
        TextField massTimeField = new TextField(SCALE_TIMEOUT.toString());

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField(SCALE_PORT);

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    massTimeField.getText().equals(SCALE_TIMEOUT.toString()) &&
                            portField.getText().equals(SCALE_PORT)
            );
        };

        massTimeField.textProperty().addListener(changeListener);
        portField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
            SCALE_TIMEOUT = Integer.parseInt(massTimeField.getText());
            SCALE_PORT = portField.getText();
            configurations.setScaleTimeout(Integer.parseInt(massTimeField.getText()));
            configurations.setScalePort(portField.getText());
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(10,
                massTimeLabel, massTimeField,
                portLabel, portField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 400, 220);
        popupStage.setScene(popupScene);
        popupStage.show();
    }


    private void showPrinterPopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Printer Sozlamalari");

        // Form Elements
        Label printerNameLabel = new Label("Printer nomi:");
        TextField printerNameField = new TextField(PRINTER_NAME);

        // Buttons
        Button testButton = new Button("Test");
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button when the text field value changes
        printerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(printerNameField.getText().equals(PRINTER_NAME));
        });

        // Test button action
        testButton.setOnAction(event -> {
            printCheck.testCheckRecipient();
        });

        saveButton.setOnAction(event -> {
            PRINTER_NAME = printerNameField.getText();
            configurations.setPrinterName(printerNameField.getText());
            configUtilsService.saveConfig(configurations);
            // Add save logic here
            popupStage.close();
        });

        cancelButton.setOnAction(event -> popupStage.close());

        testButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 15;");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 15;");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox buttonBox = new HBox(testButton, saveButton, cancelButton);
        buttonBox.setSpacing(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Layout
        VBox layout = new VBox(10,
                printerNameLabel, printerNameField,
                buttonBox);
        layout.setSpacing(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #c3c3c3; -fx-border-radius: 5; -fx-background-radius: 5;");

        Scene popupScene = new Scene(layout, 400, 200);
        popupStage.setScene(popupScene);
        popupStage.show();
    }


}
