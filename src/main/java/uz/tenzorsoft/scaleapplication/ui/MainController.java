package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckPhotosEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.service.ControllerService;
import uz.tenzorsoft.scaleapplication.service.SendDataService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.service.UserService;
import uz.tenzorsoft.scaleapplication.ui.components.DataSendController;
import uz.tenzorsoft.scaleapplication.ui.components.TruckScalingController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isScaleControlOn;

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

    @FXML
    private Pane scaleAutomationPane;

    @FXML
    private Button connectButton;

    public void load() {
        loadMainMenu();
        connectionsController.updateConnections();
        dataSendController.sendNotSentData();
        truckScalingController.start();
        tableController.loadData();

    }

    private void loadMainMenu() {
        try {
            fxmlLoader.setLocation(getClass().getResource("/fxml/main.fxml"));
            Parent root = fxmlLoader.load();

            ToggleSwitch toggleSwitch = new ToggleSwitch("");
            toggleSwitch.setSelected(true);

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
            connectButton.setText("Connect to controller");
        } else {
            buttonController.connect();
            connectButton.setText("Disconnect from controller");
        }
    }

    public static void showAlert(Alert.AlertType alertType, String headerText, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(headerText);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static short showCargoScaleConfirmationDialog(double weight) {
        AtomicReference<Short> resp = new AtomicReference<>((short) 0);
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Truck total weight", ButtonType.APPLY, ButtonType.CANCEL);
        confirmDialog.initStyle(StageStyle.UTILITY);

        confirmDialog.setHeaderText(null);
        confirmDialog.setTitle("Truck weigh is" + weight + "\nDo you accept?");
        Button applyButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.APPLY);
        applyButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        applyButton.setCursor(Cursor.HAND);

        Button cancelButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        cancelButton.setCursor(Cursor.HAND);

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.APPLY) {
                resp.set((short) 1);
            } else {
                resp.set((short) 0);
            }
        });
        return resp.get();
    }


}
