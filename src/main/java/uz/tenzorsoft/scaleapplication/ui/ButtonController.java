package uz.tenzorsoft.scaleapplication.ui;

import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.ModbusException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.CommandsEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.domain.response.CheckCommandsDto;
import uz.tenzorsoft.scaleapplication.service.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.domain.Settings.CAMERA_1;
import static uz.tenzorsoft.scaleapplication.domain.Settings.CAMERA_3;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.scalePort;
import static uz.tenzorsoft.scaleapplication.ui.MainController.*;

@Component
@RequiredArgsConstructor
public class ButtonController implements BaseController {

    private final ControllerService controllerService;
    private final TruckService truckService;
    private final CameraViewController cameraViewController;
    private final ScaleLogService scaleLogService;
    private final LogService logService;
    private final RestTemplate restTemplate;

    @Autowired
    @Lazy
    private MainController mainController;
    @Autowired
    @Lazy
    private TableController tableController;

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

    private String commandComment = "";

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


    public boolean openGate1() {
        try {
            if (!isTesting) {
                controllerService.openGate1();
                commandComment = "Finished";
            }
        } catch (ModbusException e) {
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00017: (" + getClass().getName() + ") " +e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean openGate1(int truckPosition) {
        try {
            if (!isTesting) return controllerService.openGate1(truckPosition);
            else {
                ScaleSystem.truckPosition = truckPosition;
                gate1Connection = true;
                return true;
            }
        } catch (ModbusException e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00018: (" + getClass().getName() + ") " +e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean openGate1Manually() {
        try {
            if (!isConnected) {
                showAlert(Alert.AlertType.ERROR, "Error","Controllerga ulanmagan");
                return false;
            }
            isWaiting = true;
            String truckNumber = showNumberInsertDialog();
            if (!truckNumber.isEmpty()) {
                // truckNumber == "RAQAMSIZ"
//                if (!truckService.isValidTruckNumber(truckNumber) && !truckService.isStandard(truckNumber)) {
//                    showAlert(Alert.AlertType.WARNING, "Not match", "Raqam mos kelmadi: " + truckNumber);
//                    return;
//                }
                if (!truckService.isEntranceAvailableForCamera1(truckNumber)) {
                    logService.save(new LogEntity(5L, truckNumber, "00019: (" + getClass().getName() + ") " + "Entrance not available"));
                    showAlert(Alert.AlertType.WARNING, "Not available", truckNumber + " kirishi mumkin emas");
                    return false;
                }
                currentTruck.setTruckNumber(truckNumber);
                AttachResponse attachResponse = cameraViewController.takePicture(CAMERA_1);
                Long attachId = attachResponse.getId();
                currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.MANUAL_ENTRANCE_PHOTO));
                currentTruck.setEnteredStatus(TruckAction.MANUAL_ENTRANCE);
                truckService.saveTruck(currentTruck, 1, attachResponse);
                tableController.addLastRecord();
                openGate1(0);
                isWaiting = false;
                System.out.println("Saving truck number: " + truckNumber + " from Camera 1");
                return true;
            }
        } catch (Exception e) {
            isWaiting = false;
            logService.save(new LogEntity(5L, truckNumber, "00020: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
        }
        return false;
    }

    public boolean openGate2Manually() {
        try {
            if (!isConnected) {
                showAlert(Alert.AlertType.ERROR, "Xatolik","Controllerga ulanmagan");
                return false;
            }
            isWaiting = true;
            String truckNumber = showNotFinishedTrucksDialog(truckService.getNotFinishedTrucks());
            if (!truckNumber.isEmpty()) {
                // "RAQAMSIZ"
//                if (!truckService.isValidTruckNumber(truckNumber) && !truckService.isStandard(truckNumber)) {
//                    // mainController.showAlert(Alert.AlertType.WARNING, "Not match", "Raqam mos kelmadi: " + truckNumber);
//                    return;
//                }
                if (!truckService.isNotFinishedTrucksExists()) {
                    logService.save(new LogEntity(5L, truckNumber, "00021: (" + getClass().getName() + ") " +"All trucks are exited!"));
                    // showAlert(Alert.AlertType.WARNING, "Topilmadi", "Barcha avtomobillar chiqib ketgan!");
                    return false;
                }
                if (!truckService.isEntranceAvailableForCamera2(truckNumber)) {
                    logService.save(new LogEntity(5L, truckNumber, "00022: (" + getClass().getName() + ") " +"Entrance not available"));
                    // showAlert(Alert.AlertType.WARNING, "Topilmadi", truckNumber + " kirishi mumkin emas");
                    return false;
                }
                currentTruck.setTruckNumber(truckNumber);
                AttachResponse attachResponse = cameraViewController.takePicture(CAMERA_3);
                Long attachId = attachResponse.getId();
                currentTruck.getAttaches().add(new AttachIdWithStatus(attachId, AttachStatus.MANUAL_EXIT_PHOTO));
                currentTruck.setExitedStatus(TruckAction.MANUAL_EXIT);
                truckService.saveTruck(currentTruck, 2, attachResponse);
                openGate2(7);
                return true;
            }
        } catch (Exception e) {
            logService.save(new LogEntity(5L, truckNumber, "00023: (" + getClass().getName() + ") " +e.getMessage()));
            e.printStackTrace();
        }
        isWaiting = false;
        return false;
    }

    public boolean closeGate1() {
        try {
            if (!isTesting) {
                controllerService.closeGate1();
                commandComment = "Finished";
            }
        } catch (ModbusException e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber,"00024: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
        return false;
    }

    public boolean openGate2() {
        try {
            if (!isTesting)  return controllerService.openGate2();
        } catch (ModbusException e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber, "00025: (" + getClass().getName() + ") " +e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean openGate2(int truckPosition) {
        try {
            if (!isTesting) return controllerService.openGate2(truckPosition);
            else {
                ScaleSystem.truckPosition = truckPosition;
                gate2Connection = true;
                return true;
            }
        } catch (ModbusException e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber,"00026: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }
        return false;
    }

    public boolean closeGate2() {
        try {
            if (!isTesting) {
                controllerService.closeGate2();
                commandComment = "Finished";
            }
        } catch (ModbusException e) {
            commandComment = e.getMessage();
            System.err.println(e.getMessage());
            logService.save(new LogEntity(5L, truckNumber,"00027: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
        return false;
    }

    public void connect() {
        try {
            controllerService.connect();
        } catch (Exception e) {
            logService.save(new LogEntity(5L, truckNumber, "00028: (" + getClass().getName() + ") " +e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Xatolik", e.getMessage());
        }

    }

    public void disconnect() {
        controllerService.disconnect();
    }

    public void handleServerCommands(CommandsEntity commands) {
        if (commands.getOpenGate1()) {
            openGate1();
            sendCommandStatus(commands.getServerId(), commandComment);
        }else if (commands.getOpenGate2()) {
            openGate2();
            sendCommandStatus(commands.getServerId(), commandComment);
        } else if (commands.getCloseGate1()) {
            closeGate1();
            sendCommandStatus(commands.getServerId(), commandComment);
        }else if (commands.getCloseGate2()) {
            closeGate2();
            sendCommandStatus(commands.getServerId(), commandComment);
        }else if(commands.getWeighing()){
            getTruckWeigh();
            sendCommandStatus(commands.getServerId(), commandComment);
        }
            commandComment = "";
    }

    private void sendCommandStatus(long commandId, String commandComment) {
        boolean isFinished = !commandComment.isEmpty();
        CheckCommandsDto request = new CheckCommandsDto(commandId, commandComment, isFinished);
        restTemplate.postForEntity(
                Instances.SERVER_URL + "/commands/checkCommands",
                request, Void.class
        );
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
                    commandComment = e.getMessage();
                    System.err.println(e.getMessage());
                    return 0.0;
                }
                double numericValue = parseWeightData(data);
                scaleLogService.save(data, String.valueOf(numericValue));
                System.out.println("Kg: " + numericValue);
                commandComment = "Finished";
                return numericValue;
            }
            commandComment = "bytes read is 0";

        } catch (Exception e) {
            commandComment = e.getMessage();
            return 0.0;
        }
        return 0.0;
    }

    private double parseWeightData(String data) {
        try {
            Pattern pattern = Pattern.compile("\\+\\d{7}\\w");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                String entry = matcher.group();
                String numericPart = entry.substring(1, entry.length() - 1);
                return Integer.parseInt(numericPart) / 10.0;
            }
            return 0.0;
        } catch (Exception e) {
            commandComment = e.getMessage();
            System.err.println("Unable to parse weight data");
            System.err.println(e.getMessage());
            return 0;
        }
    }
}
