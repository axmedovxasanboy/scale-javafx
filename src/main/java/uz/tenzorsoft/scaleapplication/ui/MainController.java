package uz.tenzorsoft.scaleapplication.ui;

import com.ghgande.j2mod.modbus.ModbusException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.service.ControllerService;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static uz.tenzorsoft.scaleapplication.domain.Instances.currentUser;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.*;

@Component
@RequiredArgsConstructor
public class MainController {
    private final FXMLLoader fxmlLoader;
    private final ExecutorService executors;
    private final ControllerService controllerService;
    private final TruckService truckService;

    @FXML
    private Pane scaleAutomationPane;

    @FXML
    private Tab cameraTab;

    @FXML
    private Tab userTab;

    @FXML
    private TextField username;

    @FXML
    private TextField phoneNumber;

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

    @FXML
    private ImageView controller;

    @FXML
    private ImageView camera1;

    @FXML
    private ImageView camera2;

    @FXML
    private ImageView camera3;

    @FXML
    private ImageView gate1;

    @FXML
    private ImageView gate2;

    @FXML
    private ImageView sensor1;

    @FXML
    private ImageView sensor2;

    @FXML
    private ImageView sensor3;

    @FXML
    private TableView<TableViewData> tableData;

    @FXML
    private TableColumn<TableViewData, Long> id;

    @FXML
    private TableColumn<TableViewData, String> enteredTruckNumber;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> enteredDate;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> enteredTime;

    @FXML
    private TableColumn<TableViewData, Double> enteredWeight;

    @FXML
    private TableColumn<TableViewData, String> enteredOnDuty;

    @FXML
    private TableColumn<TableViewData, String> exitedTruckNumber;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> exitedDate;

    @FXML
    private TableColumn<TableViewData, LocalDateTime> exitedTime;

    @FXML
    private TableColumn<TableViewData, Double> exitedWeight;

    @FXML
    private TableColumn<TableViewData, String> exitedOnDuty;

    @FXML
    public void initialize() {
        setupButtonPressEffect(button1, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button2, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button3, "#ebed72", "#e3e360");
        setupButtonPressEffect(button4, "#f56e6e", "#e46464");
        setupButtonPressEffect(button5, "#f56e6e", "#e46464");


        id.setCellValueFactory(new PropertyValueFactory<>("Id"));
        enteredTruckNumber.setCellValueFactory(new PropertyValueFactory<>("EnteredTruckNumber"));
        enteredDate.setCellValueFactory(new PropertyValueFactory<>("EnteredDate"));
        enteredTime.setCellValueFactory(new PropertyValueFactory<>("EnteredTime"));
        enteredWeight.setCellValueFactory(new PropertyValueFactory<>("EnteredWeight"));
        enteredOnDuty.setCellValueFactory(new PropertyValueFactory<>("EnteredOnDuty"));

        exitedTruckNumber.setCellValueFactory(new PropertyValueFactory<>("ExitedTruckNumber"));
        exitedDate.setCellValueFactory(new PropertyValueFactory<>("ExitedDate"));
        exitedTime.setCellValueFactory(new PropertyValueFactory<>("ExitedTime"));
        exitedWeight.setCellValueFactory(new PropertyValueFactory<>("ExitedWeight"));
        exitedOnDuty.setCellValueFactory(new PropertyValueFactory<>("ExitedOnDuty"));


    }

    private void setupButtonPressEffect(Button button, String normalColor, String pressedColor) {
        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: " + pressedColor + "; -fx-border-color: black; -fx-border-radius: 7; -fx-background-radius: 10; -fx-border-width: 0.8;"));
        button.setOnMouseReleased(event -> button.setStyle("-fx-background-color: " + normalColor + "; -fx-border-color: black; -fx-border-radius: 7; -fx-background-radius: 10; -fx-border-width: 0.8;"));
    }

