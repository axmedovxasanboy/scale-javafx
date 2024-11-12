package uz.tenzorsoft.scaleapplication.ui;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.ModbusException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.service.ControllerService;
import uz.tenzorsoft.scaleapplication.service.ScaleLogService;
import uz.tenzorsoft.scaleapplication.service.ScaleSystem;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.io.UnsupportedEncodingException;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;
import static uz.tenzorsoft.scaleapplication.ui.MainController.*;

@Component
@RequiredArgsConstructor
public class ButtonController {

    private final ControllerService controllerService;
    private final TruckService truckService;
    private final CameraViewController cameraViewController;
    private final TableController tableController;
    private final ScaleLogService scaleLogService;

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
            if (!isTesting) controllerService.openGate1();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
    }

    public void openGate1(int truckPosition) {
        try {
            if (!isTesting) controllerService.openGate1(truckPosition);
            else {
                ScaleSystem.truckPosition = truckPosition;
                gate1Connection = true;
            }
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
    }

    public void openGate1Manually() {
        try {
            isWaiting = true;
            String truckNumber = showNumberInsertDialog();
            if (!truckNumber.isEmpty()) {
                // truckNumber == "RAQAMSIZ"
                if (!truckService.isValidTruckNumber(truckNumber) && !truckService.isStandard(truckNumber)) {
                    showAlert(Alert.AlertType.WARNING, "Not match", "Raqam mos kelmadi: " + truckNumber);
                    return;
                }
                if (!truckService.isEntranceAvailableForCamera1(truckNumber)) {
                    showAlert(Alert.AlertType.WARNING, "Not available", truckNumber + " kirishi mumkin emas");
                    return;
                }
                currentTruck.setTruckNumber(truckNumber);
                Long attachId = cameraViewController.takePicture(CAMERA_1).getId();
                currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.MANUAL_ENTRANCE_PHOTO));
                currentTruck.setEnteredStatus(TruckAction.MANUAL_ENTRANCE);
                truckService.saveTruck(currentTruck, 1);
                tableController.addLastRecord();
                openGate1(0);
                isWaiting = false;
                System.out.println("Saving truck number: " + truckNumber + " from Camera 1");
            }
        } catch (Exception e) {
            isWaiting = false;
            e.printStackTrace();
        }
    }

    public void openGate2Manually() {
        try {
            isWaiting = true;
            String truckNumber = showNotFinishedTrucksDialog(truckService.getNotFinishedTrucks());
            if (!truckNumber.isEmpty()) {
                // "RAQAMSIZ"
                if (!truckService.isValidTruckNumber(truckNumber) && !truckService.isStandard(truckNumber)) {
                    showAlert(Alert.AlertType.WARNING, "Not match", "Raqam mos kelmadi: " + truckNumber);
                    return;
                }
                if (!truckService.isNotFinishedTrucksExists()) {
                    showAlert(Alert.AlertType.WARNING, "Not found", "Barcha avtomobillar chiqib ketgan!");
                    return;
                }
                if (!truckService.isEntranceAvailableForCamera2(truckNumber)) {
                    showAlert(Alert.AlertType.WARNING, "Not found", truckNumber + " kirishi mumkin emas");
                    return;
                }
                currentTruck.setTruckNumber(truckNumber);
                Long attachId = cameraViewController.takePicture(CAMERA_3).getId();
                currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.MANUAL_EXIT_PHOTO));
                currentTruck.setExitedStatus(TruckAction.MANUAL_EXIT);
                truckService.saveTruck(currentTruck, 2);
                openGate2(7);
                isWaiting = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeGate1() {
        try {
            if (!isTesting) controllerService.closeGate1();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void openGate2() {
        try {
            if (!isTesting) controllerService.openGate2();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
    }

    public void openGate2(int truckPosition) {
        try {
            if (!isTesting) controllerService.openGate2(truckPosition);
            else {
                ScaleSystem.truckPosition = truckPosition;
                gate2Connection = true;
            }
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
    }

    public void closeGate2() {
        try {
            if (!isTesting) controllerService.closeGate2();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void connect() {
        try {
            controllerService.connect();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }

    }

    public void disconnect() {
        controllerService.disconnect();
    }

    public double getTruckWeigh() {
        try {
            scalePort.closePort();
            scalePort.openPort();

            scalePort.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
            scalePort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

            byte[] readBuffer = new byte[1024];
            int bytesRead = scalePort.readBytes(readBuffer, readBuffer.length);
            if (bytesRead > 0) {
                String data = new String(readBuffer, 0, bytesRead).trim();

                try {
                    System.out.println("Scale data: " + new String(data.getBytes(), "UTF-8"));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                double numericValue = parseWeightData(data);
                numericValue /= 10;
                scaleLogService.save(data, String.valueOf(numericValue));
                System.out.println("Kg: " + numericValue);
                return numericValue;
            }

        } catch (Exception e) {
            return 0.0;
        }
        return 0.0;
    }

    private double parseWeightData(String data) {
        try {
            String numericPart = data.substring(1, 8);
            return Integer.parseInt(numericPart);
        } catch (Exception e) {
            System.err.println("Unable to scale data");
            System.err.println(e.getMessage());
        }

        return 0;
    }
}
