package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import edu.virginia.sde.reviews.utils.WindowConstants;

public class CourseReviewsApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Fix the FXML path to match the package structure
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/login.fxml"));
        Parent root = loader.load();
        
        // Set up the primary stage
        primaryStage.setTitle("Course Reviews");
        primaryStage.setScene(new Scene(root, WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT));
        primaryStage.setResizable(false); // Prevent window resizing as per requirements
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}