package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.response.UserResponse;
import uz.tenzorsoft.scaleapplication.service.CloudService;
import uz.tenzorsoft.scaleapplication.service.UserService;

@Component
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final MainController mainController;
    private final CloudService cloudService;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private void handleLogin() {
        String phoneNumber = phoneNumberField.getText();
        String password = passwordField.getText();

        UserResponse userResponse = userService.validateUser(phoneNumber, password);

        if (userResponse != null) {
            mainController.load();
            Stage currentStage = (Stage) anchorPane.getScene().getWindow();
            currentStage.close();
        } else {

            if (cloudService.login(phoneNumber, password)) {
                Stage dialog = new Stage();
                dialog.setTitle("SMS Verification");
                dialog.setScene(new Scene(anchorPane));
                dialog.setResizable(false);

                Label smsCodeLabel = new Label("Telefonga borgan SMS kodni kiriting:");
                TextField smsInput = new TextField();
                smsInput.setPromptText("SMS kod: ");
                smsInput.setPrefWidth(200);
                smsInput.setMaxWidth(250);


                Button checkSMSCode = new Button("SMS kodni tekshirish");

                VBox dialogVbox = new VBox(10);
                dialogVbox.setAlignment(Pos.CENTER);  // Center all elements
                dialogVbox.setPadding(new Insets(20)); // Add padding to prevent elements from sticking to the edges
                dialogVbox.getChildren().addAll(smsCodeLabel, smsInput, checkSMSCode);
                Scene dialogScene = new Scene(dialogVbox, 300, 400);
                dialog.setScene(dialogScene);

                checkSMSCode.setOnAction(e -> {
                    String verify = cloudService.verify(phoneNumber, password, smsInput.getText());
                    if (!verify.isEmpty() && !verify.equals("INCORRECT_PASSWORD")) {
                        userService.create(phoneNumber, password);
                    }
                });
            }

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText(null);
            alert.setContentText("Invalid phone number or password.");
            alert.showAndWait();
        }

    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) phoneNumberField.getScene().getWindow();
        stage.close();
    }

    public static void showAlert(Alert.AlertType alertType, String headerText, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(headerText);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
