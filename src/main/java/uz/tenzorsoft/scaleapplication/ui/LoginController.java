package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.response.UserResponse;
import uz.tenzorsoft.scaleapplication.service.UserService;

@Component
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;
    private final MainController mainController;

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
}
