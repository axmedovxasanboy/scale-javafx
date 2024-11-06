package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageController {

    @FXML
    private ImageView imageView1, imageView2, imageView3, imageView4;

    @FXML
    public void initialize() {

        imageView1.setImage(new Image("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSz7anHD2cY-BoGEN5YeyPKsufhb434-nVleQ&s"));
        imageView2.setImage(new Image("https://media.cnn.com/api/v1/images/stellar/prod/190430171751-mona-lisa.jpg?q=w_2000,c_fill"));
        imageView3.setImage(new Image("https://media.greatbigphotographyworld.com/wp-content/uploads/2022/04/famous-war-photographers.jpg"));
        imageView4.setImage(new Image("https://upload.wikimedia.org/wikipedia/en/thumb/3/3c/Chris_Hemsworth_as_Thor.jpg/220px-Chris_Hemsworth_as_Thor.jpg"));

        // Add click listeners to show images in a modal
        addClickListener(imageView1);
        addClickListener(imageView2);
        addClickListener(imageView3);
        addClickListener(imageView4);
    }

    private void addClickListener(ImageView imageView) {
        imageView.setOnMouseClicked(event -> showImageInModal(imageView.getImage()));
    }

    private void showImageInModal(Image image) {
        // Create a new stage (modal)
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle("Image Preview");

        // Create an ImageView to display the image
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600); // Adjust modal size as needed

        // Create layout and add ImageView
        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 600, 400); // Adjust modal size as needed
        modalStage.setScene(scene);

        modalStage.showAndWait();
    }
}
