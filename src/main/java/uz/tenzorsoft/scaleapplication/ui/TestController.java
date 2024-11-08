package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isScaleControlOn;

@Component
@RequiredArgsConstructor
public class TestController {

    private final ExecutorService executors;
    @FXML
    private Pane testSwitchPane, sensor1Pane, sensor2Pane, sensor3Pane;

    @FXML
    private Label testLabel, sensor1Label, sensor2Label, sensor3Label;

    @FXML
    private TextField weighInputField;

    public void setWeigh() {
        double weigh = Double.parseDouble(weighInputField.getText());
    }

    private void start(){

        executors.execute(()->{
            while(true){
                try {
                    Thread.sleep(500);
                } catch (Exception e) {

                }
            }
        });

    }


}
