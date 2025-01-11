package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
public class ScaleController {

    private final ExecutorService executors;
    private final ButtonController buttonController;

    @FXML
    private TextField scaleWeigh;

    public void initialize() {
        scaleWeigh.setDisable(true);
        scaleWeigh.setText("000 kg");
    }

    public void showScale() {
        executors.execute(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> {
                        double weigh = buttonController.getTruckWeigh();
                        scaleWeigh.setText(String.valueOf(weigh));
                    });

                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        });
    }

}
