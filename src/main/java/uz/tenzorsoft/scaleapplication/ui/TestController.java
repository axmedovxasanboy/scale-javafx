package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;

@Component
@RequiredArgsConstructor
public class TestController {

    private final ExecutorService executors;
    @FXML
    private Pane testSwitchPane, sensor1Pane, sensor2Pane, sensor3Pane;

    @FXML
    private Label testLabel, sensor1Label, sensor2Label, sensor3Label;

    @FXML
    private ToggleSwitch sensor1Switch, sensor2Switch, sensor3Switch, testStatusSwitch;

    @FXML
    private TextField weighInputField;

    public void setWeigh() {
        double weigh = Double.parseDouble(weighInputField.getText());
    }

    public void setTestingStatusLabel() {
        testStatusSwitch.setSelected(!testStatusSwitch.isSelected());
        isTesting = testStatusSwitch.isSelected();
    }

    public void setSensor1StatusLabel() {
        if (isTesting) {
            sensor1Switch.setSelected(!sensor1Switch.isSelected());
            sensor1Connection = sensor1Switch.isSelected();
        }
    }

    public void setSensor2StatusLabel() {
        if (isTesting) {
            sensor2Switch.setSelected(!sensor2Switch.isSelected());
            sensor2Connection = sensor2Switch.isSelected();
        }
    }

    public void setSensor3StatusLabel() {
        if (isTesting) {
            sensor3Switch.setSelected(!sensor3Switch.isSelected());
            sensor3Connection = sensor3Switch.isSelected();
        }
    }

    public void setTestingStatusSwitch() {
        isTesting = testStatusSwitch.isSelected();
    }

    public void setSensor1StatusSwitch() {
        sensor1Connection = sensor1Switch.isSelected();
    }

    public void setSensor2StatusSwitch() {
        sensor2Connection = sensor2Switch.isSelected();
    }

    public void setSensor3StatusSwitch() {
        sensor3Connection = sensor3Switch.isSelected();
    }

    public void start() {
        executors.execute(() -> {
            while (true) {
                try {
                    if (!isTesting) {
                        sensor1Switch.setSelected(false);
                        sensor1Connection = sensor1Switch.isSelected();
                        sensor2Switch.setSelected(false);
                        sensor2Connection = sensor2Switch.isSelected();
                        sensor3Switch.setSelected(false);
                        sensor3Connection = sensor3Switch.isSelected();
                        testStatusSwitch.setSelected(false);
                        isTesting = false;
                        weighInputField.setDisable(true);
                        sensor1Switch.setDisable(true);
                        sensor2Switch.setDisable(true);
                        sensor3Switch.setDisable(true);
                    } else {
                        sensor1Connection = sensor1Switch.isSelected();
                        sensor2Connection = sensor2Switch.isSelected();
                        sensor3Connection = sensor3Switch.isSelected();
                        testStatusSwitch.setSelected(true);
                        weighInputField.setDisable(false);
                        sensor1Switch.setDisable(false);
                        sensor2Switch.setDisable(false);
                        sensor3Switch.setDisable(false);
                        isTesting = true;
                    }

                    Thread.sleep(1000);

                } catch (Exception ignored) {

                }
            }
        });
    }


}
