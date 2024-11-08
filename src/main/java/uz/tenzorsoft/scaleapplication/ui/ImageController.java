package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.service.TruckService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static uz.tenzorsoft.scaleapplication.ui.MainController.showAlert;

@Component
@RequiredArgsConstructor
public class ImageController {

    private final TruckService truckService;
    private List<ImageView> images;
    @FXML
    private ImageView imageView1, imageView2, imageView3, imageView4;

    @FXML
    private Pane imageContainer;

    @FXML
    public void initialize() {
        Image image = new Image("/images/no-image.jpg");

        imageView1.setImage(image);
        imageView2.setImage(image);
        imageView3.setImage(image);
        imageView4.setImage(image);
        images = new ArrayList<>();
        images.add(imageView1);
        images.add(imageView2);
        images.add(imageView3);
        images.add(imageView4);

        double imageRatio = 0.95;
        for (ImageView imageView : images) {
            imageView.fitWidthProperty().bind(imageContainer.widthProperty());
            imageView.fitHeightProperty().bind(imageContainer.heightProperty());
            imageView.setPreserveRatio(true);
        }
    }

    public void showImages(TableViewData data) {
        TruckEntity truck = truckService.findById(data.getId());
        if (truck == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Truck does not exist with id: " + data.getId());
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            try {
                String imagePath = truck.getTruckPhotos().get(i).getTruckPhoto().getPath();
                InputStream inputStream = new FileInputStream(imagePath);
                Image image = new Image(inputStream); // Set background loading to true for smoother UI
                images.get(i).setImage(image);
                setupImageClick(images.get(i), image);
            } catch (FileNotFoundException e) {

            }
        }
    }

    private void setupImageClick(ImageView imageView, Image image) {
        imageView.setOnMouseClicked(event -> {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            ImageView fullImageView = new ImageView(image);
            fullImageView.setPreserveRatio(true);

            // Dynamically size the full image dialog
            double dialogWidth = 800; // Set desired dialog width
            double dialogHeight = 600; // Set desired dialog height
            fullImageView.fitWidthProperty().bind(dialogStage.widthProperty().multiply(0.9));
            fullImageView.fitHeightProperty().bind(dialogStage.heightProperty().multiply(0.9));

            VBox vbox = new VBox(fullImageView);
            vbox.setAlignment(Pos.CENTER);
            Scene scene = new Scene(vbox, dialogWidth, dialogHeight);
            dialogStage.setScene(scene);

            dialogStage.showAndWait();
        });
    }


}
