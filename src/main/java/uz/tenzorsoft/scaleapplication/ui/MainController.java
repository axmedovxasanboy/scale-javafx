package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
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
    public void initialize() {
        setupButtonPressEffect(button1, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button2, "#b2f5a9", "#a8e69f");
        setupButtonPressEffect(button3, "#ebed72", "#e3e360");
        setupButtonPressEffect(button4, "#f56e6e", "#e46464");
        setupButtonPressEffect(button5, "#f56e6e", "#e46464");
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
