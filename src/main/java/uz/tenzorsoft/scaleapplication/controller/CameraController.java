package uz.tenzorsoft.scaleapplication.controller;

import jakarta.servlet.http.HttpServletRequest;
import javafx.scene.control.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.TruckPhotoService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;
import uz.tenzorsoft.scaleapplication.ui.MainController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;
import static uz.tenzorsoft.scaleapplication.service.ScaleSystem.truckPosition;
import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CameraController {

    private final AttachService attachService;
    private final TableController tableController;
    private final ButtonController buttonController;
    private final MainController mainController;
    private final TruckService truckService;
    private final TruckPhotoService truckPhotoService;

    @Value("${number.regex.pattern}")
    private String regexPattern;

    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(HttpServletRequest request, @PathVariable("id") Integer id) {
        if (request instanceof MultipartHttpServletRequest multipartRequest) {

            if (multipartRequest.getFileMap().size() < 4 || truckPosition != -1 || isWaiting || currentUser.getId() == null) {
                return ResponseEntity.ok("NOT_MATCH");
            }
            for (Map.Entry<String, MultipartFile> entry : multipartRequest.getFileMap().entrySet()) {
                String fileName = entry.getKey();
                MultipartFile file = entry.getValue();

                log.info("Processing file: {}", fileName);

                if (file.isEmpty()) {
                    log.warn("File is empty: {}", fileName);
                    continue;
                }

                try {
                    if (fileName.equals("detectionPicture.jpg")) {
                        if (!saveFile(file, id)) {
                            log.warn("See logs for error cause. Unable to save file: {}", fileName);
                            return ResponseEntity.ok("Unable to save file");
                        }
                    } else {
                        attachService.saveToSystem(file);
                    }
                    log.info("File processed: {}", fileName);
                } catch (IOException e) {
                    log.warn("File processing failed: {}", fileName);
                    log.error(e.getMessage(), e);
                    return ResponseEntity.status(500).body("Failed to save file: " + file.getOriginalFilename());
                }
            }
            if (id == 1) tableController.addLastRecord();

            if (id == 1) buttonController.openGate1(0);
            else if (id == 2) buttonController.openGate2(7);

            return ResponseEntity.ok("Files uploaded and saved successfully.");
        }
        log.warn("Request is not a multipart request");
        return ResponseEntity.badRequest().body("Request is not a multipart request");
    }

    private boolean saveFile(MultipartFile file, Integer id) throws IOException {
        AttachResponse attach;
        if (file.getContentType() != null && file.getContentType().equals("text/xml")) {
            truckNumber = extractTagFromXmlFile(file, "originalLicensePlate");
            Pattern pattern = Pattern.compile(regexPattern);
            Matcher matcher = pattern.matcher(truckNumber);
            if (!matcher.find()) {
                showAlert(Alert.AlertType.WARNING, "Not match", "Truck number does not match: " + truckNumber);
                log.warn("Truck number does not match: {}", truckNumber);
                truckNumber = "";
                return false;
            }

            if (id == 2) {
                try {
                    TruckEntity enteredByTruckNumber = truckService.findEnteredByTruckNumber(truckNumber);
                    System.out.println("truckNumber = " + truckNumber);
                    System.out.println("entered truck = " + enteredByTruckNumber);
                } catch (RuntimeException e) {
                    List<TruckEntity> enteredTrucks = truckService.findEnteredTrucks();
                    if (enteredTrucks.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "All trucks are exited");
                        return false;
                    }
                    String truckNumber = mainController.showNumberVerificationDialog(Instances.truckNumber, enteredTrucks);
                    if (truckNumber != null) Instances.truckNumber = truckNumber;
                    System.out.println("selected truckNumber = " + truckNumber);
                    isWaiting = false;
                    return truckNumber != null;
                }
            }

            log.info("Truck number matches: {}", truckNumber);
        } else {
            log.info("File is not an XML. Skipping xml tag extraction.");
        }

        attach = attachService.saveToSystem(
                file,
                id == 1 ? AttachStatus.ENTRANCE_PHOTO : id == 2 ? AttachStatus.EXIT_PHOTO : null
        );

        if (truckNumber != null && Objects.equals(file.getContentType(), "image/jpeg")) {
            if (id == 1) {
                currentTruckEntity = truckService.create(attach.getId(), truckNumber);
            } else {
                try {
                    TruckEntity truck = truckService.findByTruckNumber(truckNumber, TruckAction.ENTRANCE);
                    truck.getTruckPhotos().add(truckPhotoService.findById(attach.getId()));
                    currentTruckEntity = truck;
                } catch (RuntimeException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                    return false;
                }
            }
            truckNumber = "";
            return true;
        }
        return attach != null;
    }

    private String extractTagFromXmlFile(MultipartFile file, String tagName) {
        try {
            byte[] fileBytes = file.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                Element element = (Element) nodeList.item(0);
                return element.getTextContent();
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
