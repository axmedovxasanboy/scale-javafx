package uz.tenzorsoft.scaleapplication.ui;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.service.ControllerService;

import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;

@Component
@RequiredArgsConstructor
public class ButtonController {

    private final ControllerService controllerService;

    @FXML
    private ImageView gate1;

    @FXML
    private ImageView gate2;

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
    public void initialize() {
        setupButtonPressEffect(button1, "#4CAF50");
        setupButtonPressEffect(button2, "#4CAF50");
        setupButtonPressEffect(button3, "#C5C900");
        setupButtonPressEffect(button4, "#D32F2F");
        setupButtonPressEffect(button5, "#D32F2F");
    }

    private void setupButtonPressEffect(Button button, String pressedColor) {
        String originalColor = button.getStyle();

        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: " + pressedColor + "; -fx-border-color: black; -fx-border-radius: 7; -fx-background-radius: 10; -fx-border-width: 0.8;"));

        button.setOnMouseReleased(event -> button.setStyle(originalColor));
    }


    public void openGate1() {
        try {
            controllerService.openGate1();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void openGate1(int truckPosition) {
        try {
            controllerService.openGate1(truckPosition);
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

    public void openGate2(int truckPosition) {
        try {
            controllerService.openGate2(truckPosition);
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
}
