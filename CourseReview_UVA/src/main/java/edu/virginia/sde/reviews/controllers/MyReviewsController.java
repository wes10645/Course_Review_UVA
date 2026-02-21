package edu.virginia.sde.reviews.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import edu.virginia.sde.reviews.Models.Review;
import edu.virginia.sde.reviews.Models.Course;
import edu.virginia.sde.reviews.database.DatabaseManager;
import edu.virginia.sde.reviews.utils.WindowConstants;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class MyReviewsController {
    private DatabaseManager databaseManager = new DatabaseManager();

    @FXML private TableView<Review> myReviewsTable;
    @FXML private TableColumn<Review, String> courseColumn;
    @FXML private TableColumn<Review, Integer> ratingColumn;
    @FXML private TableColumn<Review, String> timestampColumn;

    @FXML
    public void initialize() {
        // Set up table columns
        courseColumn.setCellValueFactory(cellData -> {
            // You may need to fetch course info if not present in Review
            int courseId = cellData.getValue().getCourseId();
            Course course = databaseManager.searchCourses(null, null, null)
                .stream().filter(c -> c.getId() == courseId).findFirst().orElse(null);
            String courseStr = (course != null)
                ? course.getSubject() + " " + course.getNumber()
                : "Course " + courseId;
            return new javafx.beans.property.SimpleStringProperty(courseStr);
        });
        ratingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getRating()).asObject());
        timestampColumn.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getTimestamp();
            String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        // Load user's reviews
        List<Review> reviews = databaseManager.getUserReviews();
        ObservableList<Review> reviewList = FXCollections.observableArrayList(reviews);
        myReviewsTable.setItems(reviewList);

        // Make rows clickable to go to course review scene
        myReviewsTable.setRowFactory(tv -> {
            TableRow<Review> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Review review = row.getItem();
                    openCourseReviewScene(review.getCourseId());
                }
            });
            return row;
        });
    }

    private void openCourseReviewScene(int courseId) {
        try {
            // Find the course object
            Course course = databaseManager.searchCourses(null, null, null)
                .stream().filter(c -> c.getId() == courseId).findFirst().orElse(null);
            if (course == null) return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/reviews.fxml"));
            Parent root = loader.load();
            ReviewController controller = loader.getController();
            controller.setCourse(course);

            Stage stage = (Stage) myReviewsTable.getScene().getWindow();
            stage.setScene(new Scene(root, WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT));
        } catch (IOException e) {
            showError("Could not load Course Review screen.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/search.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) myReviewsTable.getScene().getWindow();
            stage.setScene(new Scene(root, WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT));
        } catch (IOException e) {
            showError("Could not load Course Search screen.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
