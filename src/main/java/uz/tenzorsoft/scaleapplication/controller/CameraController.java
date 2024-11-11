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
import uz.tenzorsoft.scaleapplication.domain.enumerators.AttachStatus;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AttachIdWithStatus;
import uz.tenzorsoft.scaleapplication.domain.response.AttachResponse;
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.ui.ButtonController;
import uz.tenzorsoft.scaleapplication.ui.TableController;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
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
    private final TruckService truckService;

    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(HttpServletRequest request, @PathVariable("id") Integer cameraId) {
        if (request instanceof MultipartHttpServletRequest multipartRequest) {
            System.out.println("Request is processing " + cameraId);
            System.out.println("Camera id: " + cameraId);
            System.out.println("multipartRequest.getFileMap().size() = " + multipartRequest.getFileMap().size());

            if (multipartRequest.getFileMap().size() < 3 || truckPosition != -1 || isWaiting || currentUser.getId() == null) {
                return ResponseEntity.ok("NOT_MATCH");
            }
            for (Map.Entry<String, MultipartFile> entry : multipartRequest.getFileMap().entrySet()) {
                String fileName = entry.getKey();
                MultipartFile file = entry.getValue();
                System.out.println("fileName = " + fileName);
                System.out.println("File processing");

                if (file.isEmpty()) {
                    log.warn("File is empty: {}", fileName);
                    continue;
                }

                try {
                    try {
                        if (fileName.equals("anpr.xml")) {
                            truckNumber = extractNumberFromXmlFile(file);
                            System.out.println("truckNumber = " + truckNumber);
                            if (!truckService.isValidTruckNumber(truckNumber)) {
                                log.warn("Truck number does not match: {}", truckNumber);
                                showAlert(Alert.AlertType.ERROR, "Error", "Truck number does not match");
                                truckNumber = "";
                                return ResponseEntity.ok("NOT_MATCH");
                            }
                            if (!truckService.checkEntranceAvailable(truckNumber)) {
                                System.err.println("Entrance available after 3 minutes");
                                showAlert(Alert.AlertType.ERROR, "Entrance not available", "Entrance not available");
                                return ResponseEntity.ok("Entrance exception");
                            }

                        }
                    } catch (Exception e) {
                        System.out.println("ANPR Exception" + e.getMessage());
                    }
                    try {
                        if (fileName.equals("detectionPicture.jpg")) {

                            AttachResponse attachResponse = attachService.saveToSystem(file);
                            if (attachResponse == null) {
                                log.warn("See logs for error cause. Unable to save file: {}", fileName);
                                return ResponseEntity.ok("Unable to save file");
                            }
                            if (cameraId == 1) {
                                System.out.println("saving image " + cameraId);
                                currentTruck.getAttaches().add(new AttachIdWithStatus(attachResponse.getId(), AttachStatus.ENTRANCE_PHOTO));
                            } else if (cameraId == 2) {
                                System.out.println("saving image 2");
                                currentTruck.getAttaches().add(new AttachIdWithStatus(attachResponse.getId(), AttachStatus.EXIT_PHOTO));
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Image Exception " + e.getMessage());
                    }
                } catch (Exception e) {
                    log.warn("File processing failed: {}", fileName);
                    log.error(e.getMessage(), e);
                    System.out.println(e.getMessage());
                    return ResponseEntity.status(500).body("Failed to save file: " + file.getOriginalFilename());
                }
            }
            currentTruck.setTruckNumber(truckNumber);
            currentTruck.setEnteredStatus(TruckAction.ENTRANCE);
            try {
                truckService.saveTruck(currentTruck, cameraId);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            if (cameraId == 1) tableController.addLastRecord();

            if (cameraId == 1) {
                System.out.println("Opening gate 1");
                buttonController.openGate1(0);
            } else if (cameraId == 2) {
                currentTruck.setExitedStatus(TruckAction.EXIT);
                System.out.println("Opening gate 2");
                buttonController.openGate2(7);
            }

            return ResponseEntity.ok("Files uploaded and saved successfully.");
        }
        log.warn("Request is not a multipart request");
        return ResponseEntity.badRequest().body("Request is not a multipart request");
    }

    private String extractNumberFromXmlFile(MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("originalLicensePlate");
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
