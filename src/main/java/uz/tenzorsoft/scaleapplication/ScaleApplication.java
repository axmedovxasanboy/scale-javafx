package uz.tenzorsoft.scaleapplication;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ScaleApplication extends Application {
    private ConfigurableApplicationContext context;
    private Parent rootNode;

    public static void main(String[] args) {
        Application.launch(ScaleApplication.class, args);
    }

    @Override
    public void init() throws Exception {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ScaleApplication.class);
        context = builder.run(getParameters().getRaw().toArray(new String[0]));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/index.fxml"));
        loader.setControllerFactory(context::getBean);
        loader.setLocation(getClass().getResource("/fxml/index.fxml"));
        rootNode = loader.load();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/index.fxml"));
        loader.setControllerFactory(context::getBean);
        rootNode = loader.load();
        primaryStage.setTitle("Scale Application");
        primaryStage.setScene(new Scene(rootNode));

        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();
    }
}
