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
import org.springframework.stereotype.Component;

@Component
public class MenuBarController {

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

    private void showDatabasePopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Ma'lumotlar bazasi");

        // Form Elements
        Label nameLabel = new Label("Ma'lumotlar bazasi nomi:");
        TextField nameField = new TextField("DefaultDB");

        Label loginLabel = new Label("Login:");
        TextField loginField = new TextField("admin");

        Label passwordLabel = new Label("Parol:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("password123");

        TextField visiblePasswordField = new TextField("password123");
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
                    nameField.getText().equals("DefaultDB") &&
                            loginField.getText().equals("admin") &&
                            passwordField.getText().equals("password123")
            );
        };

        nameField.textProperty().addListener(changeListener);
        loginField.textProperty().addListener(changeListener);
        passwordField.textProperty().addListener(changeListener);
        visiblePasswordField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
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
        TextField camera1Field = new TextField("192.168.1.101");

        // Yuk Kamerasi
        Label camera2Label = new Label("2-kamera (Yuk kamerasi) IP manzili:");
        TextField camera2Field = new TextField("192.168.1.102");

        // Chiqish Kamerasi
        Label camera3Label = new Label("3-kamera (Chiqish kamerasi) IP manzili:");
        TextField camera3Field = new TextField("192.168.1.103");

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    camera1Field.getText().equals("192.168.1.101") &&
                            camera2Field.getText().equals("192.168.1.102") &&
                            camera3Field.getText().equals("192.168.1.103")
            );
        };

        camera1Field.textProperty().addListener(changeListener);
        camera2Field.textProperty().addListener(changeListener);
        camera3Field.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
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
        TextField controllerAddressField = new TextField("192.168.1.200");

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField("8080");

        Label timeoutLabel = new Label("Time out (ms):");
        TextField timeoutField = new TextField("5000");

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    controllerAddressField.getText().equals("192.168.1.200") &&
                            portField.getText().equals("8080") &&
                            timeoutField.getText().equals("5000")
            );
        };

        controllerAddressField.textProperty().addListener(changeListener);
        portField.textProperty().addListener(changeListener);
        timeoutField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
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
        TextField firstShlagbaumField = new TextField("3000");

        Label secondShlagbaumLabel = new Label("2-shlagbaum yopilish vaqti (ms):");
        TextField secondShlagbaumField = new TextField("3000");

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    firstShlagbaumField.getText().equals("3000") &&
                            secondShlagbaumField.getText().equals("3000")
            );
        };

        firstShlagbaumField.textProperty().addListener(changeListener);
        secondShlagbaumField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
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
        TextField massTimeField = new TextField("5000");

        Label portLabel = new Label("Porti:");
        TextField portField = new TextField("9090");

        // Buttons
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setDisable(true); // Disabled by default

        // Enable Save button on any change in text fields
        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    massTimeField.getText().equals("5000") &&
                            portField.getText().equals("9090")
            );
        };

        massTimeField.textProperty().addListener(changeListener);
        portField.textProperty().addListener(changeListener);

        saveButton.setOnAction(event -> {
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




}
