package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Settings;
import uz.tenzorsoft.scaleapplication.service.*;
import uz.tenzorsoft.scaleapplication.service.sendData.SendDataService;
import uz.tenzorsoft.scaleapplication.ui.components.DataSendController;
import uz.tenzorsoft.scaleapplication.ui.components.TruckScalingController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isScaleControlOn;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SCALE_PORT;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.scalePort;

@Component
@RequiredArgsConstructor
public class MainController {
    private final FXMLLoader fxmlLoader;
    private final ControllerService controllerService;
    private final TruckService truckService;
    private final SendDataService sendDataService;
    private final DataSendController dataSendController;
    private final ConnectionsController connectionsController;
    private final TruckScalingController truckScalingController;
    private final ButtonController buttonController;
    private final TableController tableController;
    private final ExecutorService executors;
    private final UserService userService;
    private final PrintCheck printCheck;
    private final TestController testController;
    private final ConfigUtilsService configUtilsService;

    @FXML
    private Pane scaleAutomationPane;

    @FXML
    private Button connectButton;

    public static void showAlert(Alert.AlertType alertType, String headerText, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(headerText);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static short showCargoScaleConfirmationDialog(double mass) {
        CompletableFuture<Short> futureResult = new CompletableFuture<>();

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Massani tasdiqlash");
            alert.setHeaderText(null);
            alert.initStyle(StageStyle.UTILITY);

            // Display mass (non-editable)
            Label massLabel = new Label("Massa: " + mass + " kg");
            alert.getDialogPane().setContent(massLabel);

            // Customizing buttons
            ButtonType confirmButton = new ButtonType("Tasdiqlash", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Bekor qilish", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(confirmButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            futureResult.complete(result.isPresent() && result.get() == confirmButton ? (short) 1 : (short) 0);
        });

        // Waits until the dialog result is available
        return futureResult.join();
    }


    public void load() {
        configUtilsService.loadConfigurations();
        loadMainMenu();
        scalePort = new Settings(SCALE_PORT).getSerialPort();
        connectionsController.updateConnections();
        dataSendController.sendNotSentData();
        truckScalingController.start();
        tableController.loadData();
        testController.start();
        printCheck.listAvailablePrinters();
        buttonController.connect();
        if (isConnected) {
            buttonController.closeGate1();
            buttonController.closeGate2();
        }
        controlConnectButton();
        System.out.println("All tasks are submitted!");

    }

    public static String showNumberInsertDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Enter License Plate");

        // Add TextField for license plate input
        TextField licensePlateField = new TextField();
        licensePlateField.setPromptText("Enter License Plate");

        licensePlateField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Replace spaces with dashes and convert lowercase letters to uppercase
            String formattedText = newValue.replace(" ", "-").replaceAll("[^a-zA-Z0-9-]", "").toUpperCase();

            // Set the formatted text back to the TextField
            licensePlateField.setText(formattedText);
        });

        dialog.getDialogPane().setContent(licensePlateField);

        // Add Confirm button
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Set the result converter for the dialog
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return licensePlateField.getText();
            }
            return "";
        });

        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    public static String showNotFinishedTrucksDialog(List<String> notFinishedTrucks) {
        if (notFinishedTrucks.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "All trucks are exited");
            return "";
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("License Plate Selection");
        dialog.setHeaderText("Select a License Plate");

        // Instruction label
        Label instructionLabel = new Label("Please select your license plate from the options below:");

        ComboBox<String> licensePlateDropdown = new ComboBox<>();
        licensePlateDropdown.getItems().addAll(notFinishedTrucks);
        licensePlateDropdown.setPromptText("Select License Plate");

        // Bind the Confirm button's disable property to the ComboBox's selection
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);

        // Enable confirm button only if a selection is made
        licensePlateDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            confirmButton.setDisable(newVal == null || newVal.isEmpty());
        });

        // Layout for dialog content
        VBox content = new VBox(10, instructionLabel, licensePlateDropdown);
        content.setStyle("-fx-padding: 10;");
        dialog.getDialogPane().setContent(content);

        // Set the result converter to return the selected value only on confirmation
        dialog.setResultConverter(button -> {
            if (button == confirmButtonType) {
                return licensePlateDropdown.getValue();
            }
            return "";
        });

        // Show dialog and handle the result
        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    private void controlConnectButton() {
        executors.execute(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> {
                        if (!isConnected) {
                            connectButton.setText("Connect");
                            connectButton.getStyleClass().removeAll("connect-button-disconnected");
                            connectButton.getStyleClass().add("connect-button");
                        } else {
                            connectButton.setText("Disconnect");
                            connectButton.getStyleClass().removeAll("connect-button");
                            connectButton.getStyleClass().add("connect-button-disconnected");
                        }
                    });
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()));
                }
            }
        });
    }

    private void loadMainMenu() {
        try {
            fxmlLoader.setLocation(getClass().getResource("/fxml/main.fxml"));
            Parent root = fxmlLoader.load();

            ToggleSwitch toggleSwitch = new ToggleSwitch("");
            toggleSwitch.setSelected(true);
            toggleSwitch.setOnMouseClicked(event -> {
                isScaleControlOn = toggleSwitch.isSelected();
            });

            Label label = new Label("Massani avtomatik tarzda olish");
            label.setOnMouseClicked(event -> {
                toggleSwitch.setSelected(!toggleSwitch.isSelected());
                isScaleControlOn = toggleSwitch.isSelected();
            });
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
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    private void connectToController() {
        if (isConnected) {
            buttonController.disconnect();
        } else {
            buttonController.connect();
        }
    }


}
