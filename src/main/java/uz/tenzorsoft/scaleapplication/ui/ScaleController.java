package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ScaleController {

    private final ExecutorService executors;
    private final ButtonController buttonController;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @FXML
    private TextField scaleWeigh;


    public void initialize() {
        scaleWeigh.setDisable(true);
        scaleWeigh.setText("000 kg");
    }

    public void showScale() {
        scheduler.scheduleAtFixedRate(() -> {
            double weigh = buttonController.getTruckWeigh();
            Platform.runLater(() -> scaleWeigh.setText(weigh + " kg"));
        }, 0, 500, TimeUnit.MILLISECONDS);
    }


//    public void showScale() {
//        executors.execute(() -> {
//            while (true) {
//                try {
//                    Platform.runLater(() -> {
//                        double weigh = buttonController.getTruckWeigh();
//                        scaleWeigh.setText(weigh + " kg");
//                    });
//
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.getMessage();
//                }
//            }
//        });
//    }

}
