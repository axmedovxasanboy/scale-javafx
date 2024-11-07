package uz.tenzorsoft.scaleapplication.ui;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.service.ControllerService;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;
import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;


@Component
@RequiredArgsConstructor
public class ConnectionsController {

    private final ExecutorService executors;
    private final ControllerService controllerService;
    @FXML
    private ImageView controller, camera1, camera2, camera3, gate1, gate2, sensor1, sensor2, sensor3;

    public void initialize() {
        controller.setImage(redLight);
        camera1.setImage(redLight);
        camera2.setImage(redLight);
        camera3.setImage(redLight);
        sensor1.setImage(redLight);
        sensor2.setImage(redLight);
        sensor3.setImage(redLight);
        gate1.setImage(redLight);
        gate2.setImage(redLight);
    }

    public void updateConnections() {
        executors.execute(() -> {
            while (true) {
                try {
                    gate1Connection = controllerService.checkConnection(COIL_CLOSE_GATE_1);
                    gate2Connection = controllerService.checkConnection(COIL_CLOSE_GATE_2);
                    sensor1Connection = controllerService.checkConnection(COIL_SENSOR_1);
                    sensor2Connection = controllerService.checkConnection(COIL_SENSOR_2);
                    sensor3Connection = controllerService.checkConnection(COIL_SENSOR_3);
                    camera1Connection = controllerService.checkConnection(CAMERA_1);
                    camera2Connection = controllerService.checkConnection(CAMERA_2);
                    camera3Connection = controllerService.checkConnection(CAMERA_3);
                    isConnectedToInternet = controllerService.checkConnection(GOOGLE_DNS);

                    controller.setImage(isConnected ? greenLight : redLight);
                    camera1.setImage(camera1Connection ? greenLight : redLight);
                    camera2.setImage(camera2Connection ? greenLight : redLight);
                    camera3.setImage(camera3Connection ? greenLight : redLight);
                    sensor1.setImage(sensor1Connection ? greenLight : redLight);
                    sensor2.setImage(sensor2Connection ? greenLight : redLight);
                    sensor3.setImage(sensor3Connection ? greenLight : redLight);
                    gate1.setImage(gate1Connection ? greenLight : redLight);
//                gate2.setImage(gate2Connection ? greenLight : redLight);
                    gate2.setImage(Math.random() > 0.5 ? greenLight : redLight);


                    Thread.sleep(500);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });
    }


}
