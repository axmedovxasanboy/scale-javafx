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
    private ToggleSwitch gate1Switch, gate2Switch;  // Added gate switches

    @FXML
    private TextField weighInputField;

    @FXML
    private TextField truckPositionField;

    @FXML
    public void initialize() {
        // Initialize Truck Position field as non-editable
        truckPositionField.setEditable(false);

        // Add listener to "Test" switch
        testStatusSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isTesting = newValue;
            updateToggleSwitchesState();
            updateTruckPosition(); // Update truck position if needed
        });

        updateToggleSwitchesState(); // Set initial state for switches
        updateTruckPosition();       // Set initial truck position
    }

    private void updateToggleSwitchesState() {
        boolean enableSwitches = isTesting;

        // Enable or disable switches and fields based on "Test" switch state
        sensor1Switch.setDisable(!enableSwitches);
        sensor2Switch.setDisable(!enableSwitches);
        sensor3Switch.setDisable(!enableSwitches);
        gate1Switch.setDisable(!enableSwitches);
        gate2Switch.setDisable(!enableSwitches);
        weighInputField.setDisable(!enableSwitches);

        // Reset other switches if "Test" is off
        if (!isTesting) {
            sensor1Switch.setSelected(false);
            sensor2Switch.setSelected(false);
            sensor3Switch.setSelected(false);
            gate1Switch.setSelected(false);
            gate2Switch.setSelected(false);
            truckPositionField.setText(""); // Clear truck position
        }
    }

    public void setWeigh() {
        double weigh = Double.parseDouble(weighInputField.getText());
    }

    private void updateTruckPosition() {
        if (isTesting) {
            // Example of setting truck position - this could be based on some condition
            truckPositionField.setText("Position 1");  // Example: dynamically set position
        } else {
            truckPositionField.setText("");
        }
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

    // Gate switch status handlers
    public void setGate1StatusSwitch() {
        if (isTesting) {
            // Only change state if testing is on
            gate1Connection = gate1Switch.isSelected();
        }
    }

    public void setGate2StatusSwitch() {
        if (isTesting) {
            // Only change state if testing is on
            gate2Connection = gate2Switch.isSelected();
        }
    }

    public void start() {
        executors.execute(() -> {
            while (true) {
                try {
                    if (!isTesting) {
                        // Disable all switches when testing is off
                        sensor1Switch.setSelected(false);
                        sensor1Connection = false;
                        sensor2Switch.setSelected(false);
                        sensor2Connection = false;
                        sensor3Switch.setSelected(false);
                        sensor3Connection = false;
                        gate1Switch.setSelected(false);
                        gate1Connection = false;
                        gate2Switch.setSelected(false);
                        gate2Connection = false;
                        testStatusSwitch.setSelected(false);

                        // Disable input field and switches
                        weighInputField.setDisable(true);
                        sensor1Switch.setDisable(true);
                        sensor2Switch.setDisable(true);
                        sensor3Switch.setDisable(true);
                        gate1Switch.setDisable(true);  // Disable Gate 1
                        gate2Switch.setDisable(true);  // Disable Gate 2
                    } else {
                        // Enable switches when testing is on
                        sensor1Connection = sensor1Switch.isSelected();
                        sensor2Connection = sensor2Switch.isSelected();
                        sensor3Connection = sensor3Switch.isSelected();
                        gate1Connection = gate1Switch.isSelected();
                        gate2Connection = gate2Switch.isSelected();
                        testStatusSwitch.setSelected(true);

                        // Enable input field and switches
                        weighInputField.setDisable(false);
                        sensor1Switch.setDisable(false);
                        sensor2Switch.setDisable(false);
                        sensor3Switch.setDisable(false);
                        gate1Switch.setDisable(false);  // Enable Gate 1
                        gate2Switch.setDisable(false);  // Enable Gate 2
                    }

                    Thread.sleep(1000);

                } catch (Exception ignored) {
                }
            }
        });
    }
}
