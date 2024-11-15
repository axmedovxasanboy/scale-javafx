package uz.tenzorsoft.scaleapplication.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;
import uz.tenzorsoft.scaleapplication.service.TableService;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
public class TableController {

    private final TruckService truckService;
    private final TableService tableService;
    private final ExecutorService executors;
    private final ImageController imageController;
    @Autowired
    @Lazy
    private MainController mainController;
    private final PrintCheck printCheck;
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
    private TableColumn<TableViewData, String> minWeight;

    @FXML
    private TableColumn<TableViewData, String> maxWeight;

    @FXML
    private TableColumn<TableViewData, String> pickupWeight;

    @FXML
    private TableColumn<TableViewData, String> dropWeight;
    @Autowired
    private LogService logService;


    @FXML
    public void initialize() {
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

        minWeight.setCellValueFactory(new PropertyValueFactory<>("MinWeight"));
        maxWeight.setCellValueFactory(new PropertyValueFactory<>("MaxWeight"));
        pickupWeight.setCellValueFactory(new PropertyValueFactory<>("PickupWeight"));
        dropWeight.setCellValueFactory(new PropertyValueFactory<>("DropWeight"));

        tableData.setRowFactory(tv -> {
            TableRow<TableViewData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    mainController.getIssueCheckButton().setDisable(false);
                    TableViewData rowItem = row.getItem();
                    mainController.getIssueCheckButton().setOnMouseClicked(e->{
                        printCheck.printReceipt(truckService.findById(rowItem.getId()));
                        mainController.getIssueCheckButton().setDisable(true);
                    });
                    imageController.showImages(rowItem); // Show images based on the selected row data
                }
            });
            return row;
        });
    }

    public void loadData() {
        executors.execute(() -> {
            while (true) {
                try {
                    List<TableViewData> data = new ArrayList<>();
                    List<TruckEntity> all = truckService.findAll();
                    for (TruckEntity truck : all) {
                        data.add(tableService.entityToTableData(truck));
                    }
                    tableData.setItems(FXCollections.observableArrayList(data));

                    Thread.sleep(60000);
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
//                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });

    }


    public void addLastRecord() {
        TableViewData record = truckService.findLastRecord();
        if (record == null) return;
        tableData.getItems().add(record);
    }

    public void updateTableRow(TruckEntity truckEntity) {
        if (truckEntity == null) return;
        ObservableList<TableViewData> items = tableData.getItems();
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(truckEntity.getId())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            TableViewData record = truckService.entityToTableViewData(truckEntity);
            items.set(index, record);
        }
    }

}
