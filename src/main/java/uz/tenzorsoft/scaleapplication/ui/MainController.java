package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.Settings;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.service.ConfigUtilsService;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;
import uz.tenzorsoft.scaleapplication.ui.components.DataSendController;
import uz.tenzorsoft.scaleapplication.ui.components.SendStatuesDataController;
import uz.tenzorsoft.scaleapplication.ui.components.TruckScalingController;
import uz.tenzorsoft.scaleapplication.websocket.WebSocketClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.SCALE_PORT;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.scalePort;

@Component
@RequiredArgsConstructor
public class MainController implements BaseController {
    private final FXMLLoader fxmlLoader;
    private final DataSendController dataSendController;
    private final ConfigUtilsService configUtilsService;
    private final SendStatuesDataController sendStatuesDataController;
    private final LogService logService;
    private final UserController userController;

    @Autowired
    @Lazy
    private WebSocketClient webSocketClient;

    @Autowired
    @Lazy
    private ConnectionsController connectionsController;

    @Autowired
    @Lazy
    private PrintCheck printCheck;

    @Autowired
    @Lazy
    private TableController tableController;
    @Autowired
    @Lazy
    private ButtonController buttonController;
    @Autowired
    @Lazy
    private TruckScalingController truckScalingController;
    @Autowired
    @Lazy
    private TestController testController;
    @Autowired
    @Lazy
    private ControlPane controlPane;

    @FXML
    private Pane scaleAutomationPane;


//    public void showAlert(Alert.AlertType alertType, String headerText, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(headerText);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }

    public static short showCargoScaleConfirmationDialog(TruckEntity currentTruckEntity, double mass) {
        CompletableFuture<Short> futureResult = new CompletableFuture<>();
        double entranceWeigh = currentTruckEntity.getTruckActions().stream()
                .filter(action -> action.getAction() == TruckAction.ENTRANCE
                        || action.getAction() == TruckAction.MANUAL_ENTRANCE).findFirst()
                .map(TruckActionEntity::getWeight).orElse(0.0);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Massani tasdiqlash");
            alert.setHeaderText(null);
            alert.initStyle(StageStyle.UTILITY);

            // Display mass details (non-editable)
            Label massLabel = new Label(
                    "Sof og'irlik: " + Math.abs(mass - entranceWeigh) + " kg\n" +
                            "Kirishdagi massa: " + entranceWeigh + " kg\n" +
                            "Chiqishdagi massa: " + mass
            );
            alert.getDialogPane().setContent(massLabel);

            // Customizing buttons
            ButtonType confirmButton = new ButtonType("Tasdiqlash", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Bekor qilish", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType remeasureButton = new ButtonType("Qayta o'lchash", ButtonBar.ButtonData.OTHER); // Add Re-measure button
            alert.getButtonTypes().setAll(confirmButton, remeasureButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();

            // Determine the result based on the button clicked
            if (result.isPresent()) {
                if (result.get() == confirmButton) {
                    futureResult.complete((short) 1); // Confirm
                } else if (result.get() == remeasureButton) {
                    futureResult.complete((short) 2); // Re-measure
                } else {
                    futureResult.complete((short) 0); // Cancel
                }
            } else {
                futureResult.complete((short) 0); // No result (treated as cancel)
            }
        });

        try {
            return futureResult.get(); // Block until a result is available
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // Return cancel in case of an error
        }
    }


