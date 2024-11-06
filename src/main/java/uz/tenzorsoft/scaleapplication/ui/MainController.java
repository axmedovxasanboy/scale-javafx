package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.service.ControllerService;
import uz.tenzorsoft.scaleapplication.service.SendDataService;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnectedToInternet;

@Component
@RequiredArgsConstructor
public class MainController {
    private final FXMLLoader fxmlLoader;
    private final ExecutorService executors;
    private final ControllerService controllerService;
    private final TruckService truckService;
    private final SendDataService sendDataService;

    @FXML
    private Pane scaleAutomationPane;

    @FXML
    private Button connectButton;

    public void load() {
        try {
            loadMainMenu();
            loadCameraMenu();
            executors.execute(() -> {
                while (true) {
                    sendDataService.sendStatuses();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    }
                }
            });
            executors.execute(() -> {
                while (true) {
                    if (isConnectedToInternet) sendDataService.sendNotSentData();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    }
                }
            });

            executors.execute(() -> {
                while (true) {
                    try (Socket socket = new Socket()) {
                        SocketAddress address = new InetSocketAddress("8.8.8.8", 53);
                        socket.connect(address, 2000);
                        isConnectedToInternet = true;
                    } catch (IOException ignored) {
                        isConnectedToInternet = false;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    }
                }
            });
        } catch (IOException | RuntimeException e) {
            showAlert(Alert.AlertType.WARNING, "Warning", e.getMessage());
        }
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

    @FXML
    private void connectToController() {
        try {
            if (isConnected) {
                controllerService.disconnect();
                connectButton.setText("Connect to controller");
            } else {
                controllerService.connect();
                connectButton.setText("Disconnect from controller");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public static void showAlert(Alert.AlertType alertType, String headerText, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(headerText);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public String showNumberVerificationDialog(String truckNumber, List<TruckEntity> enteredTrucks) {
        return null;
    }
}
