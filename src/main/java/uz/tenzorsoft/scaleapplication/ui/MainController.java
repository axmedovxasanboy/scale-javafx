package uz.tenzorsoft.scaleapplication.ui;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.service.ControllerService;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static uz.tenzorsoft.scaleapplication.domain.Instances.currentUser;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Component
@RequiredArgsConstructor
public class MainController {
    private final FXMLLoader fxmlLoader;
    private final ExecutorService executors;
    private final ControllerService controllerService;
    private final TruckService truckService;

    @FXML
    private Pane scaleAutomationPane;

    @FXML
    private Tab cameraTab;

    @FXML
    private Tab userTab;

    @FXML
    private TextField username;

    @FXML
    private TextField phoneNumber;

    @FXML
    private Button button1;

    @FXML
    private Button button2;

    @FXML
    private Button button3;

    @FXML
    private Button button4;

    @FXML
    private Button button5;

    @FXML
    private ImageView controller;

    @FXML
    private ImageView camera1;

    @FXML
    private ImageView camera2;

    @FXML
    private ImageView camera3;

    @FXML
    private ImageView gate1;

    @FXML
    private ImageView gate2;

    @FXML
    private ImageView sensor1;

    @FXML
    private ImageView sensor2;

    @FXML
    private ImageView sensor3;

    @FXML
    private TableView<TableViewData> tableData;

    @FXML
    private TableColumn<TableViewData, Long> id;

    @FXML
    private TableColumn<TableViewData, String> enteredTruckNumber;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> enteredDate;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> enteredTime;

    @FXML
    private TableColumn<TableViewData, Double> enteredWeight;

    @FXML
    private TableColumn<TableViewData, String> enteredOnDuty;

    @FXML
    private TableColumn<TableViewData, String> exitedTruckNumber;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> exitedDate;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> exitedTime;

    @FXML
    private TableColumn<TableViewData, Double> exitedWeight;

    @FXML
    private TableColumn<TableViewData, String> exitedOnDuty;

    @FXML
    private Text changePasswordText;

    @FXML
    private Button logoutButton;


    @FXML
    public void initialize() {
        setupButtonPressEffect(button1, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button2, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button3, "#ebed72", "#e3e360");
        setupButtonPressEffect(button4, "#f56e6e", "#e46464");
        setupButtonPressEffect(button5, "#f56e6e", "#e46464");


        id.setCellValueFactory(new PropertyValueFactory<>("Id"));
        enteredTruckNumber.setCellValueFactory(new PropertyValueFactory<>("EnteredTruckNumber"));
        enteredDate.setCellValueFactory(new PropertyValueFactory<>("EnteredDate"));
        enteredTime.setCellValueFactory(new PropertyValueFactory<>("EnteredTime"));
        enteredWeight.setCellValueFactory(new PropertyValueFactory<>("EnteredWeight"));
        enteredOnDuty.setCellValueFactory(new PropertyValueFactory<>("EnteredOnDuty"));

        exitedTruckNumber.setCellValueFactory(new PropertyValueFactory<>("ExitedTruckNumber"));
        exitedDate.setCellValueFactory(new PropertyValueFactory<>("ExitedDate"));
        exitedTime.setCellValueFactory(new PropertyValueFactory<>("ExitedTime"));
        exitedWeight.setCellValueFactory(new PropertyValueFactory<>("ExitedWeight"));
        exitedOnDuty.setCellValueFactory(new PropertyValueFactory<>("ExitedOnDuty"));


        // Handle click on "Parolni almashtirish"
        changePasswordText.setOnMouseClicked(event -> openPasswordChangeDialog());
    }

