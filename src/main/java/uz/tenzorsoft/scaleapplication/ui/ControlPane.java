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
import uz.tenzorsoft.scaleapplication.domain.entity.ProductsEntity;
import uz.tenzorsoft.scaleapplication.service.LogService;
import uz.tenzorsoft.scaleapplication.service.ProductService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static uz.tenzorsoft.scaleapplication.domain.Instances.isAvailableToConnect;
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
        products = FXCollections.observableArrayList(productService.getAllProducts());

        // Check if there are any products available
        if (!products.isEmpty()) {
            // Set the selected product if one exists and is not deleted
            ProductsEntity selectedProduct = productService.getSelectedProduct();
            if (selectedProduct != null && selectedProduct.getIsSelected() && !selectedProduct.getIsDeleted()) {
                productCombobox.setValue(selectedProduct.getName());  // Set selected product
            } else {
                showNoProductsPopup(); // If no product is selected, show the pop-up
            }
        } else {
            showNoProductsPopup(); // If there are no products, show the pop-up
        }

        // Initialize product list
        products.add("Mahsulot qo'shish");
        productCombobox.setItems(products);

// Apply a custom cell factory for styling
        productCombobox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Apply special style for "Mahsulot qo'shish"
                    if ("Mahsulot qo'shish".equals(item)) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-background-color: #f0f8ff;");
                    } else {
                        setStyle(""); // Default style for other items
                    }
                }
            }
        });

// Ensure the selected item in the ComboBox also uses the custom style
        productCombobox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Mahsulot qo'shish".equals(item)) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-background-color: #f0f8ff;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Handle selection
        productCombobox.setOnAction(event -> handleSelection(productCombobox.getValue()));

    }

    private void handleSelection(String selected) {
        if ("Mahsulot qo'shish".equals(selected)) {
            addNewProduct();
        } else if (selected == null || selected.trim().isEmpty()) {
            showNoProductsPopup();
        } else {
            // Update the selected product
            productService.updateSelectedProduct(selected);
        }
    }

    private void showNoProductsPopup() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Mahsulotlar mavjud emas");
        alert.setHeaderText("Hech qanday mahsulot qo'shilmagan.");
        alert.setContentText("Davom etish uchun mahsulot qo‘shing.");

        // Add custom buttons
        ButtonType addProductButton = new ButtonType("Mahsulot qo'shish");
        ButtonType cancelButton = ButtonType.CLOSE; // Default close button

        alert.getButtonTypes().setAll(addProductButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == addProductButton) {
                addNewProduct();
            } else if (result.get() == cancelButton) {
                closeProgram(); // Close the program if "Cancel" is clicked
            }
        }
    }

    private void closeProgram() {
        // Close the program gracefully
        Platform.exit(); // Exits JavaFX application
        System.exit(0);  // Terminates JVM
    }

    private void addNewProduct() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mahsulot qo'shish");
        dialog.setHeaderText("Yangi mahsulot qo'shing");
        dialog.setContentText("Mahsulot nomini kiriting:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(productName -> {
            if (!productName.trim().isEmpty()) {
                // Save the new product and mark it as selected
                ProductsEntity newProduct = productService.saveProduct(productName);
                productService.updateSelectedProduct(newProduct.getName()); // Mark as selected

                // Refresh the product list
                products.add(products.size() - 1, newProduct.getName()); // Add before "Mahsulot qo'shish"
                productCombobox.setValue(newProduct.getName()); // Automatically select the new product

                // First alert for product added
                mainController.showAlert(Alert.AlertType.INFORMATION, "Mahsulot qo'shildi", "Mahsulot qo'shildi: " + productName);

                // Second alert for product selected
                mainController.showAlert(Alert.AlertType.INFORMATION, "Ma'lumot", "Tanlandi: " + newProduct.getName());
            } else {
                mainController.showAlert(Alert.AlertType.ERROR, "Xatolik", "Mahsulot nomi bo'sh bo'lishi mumkin emas!");
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
