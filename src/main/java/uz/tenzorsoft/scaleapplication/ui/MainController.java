package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static uz.tenzorsoft.scaleapplication.domain.Instances.currentUser;

@Component
@RequiredArgsConstructor
public class MainController {

    private final FXMLLoader fxmlLoader;

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
    private Text changePasswordText;


    @FXML
    public void initialize() {
        setupButtonPressEffect(button1, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button2, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button3, "#ebed72", "#e3e360");
        setupButtonPressEffect(button4, "#f56e6e", "#e46464");
        setupButtonPressEffect(button5, "#f56e6e", "#e46464");

        // Handle click on "Parolni almashtirish"
        changePasswordText.setOnMouseClicked(event -> openPasswordChangeDialog());
    }

    private void openPasswordChangeDialog() {
        // Create a new dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Parolni almashtirish");

        // Step 1: Phone Number Input Field
        Label phoneLabel = new Label("Telefon nomerini kiriting:");
        TextField phoneInput = new TextField();
        phoneInput.setPromptText("Telefon nomer");
        phoneInput.setPrefWidth(150);  // Reduced width

        Button sendSmsButton = new Button("SMS Yuborish");
        sendSmsButton.getStyleClass().add("primary-button");  // Add Bootstrap-like style

        // Step 2: SMS Code Input Field (initially hidden)
        Label smsLabel = new Label("SMS kodini kiriting:");
        smsLabel.setVisible(false);
        TextField smsInput = new TextField();
        smsInput.setPromptText("SMS kod");
        smsInput.setPrefWidth(150);  // Reduced width
        smsInput.setVisible(false);

        Button confirmSmsButton = new Button("Confirm");
        confirmSmsButton.setVisible(false);
        confirmSmsButton.getStyleClass().add("primary-button");  // Add Bootstrap-like style

        // Step 3: New Password Fields (initially hidden)
        Label newPasswordLabel = new Label("Yangi parol:");
        newPasswordLabel.setVisible(false);
        PasswordField newPasswordInput = new PasswordField();
        newPasswordInput.setPromptText("Yangi parol");
        newPasswordInput.setVisible(false);

        Label confirmPasswordLabel = new Label("Parolni qayta kiriting:");
        confirmPasswordLabel.setVisible(false);
        PasswordField confirmPasswordInput = new PasswordField();
        confirmPasswordInput.setPromptText("Parolni qayta kiriting");
        confirmPasswordInput.setVisible(false);

        Button confirmPasswordButton = new Button("Tasdiqlash");
        confirmPasswordButton.setVisible(false);
        confirmPasswordButton.getStyleClass().add("primary-button");  // Add Bootstrap-like style

        // Layout for dialog components
        VBox dialogVbox = new VBox(10);
        dialogVbox.getChildren().addAll(phoneLabel, phoneInput, sendSmsButton, smsLabel, smsInput, confirmSmsButton, newPasswordLabel, newPasswordInput, confirmPasswordLabel, confirmPasswordInput, confirmPasswordButton);

        Scene dialogScene = new Scene(dialogVbox, 300, 400);
        dialog.setScene(dialogScene);

        // Step 1: Send SMS Button Action
        sendSmsButton.setOnAction(e -> {
            // Simulate sending SMS with the default code "11111"
            String smsCode = "11111"; // Temporary code
            saveSmsCodeToDatabase(smsCode);

            smsLabel.setVisible(true);
            smsInput.setVisible(true);
            confirmSmsButton.setVisible(true);
        });

        // Step 2: Confirm SMS Button Action
        confirmSmsButton.setOnAction(e -> {
            String smsCode = smsInput.getText();

            // Check SMS code (default code "11111")
            if ("11111".equals(smsCode)) {  // Temporary code for testing
                newPasswordLabel.setVisible(true);
                newPasswordInput.setVisible(true);
                confirmPasswordLabel.setVisible(true);
                confirmPasswordInput.setVisible(true);
                confirmPasswordButton.setVisible(true);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid SMS code, please try again.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // Step 3: Confirm Password Button Action
        confirmPasswordButton.setOnAction(e -> {
            String newPassword = newPasswordInput.getText();
            String confirmPassword = confirmPasswordInput.getText();

            if (newPassword.equals(confirmPassword)) {
                // Update password in the backend (store it in DB)
                System.out.println("Password successfully changed!");
                dialog.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Passwords do not match.", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // Validation for Required Fields
        sendSmsButton.setOnAction(e -> {
            if (phoneInput.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Telefon nomeri talab qilinadi.", ButtonType.OK).showAndWait();
                return;
            }
            // Continue with sending SMS if phone number is provided
            smsLabel.setVisible(true);
            smsInput.setVisible(true);
            confirmSmsButton.setVisible(true);
        });

        // Show the dialog
        dialog.showAndWait();
    }

    private void saveSmsCodeToDatabase(String smsCode) {
        // Temporary method to simulate storing the SMS code in the database
        System.out.println("SMS code stored in DB: " + smsCode);
    }

    private void setupButtonPressEffect(Button button, String normalColor, String pressedColor) {
        button.setOnMousePressed(event -> button.setStyle("-fx-background-color: " + pressedColor + "; -fx-border-color: black; -fx-border-radius: 7; -fx-background-radius: 10; -fx-border-width: 0.8;"));
        button.setOnMouseReleased(event -> button.setStyle("-fx-background-color: " + normalColor + "; -fx-border-color: black; -fx-border-radius: 7; -fx-background-radius: 10; -fx-border-width: 0.8;"));
    }

    public void load() {
        try {
            loadMainMenu();
            loadCameraManu();
            loadUserMenu();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private void loadCameraManu() {


    }

    private void loadUserMenu() {
        username.setText(currentUser.getUsername());
        phoneNumber.setText(currentUser.getPhoneNumber());
    }


}