    public void load() {
        try {
            loadMainMenu();
            loadCameraMenu();
            loadUserMenu();
            executors.execute(() -> {
                try {
                    updateConnections();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            executors.execute(this::loadTableData);
            AtomicInteger helper = new AtomicInteger(1);
            executors.execute(() -> {
                        while (true) {
                            TruckEntity truck = truckService.save(
                                    new TruckEntity(
                                            "01A77" + helper + "AA",
                                            new TruckActionEntity(
                                                    100.0 + helper.get(), TruckAction.ENTRANCE, currentUser
                                            ), null
                                    ));
                            helper.getAndIncrement();
                            addTableRow(truck);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
            );

        } catch (IOException | RuntimeException e) {
            showAlert(Alert.AlertType.WARNING, "Warning", e.getMessage());
        }
    }

    private void addTableRow(TruckEntity truck) {
        TableViewData tableData = new TableViewData();
        tableData.setId(truck.getId());

        TruckActionEntity truckAction = truck.getTruckAction();
        switch (truckAction.getAction()) {
            case ENTRANCE, MANUAL_ENTRANCE -> {
                tableData.setEnteredTruckNumber(truck.getTruckNumber());
                tableData.setEnteredDate(truckService.getDate(truckAction.getCreatedAt()));
                tableData.setEnteredTime(truckService.getTime(truckAction.getCreatedAt()));
                tableData.setEnteredWeight(truckAction.getWeight());
                tableData.setEnteredOnDuty(truckAction.getOnDuty().getUsername());
            }
            case MANUAL_EXIT, EXIT -> {
                tableData.setExitedTruckNumber(truck.getTruckNumber());
                tableData.setExitedDate(truckService.getDate(truckAction.getCreatedAt()));
                tableData.setExitedTime(truckService.getTime(truckAction.getCreatedAt()));
                tableData.setExitedWeight(truckAction.getWeight());
                tableData.setExitedOnDuty(truckAction.getOnDuty().getUsername());
            }
        }
        this.tableData.getItems().add(tableData);
    }

    private void loadMainMenu() throws IOException {
        fxmlLoader.setLocation(getClass().getResource("/fxml/main.fxml"));
        Parent root = fxmlLoader.load();

        ToggleSwitch toggleSwitch = new ToggleSwitch("");
        toggleSwitch.setSelected(true);

        Label label = new Label("Massani avtomatik tarzda olish");
        HBox toggleWithLabel = new HBox(5, toggleSwitch, label);

        toggleWithLabel.setAlignment(Pos.CENTER);

        scaleAutomationPane.getChildren().clear();
        scaleAutomationPane.getChildren().add(toggleWithLabel);

        toggleWithLabel.layoutXProperty().bind(scaleAutomationPane.widthProperty().subtract(toggleWithLabel.widthProperty()).divide(2));
        toggleWithLabel.layoutYProperty().bind(scaleAutomationPane.heightProperty().subtract(toggleWithLabel.heightProperty()).divide(2));

        Stage mainWindowStage = new Stage();
        mainWindowStage.setTitle("Main Window");
        mainWindowStage.setScene(new Scene(root));

        mainWindowStage.show();
    }

    private void loadCameraMenu() {


    }

    private void loadUserMenu() {
        username.setText(currentUser.getUsername());
        phoneNumber.setText(currentUser.getPhoneNumber());
    }

    private void updateConnections() throws InterruptedException {
        while (true) {
            try {
                controller.setImage(isConnected ? greenLight : redLight);
                gate1.setImage(controllerService.checkConnection(COIL_CLOSE_GATE_1) ? greenLight : redLight);
                gate2.setImage(controllerService.checkConnection(COIL_CLOSE_GATE_2) ? greenLight : redLight);
                sensor1.setImage(controllerService.checkConnection(COIL_SENSOR_1) ? greenLight : redLight);
                sensor2.setImage(controllerService.checkConnection(COIL_SENSOR_2) ? greenLight : redLight);
                sensor3.setImage(controllerService.checkConnection(COIL_SENSOR_3) ? greenLight : redLight);
                camera1.setImage(controllerService.checkConnection(CAMERA_1) ? greenLight : redLight);
                camera2.setImage(controllerService.checkConnection(CAMERA_2) ? greenLight : redLight);
                camera3.setImage(Math.random() > 0.5 ? greenLight : redLight);
//                camera3.setImage(controllerService.checkConnection(CAMERA_3) ? greenLight : redLight);
            } catch (ModbusException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
            Thread.sleep(500);
        }
    }

    private void loadTableData() {
        truckService.save(new TruckEntity(
                "01A777AA", new TruckActionEntity(
                100.0, TruckAction.ENTRANCE, currentUser
        ), null
        ));
        List<TableViewData> collection = truckService.getTruckData();
        tableData.setItems(FXCollections.observableArrayList(
                collection
        ));
    }

    public void openGate1() {
        try {
            controllerService.openGate1();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void closeGate1() {
        try {
            controllerService.closeGate1();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void openGate2() {
        try {
            controllerService.openGate2();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    public void closeGate2() {
        try {
            controllerService.closeGate2();
        } catch (ModbusException e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private static void showAlert(Alert.AlertType alertType, String headerText, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(headerText);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