    private void openPasswordChangeDialog() {
        // Create a new dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setTitle("Parolni almashtirish");

        // Step 1: Phone Number Input Field
        Label phoneLabel = new Label("Telefon nomerini kiriting:");
        TextField phoneInput = new TextField();
        phoneInput.setPromptText("Telefon nomer");
        phoneInput.setPrefWidth(200);  // Adjusted width
        phoneInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Button sendSmsButton = new Button("SMS Yuborish");
        setupButtonPressEffect(sendSmsButton, "#578fea", "#426bb2");

        // Step 2: SMS Code Input Field (initially hidden)
        Label smsLabel = new Label("SMS kodini kiriting:");
        smsLabel.setVisible(false);
        TextField smsInput = new TextField();
        smsInput.setPromptText("SMS kod");
        smsInput.setVisible(false);
        smsInput.setPrefWidth(200);  // Adjusted width
        smsInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Button confirmSmsButton = new Button("Tasdiqlash");
        confirmSmsButton.setVisible(false);
        setupButtonPressEffect(confirmSmsButton, "#578fea", "#426bb2");

        // Step 3: New Password Fields (initially hidden)
        Label newPasswordLabel = new Label("Yangi parol:");
        newPasswordLabel.setVisible(false);
        PasswordField newPasswordInput = new PasswordField();
        newPasswordInput.setPromptText("Yangi parol");
        newPasswordInput.setVisible(false);
        newPasswordInput.setPrefWidth(200);  // Adjusted width
        newPasswordInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Label confirmPasswordLabel = new Label("Parolni qayta kiriting:");
        confirmPasswordLabel.setVisible(false);
        PasswordField confirmPasswordInput = new PasswordField();
        confirmPasswordInput.setPromptText("Parolni qayta kiriting");
        confirmPasswordInput.setVisible(false);
        confirmPasswordInput.setPrefWidth(200);  // Adjusted width
        confirmPasswordInput.setMaxWidth(250);   // Set max width to prevent it from getting too wide

        Button confirmPasswordButton = new Button("Tasdiqlash");
        confirmPasswordButton.setVisible(false);
        setupButtonPressEffect(confirmPasswordButton, "#578fea", "#426bb2");

        // Layout for dialog components
        VBox dialogVbox = new VBox(10);
        dialogVbox.setAlignment(Pos.CENTER);  // Center all elements
        dialogVbox.setPadding(new Insets(20)); // Add padding to prevent elements from sticking to the edges
        dialogVbox.getChildren().addAll(phoneLabel, phoneInput, sendSmsButton, smsLabel, smsInput, confirmSmsButton, newPasswordLabel, newPasswordInput, confirmPasswordLabel, confirmPasswordInput, confirmPasswordButton);

        Scene dialogScene = new Scene(dialogVbox, 300, 400);
        dialog.setScene(dialogScene);

        // Step 1: Send SMS Button Action
        sendSmsButton.setOnAction(e -> {
            if (phoneInput.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Telefon nomeri talab qilinadi.", ButtonType.OK).showAndWait();
                return;
            }

            // Simulate sending SMS with the default code "11111"
            String smsCode = "11111"; // Temporary code
            saveSmsCodeToDatabase(smsCode);

            smsLabel.setVisible(true);
            smsInput.setVisible(true);
            confirmSmsButton.setVisible(true);
        });

        // Step 2: Confirm SMS Button Action
        confirmSmsButton.setOnAction(e -> {
            String smsCode = smsInput.getText();

            // Check SMS code (default code "11111")
            if ("11111".equals(smsCode)) {  // Temporary code for testing
                newPasswordLabel.setVisible(true);
                newPasswordInput.setVisible(true);
                confirmPasswordLabel.setVisible(true);
                confirmPasswordInput.setVisible(true);
                confirmPasswordButton.setVisible(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "SMS kod noto‘g‘ri, qayta urinib ko‘ring.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // Step 3: Confirm Password Button Action
        confirmPasswordButton.setOnAction(e -> {
            String newPassword = newPasswordInput.getText();
            String confirmPassword = confirmPasswordInput.getText();

            // Validate that password fields are not empty
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Parol maydoni bo'sh bo'lmasligi kerak.", ButtonType.OK).showAndWait();
                return;
            }

            if (newPassword.equals(confirmPassword)) {
                // Update password in the backend (store it in DB)
                System.out.println("Password successfully changed!");
                dialog.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Parol bir-biriga to'g'ri kelmadi.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // Show the dialog
        dialog.showAndWait();
    }

    private void saveSmsCodeToDatabase(String smsCode) {
        // Temporary method to simulate storing the SMS code in the database
        System.out.println("SMS code stored in DB: " + smsCode);
    }

    // Sets the button press effect for color changes
    private void setupButtonPressEffect(Button button, String normalColor, String pressedColor) {
        // Set the cursor to hand for the button
        button.setStyle("-fx-background-color: " + normalColor + "; -fx-text-fill: white; -fx-border-radius: 7; -fx-background-radius: 10; -fx-cursor: hand;");

        // Handle mouse pressed and released events for button color change
        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: " + pressedColor + "; -fx-text-fill: white; -fx-border-radius: 7; -fx-background-radius: 10; -fx-cursor: hand;"));

        button.setOnMouseReleased(event -> button.setStyle("-fx-background-color: " + normalColor + "; -fx-text-fill: white; -fx-border-radius: 7; -fx-background-radius: 10; -fx-cursor: hand;"));
    }



    @FXML
    private void handleLogoutClick() {
        // Change button color to indicate it's been pressed
        logoutButton.setStyle("-fx-background-color: darkred; -fx-text-fill: white;");

        // Create confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Tizimdan chiqmoqchimisiz?", ButtonType.YES, ButtonType.NO);
        confirmDialog.initStyle(StageStyle.UTILITY); // Simple utility style

        // Style the dialog content
        confirmDialog.setHeaderText(null);
        confirmDialog.setTitle("Tasdiqlash");

        // Customize button styles and set hand cursor
        Button yesButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.YES);
        yesButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        yesButton.setCursor(Cursor.HAND);  // Set hand cursor for "Yes"

        Button noButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.NO);
        noButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        noButton.setCursor(Cursor.HAND);   // Set hand cursor for "No"

        // Handle dialog response
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                System.exit(0); // Exit application or add your logout logic here
            } else {
                // Reset button color if 'No' is chosen
                logoutButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
            }
        });
    }

    public void load() {
        try {
            loadMainMenu();
            loadCameraMenu();
            loadUserMenu();
            executors.execute(() -> {
                try {
                    updateConnections();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            executors.execute(this::loadTableData);

        } catch (IOException | RuntimeException e) {
            showAlert(Alert.AlertType.WARNING, "Warning", e.getMessage());
        }
    }

    private void addTableRow(TruckEntity truck) {
        TableViewData tableData = new TableViewData();
        tableData.setId(truck.getId());

        TruckActionEntity truckAction = truck.getTruckAction();
        switch (truckAction.getAction()) {
            case ENTRANCE, MANUAL_ENTRANCE -> {
                tableData.setEnteredTruckNumber(truck.getTruckNumber());
                tableData.setEnteredDate(truckService.getDate(truckAction.getCreatedAt()));
                tableData.setEnteredTime(truckService.getTime(truckAction.getCreatedAt()));
                tableData.setEnteredWeight(truckAction.getWeight());
                tableData.setEnteredOnDuty(truckAction.getOnDuty().getUsername());
            }
            case MANUAL_EXIT, EXIT -> {
                tableData.setExitedTruckNumber(truck.getTruckNumber());
                tableData.setExitedDate(truckService.getDate(truckAction.getCreatedAt()));
                tableData.setExitedTime(truckService.getTime(truckAction.getCreatedAt()));
                tableData.setExitedWeight(truckAction.getWeight());
                tableData.setExitedOnDuty(truckAction.getOnDuty().getUsername());
            }
        }
        this.tableData.getItems().add(tableData);
    }

    private void loadMainMenu() throws IOException {
        fxmlLoader.setLocation(getClass().getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();

        ToggleSwitch toggleSwitch = new ToggleSwitch("");
        toggleSwitch.setSelected(true);

        Label label = new Label("Massani avtomatik tarzda olish");
        HBox toggleWithLabel = new HBox(5, toggleSwitch, label);

        toggleWithLabel.setAlignment(Pos.CENTER);

        scaleAutomationPane.getChildren().clear();
        scaleAutomationPane.getChildren().add(toggleWithLabel);

        toggleWithLabel.layoutXProperty().bind(scaleAutomationPane.widthProperty().subtract(toggleWithLabel.widthProperty()).divide(2));
        toggleWithLabel.layoutYProperty().bind(scaleAutomationPane.heightProperty().subtract(toggleWithLabel.heightProperty()).divide(2));

        Stage mainWindowStage = new Stage();
        mainWindowStage.setTitle("Main Window");
        mainWindowStage.setScene(new Scene(root));

        mainWindowStage.show();
    }

    private void loadCameraMenu() {


    }

    private void loadUserMenu() {
        username.setText(currentUser.getUsername());
        phoneNumber.setText(currentUser.getPhoneNumber());
    }

    private void updateConnections() throws InterruptedException {
        while (true) {
            try {
                controller.setImage(isConnected ? greenLight : redLight);
                gate1.setImage(controllerService.checkConnection(COIL_CLOSE_GATE_1) ? greenLight : redLight);
                gate2.setImage(controllerService.checkConnection(COIL_CLOSE_GATE_2) ? greenLight : redLight);
                sensor1.setImage(controllerService.checkConnection(COIL_SENSOR_1) ? greenLight : redLight);
                sensor2.setImage(controllerService.checkConnection(COIL_SENSOR_2) ? greenLight : redLight);
                sensor3.setImage(controllerService.checkConnection(COIL_SENSOR_3) ? greenLight : redLight);
                camera1.setImage(controllerService.checkConnection(CAMERA_1) ? greenLight : redLight);
                camera2.setImage(controllerService.checkConnection(CAMERA_2) ? greenLight : redLight);
                camera3.setImage(Math.random() > 0.5 ? greenLight : redLight);
//                camera3.setImage(controllerService.checkConnection(CAMERA_3) ? greenLight : redLight);
            } catch (ModbusException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
            Thread.sleep(500);
        }
    }

    private void loadTableData() {
        truckService.save(new TruckEntity(
                "01A777AA", new TruckActionEntity(
                100.0, TruckAction.ENTRANCE, currentUser
        ), null
        ));
        List<TableViewData> collection = truckService.getTruckData();
        tableData.setItems(FXCollections.observableArrayList(
                collection
        ));
    }

    public void openGate1() {
        try {
            controllerService.openGate1();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void closeGate1() {
        try {
            controllerService.closeGate1();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void openGate2() {
        try {
            controllerService.openGate2();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void closeGate2() {
        try {
            controllerService.closeGate2();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private static void showAlert(Alert.AlertType alertType, String headerText, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(headerText);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
