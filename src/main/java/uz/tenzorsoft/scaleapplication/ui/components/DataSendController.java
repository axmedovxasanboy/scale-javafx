package uz.tenzorsoft.scaleapplication.ui.components;

import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.service.SendDataService;

import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;

@Component
@RequiredArgsConstructor
public class DataSendController {

    private final ExecutorService executors;
    private final SendDataService sendDataService;

    public void sendNotSentData() {
        executors.execute(() -> {
            while (true) {
                try {
                    sendDataService.sendNotSentData();
                    Thread.sleep(2000);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });


    }
}