    public void load() {
        configUtilsService.loadConfigurations();
        loadMainMenu();
        userController.loadUserMenu();
        try {
            scalePort = new Settings(SCALE_PORT).getSerialPort();
            webSocketClient.connect(Instances.WEBSOCKET_URL);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, e.getMessage()));
            System.err.println(e.getMessage());
        }
        connectionsController.updateConnections();
        dataSendController.sendNotSentData();
        truckScalingController.start();
        tableController.loadData();
        testController.start();
        sendStatuesDataController.startSending();
        printCheck.listAvailablePrinters();
        if (isAvailableToConnect) buttonController.connect();
        if (isConnected) {
            buttonController.closeGate1();
            buttonController.closeGate2();
        }
        controlPane.controlConnectButton();
        System.out.println("All tasks are submitted!");

    }

    public static String showNumberInsertDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Avtomabil raqamini kiriting");

        // Add TextField for license plate input
        TextField licensePlateField = new TextField();
        licensePlateField.setPromptText("Avtomobil raqamini kiriting");

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
            //showAlert(Alert.AlertType.WARNING, "Warning", "All trucks are exited");
            return "";
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Avtomobil raqamini tanlash");
        dialog.setHeaderText("Avtomobil raqamini tanlang");

        // Instruction label
        Label instructionLabel = new Label("Quyidagi variantlardan avtomobil raqamini tanlang:");

        ComboBox<String> licensePlateDropdown = new ComboBox<>();
        licensePlateDropdown.getItems().addAll(notFinishedTrucks);
        licensePlateDropdown.setPromptText("Avtomobil raqamini tanlang");

        // Bind the Confirm button's disable property to the ComboBox's selection
        ButtonType confirmButtonType = new ButtonType("Tasdiqlash", ButtonBar.ButtonData.OK_DONE);
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

    public Map<String, String> showTruckNotFoundPopupWithSelection(String incorrectTruckNumber, List<String> notExitedTruckNumbers) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Xato raqam");
        dialog.setHeaderText("Kiritilgan avtomobil raqami noto'g'ri: " + incorrectTruckNumber);
        dialog.setContentText("Quyida avtomobillar ro'yxati keltirilgan. Tanlash yoki o'zgartirishni amalga oshiring.");

        Label instructionLabel = new Label("To'g'ri avtomobil raqamini tanlang yoki o'zgartiring:");
        ComboBox<String> licensePlateDropdown = new ComboBox<>();
        licensePlateDropdown.getItems().addAll(notExitedTruckNumbers);
        licensePlateDropdown.setPromptText("Avtomobil raqamini tanlang");

        TextField textField = new TextField(incorrectTruckNumber);
        textField.setPromptText("Avtomobil raqamini kiriting");

        licensePlateDropdown.valueProperty().addListener((obs, oldVal, newVal) -> {
            textField.setText(newVal);
        });

        ButtonType confirmButtonType = new ButtonType("Tasdiqlash", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        // Get the confirm button from the dialog
        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true); // Initially disabled

        // Enable the button only when a selection is made or text is entered
        confirmButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> licensePlateDropdown.getValue() == null || licensePlateDropdown.getValue().trim().isEmpty(),
                        licensePlateDropdown.valueProperty()
                )
        );

        VBox content = new VBox(10, instructionLabel, licensePlateDropdown, textField);
        content.setStyle("-fx-padding: 10;");
        dialog.getDialogPane().setContent(content);

        // Result converter
        dialog.setResultConverter(button -> {
            if (button == confirmButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("dropDownValue", licensePlateDropdown.getValue());
                result.put("textFieldValue", textField.getText().trim());
                return result;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();
        return result.orElse(null);
    }


    private void loadMainMenu() {
        try {
            fxmlLoader.setLocation(getClass().getResource("/fxml/main.fxml"));
            Parent root = fxmlLoader.load();

            ToggleSwitch toggleSwitch = new ToggleSwitch("");
            toggleSwitch.setSelected(true);
            toggleSwitch.setOnMouseClicked(event -> {
                boolean selected = toggleSwitch.isSelected();
                isScaleControlOn = selected;
                if (selected) {
                    cargoConfirmationStatus = 1;
                } else {
                    cargoConfirmationStatus = -1;
                }
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

            // Disable the button initially
            controlPane.getIssueCheckButton().setDisable(true);

            // Set up row selection logic


            mainWindowStage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }


    }
}
