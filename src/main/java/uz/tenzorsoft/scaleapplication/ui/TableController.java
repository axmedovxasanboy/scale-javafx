package uz.tenzorsoft.scaleapplication.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import uz.tenzorsoft.scaleapplication.service.TableService;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;

@Component
@RequiredArgsConstructor
public class TableController {

    private final TruckService truckService;
    private final TableService tableService;
    private final ExecutorService executors;
    private final ImageController imageController;
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

        tableData.setRowFactory(tv -> {
            TableRow<TableViewData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    TableViewData rowData = row.getItem();
                    imageController.showImages(rowData); // Show images based on the selected row data
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

                    Thread.sleep(60000 * 5);
                } catch (Exception e) {
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

    public void addLastRecord(TruckResponse truckResponse) {
        if (truckResponse == null) return;

        TableViewData record = convertToTableViewData(truckResponse);
        if (record != null) {
            tableData.getItems().add(record);
        }
    }

    private TableViewData convertToTableViewData(TruckResponse truckResponse) {
        TableViewData data = new TableViewData();

        data.setId(truckResponse.getId());
        data.setEnteredTruckNumber(truckResponse.getTruckNumber());

        // Map entrance details
        if (truckResponse.getEnteredAt() != null) {
            data.setEnteredDate(truckResponse.getEnteredAt().toLocalDate().toString());
            data.setEnteredTime(truckResponse.getEnteredAt().toLocalTime().toString());
        }
        data.setEnteredWeight(truckResponse.getEnteredWeight());
        data.setEnteredOnDuty(truckResponse.getEntranceConfirmedBy());

        // Map exit details
        if (truckResponse.getExitedAt() != null) {
            data.setExitedDate(truckResponse.getExitedAt().toLocalDate().toString());
            data.setExitedTime(truckResponse.getExitedAt().toLocalTime().toString());
        }
        data.setExitedWeight(truckResponse.getExitedWeight());
        data.setExitedOnDuty(truckResponse.getExitConfirmedBy());

        return data;
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

    public void updateTableRow(TruckResponse truckResponse) {
        ObservableList<TableViewData> items = tableData.getItems();
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(truckResponse.getId())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            TableViewData record = truckService.getTableViewData(truckResponse);
            items.set(index, record);
        }
    }

}
