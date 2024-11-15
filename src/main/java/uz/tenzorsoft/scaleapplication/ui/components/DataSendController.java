package uz.tenzorsoft.scaleapplication.ui.components;

import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.sendData.SendDataService;

import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isTesting;
import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;

@Component
@RequiredArgsConstructor
public class DataSendController {

    private final ExecutorService executors;
    private final SendDataService sendDataService;
    private final LogService logService;

    public void sendNotSentData() {
        executors.execute(() -> {
            while (true) {
                try {
                    try {
                        sendDataService.sendNotSentData();
                    } catch (Exception e) {
                        logService.save(new LogEntity(5L,Instances.truckNumber, e.getMessage()));
                        System.err.println(e.getMessage());
                    }
                    try {
                        if(!isTesting) sendDataService.sendDataToMyCoal();
                    } catch (Exception e) {
                        logService.save(new LogEntity(5L,Instances.truckNumber, e.getMessage()));
                        System.err.println(e.getMessage());
                    }
                    try {
                        if(!isTesting) sendDataService.sendLogsToServer();
                    } catch (Exception e) {
                        logService.save(new LogEntity(5L,Instances.truckNumber, e.getMessage()));
                        System.err.println(e.getMessage());
                    }
//                    try {
//                        sendDataService.sendStatuses();
//                    } catch (Exception e) {
//                        System.err.println(e.getMessage());
//                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    logService.save(new LogEntity(5L,Instances.truckNumber, e.getMessage()));
//                    System.err.println(e.getMessage());
                }
            }
        });
    }
}
