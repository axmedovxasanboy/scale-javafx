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
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;

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
    private TextField truckNumberFieldCamera1;
    @FXML
    private TextField truckNumberFieldCamera2;


    @FXML
    public void initialize() {
        // Initialize truck number fields
        truckNumberFieldCamera1.setEditable(true);
        truckNumberFieldCamera2.setEditable(true);

        // Listener for the "Test" switch
        testStatusSwitch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            isTesting = newValue;
            updateToggleSwitchesState();
            updateTruckPosition();
        });

        // Initialize the toggle states
        updateToggleSwitchesState();
        updateTruckPosition();
    }


    private void updateToggleSwitchesState() {
        boolean enableSensors = isTesting;

        // Enable or disable sensor switches based on "Test" switch
        sensor1Switch.setDisable(!enableSensors);
        sensor2Switch.setDisable(!enableSensors);
        sensor3Switch.setDisable(!enableSensors);
        weighInputField.setDisable(!enableSensors);

        // Gate switches should be enabled manually but remain disabled if testing is off
        gate1Switch.setDisable(!isTesting);
        gate2Switch.setDisable(!isTesting);

        // Set switch state depending on test mode
        if (isTesting) {
            // Turn on sensors automatically if "Test" is on
            sensor1Switch.setSelected(true);
            sensor2Switch.setSelected(true);
            sensor3Switch.setSelected(true);
        } else {
            // Turn off all switches if "Test" is off
            sensor1Switch.setSelected(false);
            sensor2Switch.setSelected(false);
            sensor3Switch.setSelected(false);
            gate1Switch.setSelected(false);
            gate2Switch.setSelected(false);
            truckPositionField.setText(""); // Clear truck position
        }
    }

    public void getTruckNumberFromCamera1() {
        String truckNumber = truckNumberFieldCamera1.getText();
        if (!truckNumber.isEmpty()) {
            // Save the truck number to the database
            saveTruckNumberToDatabase(truckNumber, "Camera 1");
            truckNumberFieldCamera1.clear(); // Clear the text field after saving
        }
    }

    public void getTruckNumberFromCamera2() {
        String truckNumber = truckNumberFieldCamera2.getText();
        if (!truckNumber.isEmpty()) {
            // Save the truck number to the database
            saveTruckNumberToDatabase(truckNumber, "Camera 2");
            truckNumberFieldCamera2.clear(); // Clear the text field after saving
        }
    }

    private void saveTruckNumberToDatabase(String truckNumber, String cameraSource) {
        // Implement the database save logic here (this might involve calling a service or repository)
        System.out.println("Saving truck number: " + truckNumber + " from " + cameraSource);
        // Example of saving to the database
        // truckService.saveTruckNumber(truckNumber, cameraSource);
    }

    public void setWeigh() {
        double weigh = Double.parseDouble(weighInputField.getText());
    }

    private void updateTruckPosition() {
        if (isTesting) {
            truckPositionField.setText(String.valueOf(0)); // Example static position
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

    // Gate 1 status handler
    public void setGate1StatusSwitch() {
        if (isTesting) {
            gate1Connection = gate1Switch.isSelected(); // Update gate status manually when testing is on
            truckPosition = 0; // Update position or any other logic as needed
        }
    }

    // Gate 2 status handler
    public void setGate2StatusSwitch() {
        if (isTesting) {
            gate2Connection = gate2Switch.isSelected(); // Update gate status manually when testing is on
            truckPosition = 7; // Update position or any other logic as needed
        }
    }


    public void start() {
        executors.execute(() -> {
            while (true) {
                try {
                    if (!isTesting) {
                        // Disable and reset all switches when testing is off
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

                        weighInputField.setDisable(true);
                        sensor1Switch.setDisable(true);
                        sensor2Switch.setDisable(true);
                        sensor3Switch.setDisable(true);
                        gate1Switch.setDisable(true); // Disable Gate 1
                        gate2Switch.setDisable(true); // Disable Gate 2
                    } else {
                        // Keep sensor and weigh fields enabled during testing
                        sensor1Connection = sensor1Switch.isSelected();
                        sensor2Connection = sensor2Switch.isSelected();
                        sensor3Connection = sensor3Switch.isSelected();
                        testStatusSwitch.setSelected(true);
                        truckPositionField.setText("" + truckPosition);

                        weighInputField.setDisable(false);
                        sensor1Switch.setDisable(false);
                        sensor2Switch.setDisable(false);
                        sensor3Switch.setDisable(false);

                        // Gates should remain enabled for manual control
                        gate1Switch.setDisable(false); // Enable Gate 1 for manual control
                        gate2Switch.setDisable(false); // Enable Gate 2 for manual control
                    }

                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        });
    }

}
