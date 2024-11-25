package uz.tenzorsoft.scaleapplication.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.ProductService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isConnected;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;

@Component
@RequiredArgsConstructor
public class ControlPane {
    private final ExecutorService executors;
    private final LogService logService;
    private final MainController mainController;
    private final TableController tableController;
    private final ButtonController buttonController;
    private final ProductService productService;
    @FXML
    private Button connectButton;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    @FXML
    private ComboBox<String> productCombobox;

    private ObservableList<String> products;

    @FXML
    @Getter
    @Setter
    private Button issueCheckButton, deleteButton;

    @FXML
    public void initialize() {
        StringConverter<LocalDate> dateStringConverter = new StringConverter<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        };

        startDate.setConverter(dateStringConverter);
        endDate.setConverter(dateStringConverter);
        startDate.setValue(LocalDate.now());
        endDate.setValue(LocalDate.now());
        // Initialize product list
        products = FXCollections.observableArrayList(productService.getAllProducts());
        products.add("Add Product");
        productCombobox.setItems(products);

        // Handle selection
        productCombobox.setOnAction(event -> handleSelection(productCombobox.getValue()));

    }

    private void handleSelection(String selected) {
        if ("Add Product".equals(selected)) {
            addNewProduct();
        } else if (selected != null) {
            mainController.showAlert(Alert.AlertType.INFORMATION, "Information", "selected: " + selected);
        }
    }

    private void addNewProduct() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Product");
        dialog.setHeaderText("Add a New Product");
        dialog.setContentText("Enter the product name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(productName -> {
            if (!productName.trim().isEmpty()) {
                products.add(products.size() - 1, productService.saveProduct(productName).getName()); // Add before "Add Product"
                mainController.showAlert(Alert.AlertType.INFORMATION, "Product added", "Product added: " + productName);
            } else {
                mainController.showAlert(Alert.AlertType.ERROR, "Error", "Product name cannot be empty!");
            }
        });
    }

    public void controlConnectButton() {
        executors.execute(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> {
                        if (!isConnected) {
                            connectButton.setText("Connect");
                            connectButton.getStyleClass().removeAll("connect-button-disconnected");
                            connectButton.getStyleClass().add("connect-button");
                        } else {
                            connectButton.setText("Disconnect");
                            connectButton.getStyleClass().removeAll("connect-button");
                            connectButton.getStyleClass().add("connect-button-disconnected");
                        }
                    });
                    Thread.sleep(1000);
                } catch (Exception e) {
                    logService.save(new LogEntity(5L, Instances.truckNumber, "00033: (" + getClass().getName() + ") " + e.getMessage()));
                    Platform.runLater(() -> mainController.showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()));
                }
            }
        });
    }

    @FXML
    private void getFilteredData() {
        LocalDate startDateValue = startDate.getValue();
        LocalDate endDateValue = endDate.getValue();

        if (startDateValue != null && startDateValue.isAfter(LocalDate.now())) {
            startDateValue = LocalDate.now();
            startDate.setValue(startDateValue);
        }
        if (endDateValue != null && endDateValue.isAfter(LocalDate.now())) {
            endDateValue = LocalDate.now();
            endDate.setValue(endDateValue);
        }
        tableController.loadFilter(startDateValue, endDateValue);

    }

    @FXML
    private void connectToController() {
        if (isConnected) {
            buttonController.disconnect();
            truckPosition = -1;
        } else {
            buttonController.connect();
        }
    }

}
