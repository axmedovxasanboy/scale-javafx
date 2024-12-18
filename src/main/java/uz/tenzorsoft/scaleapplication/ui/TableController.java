package uz.tenzorsoft.scaleapplication.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.PrintCheck;
import uz.tenzorsoft.scaleapplication.service.TableService;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
public class TableController implements BaseController {

    private final TruckService truckService;
    private final TableService tableService;
    private final ExecutorService executors;
    private final PrintCheck printCheck;
    private final LogService logService;

    @Autowired
    @Lazy
    private ControlPane controlPane;
    @Autowired
    @Lazy
    private ImageController imageController;

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

    @FXML
    private TableColumn<TableViewData, String> productType;


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

        productType.setCellValueFactory(new PropertyValueFactory<>("ProductType"));

        tableData.setRowFactory(tv -> {
            TableRow<TableViewData> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    printCheckAndSetDeleteButton(row);
                    editTruckNumberButtonVisibility(row);
                }
            });
            return row;
        });
    }

    private void editTruckNumberButtonVisibility(TableRow<TableViewData> row) {
        TruckEntity truck = truckService.findById(row.getItem().getId());
        if(truck == null){
            showAlert(Alert.AlertType.ERROR, "Xatolik", "Moshina topilmadi");
            return;
        }
        Button editButton = controlPane.getEditButton();

        if (!truck.getIsFinished()) {
            editButton.setDisable(false);
            editButton.setVisible(true);
            editButton.setOnAction(e -> {
                truckService.save(truck);
                loadData();
            });
            controlPane.setEditButton(editButton);
        } else {
            editButton.setDisable(true);
            editButton.setVisible(false);
            controlPane.setEditButton(editButton);
        }
        editButton.setOnMouseClicked(e -> {
            openEditPopup(truck);
            row.setItem(tableService.entityToTableData(truck));
        });
    }

    private void openEditPopup(TruckEntity truck) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Truck Information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField truckNumberField = new TextField(truck.getTruckNumber());
        truckNumberField.setPromptText("Truck Number");

        grid.add(new Label("Truck Number:"), 0, 0);
        grid.add(truckNumberField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("O'zgartirish", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Bekor qilish", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                truck.setOriginalTruckNumber(truck.getTruckNumber());
                truck.setTruckNumber(truckNumberField.getText());
                truckService.saveTruck(truck);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void printCheckAndSetDeleteButton(TableRow<TableViewData> row) {
        controlPane.getIssueCheckButton().setDisable(false);
        TableViewData rowItem = row.getItem();
        TruckEntity truck = truckService.findById(rowItem.getId());
        controlPane.getIssueCheckButton().setOnMouseClicked(e -> {
            printCheck.printReceipt(truck);
            controlPane.getIssueCheckButton().setDisable(true);
        });
        Button deleteButton = controlPane.getDeleteButton();
        if (!truck.getIsFinished()) {
            deleteButton.setDisable(false);
            deleteButton.setVisible(true);
            deleteButton.setOnAction(e -> {
                truck.setIsDeleted(true);
                truckService.save(truck);
                loadData();
            });
            controlPane.setDeleteButton(deleteButton);
        } else {
            deleteButton.setDisable(true);
            deleteButton.setVisible(false);
            controlPane.setDeleteButton(deleteButton);
        }
        imageController.showImages(rowItem); // Show images based on the selected row data
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
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00034: (" + getClass().getName() + ") " + e.getMessage()));
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                }
            }
        });

    }


    public void addLastRecord() {
        TableViewData record = truckService.findLastRecord();
        if (record == null) return;
        tableData.getItems().add(0, record);
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

    public void loadFilter(LocalDate startDateValue, LocalDate endDateValue) {
        try {
            List<TableViewData> data = new ArrayList<>();
            List<TruckEntity> filteredData = truckService.filterWithDate(startDateValue, endDateValue);
            for (TruckEntity truck : filteredData) {
                data.add(tableService.entityToTableData(truck));
            }
            tableData.setItems(FXCollections.observableArrayList(data));
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, "00049: (" + getClass().getName() + ") " + e.getMessage()));
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

}
