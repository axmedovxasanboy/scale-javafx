package uz.tenzorsoft.scaleapplication.ui;

import javafx.scene.control.Alert;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.entity.AttachEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;

import java.util.Base64;

import static uz.tenzorsoft.scaleapplication.domain.Instances.currentTruck;
import static uz.tenzorsoft.scaleapplication.domain.Instances.isTesting;
import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;

@Component
//@RequiredArgsConstructor
public class CameraViewController {
    private final AttachService attachService;

    public CameraViewController(AttachService attachService) {
        this.attachService = attachService;
    }

//    @FXML
//    private ImageView camera1;
//
//    @FXML
//    private ImageView camera2;
//
//    @FXML
//    private ImageView camera3;
//

    private void loadCameraMenu() {

    }

    public AttachResponse takePicture(String cameraIpAddress) {
        String username = "admin";
        String password = "Joe@252544";
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);

        try {
            if(isTesting){
                return attachService.getTestingImages();
            }
            String url = "http://" + cameraIpAddress + "/ISAPI/Streaming/channels/1/picture";
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                byte[] fileBytes = response.getBody();
                if (fileBytes != null && currentTruck.getTruckNumber() != null) {
                    return attachService.saveToSystem(fileBytes);
                }
            } else {
                System.out.println("Failed to get snapshot, status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error while taking picture", e.getMessage());
        }
        return null;
    }
}
