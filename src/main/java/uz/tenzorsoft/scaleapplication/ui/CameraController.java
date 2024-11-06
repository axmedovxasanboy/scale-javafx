package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CameraController {

    @FXML
    private ImageView camera1;

    @FXML
    private ImageView camera2;

    @FXML
    private ImageView camera3;


    private void loadCameraMenu() {

    }
}
