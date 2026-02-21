package edu.virginia.sde.reviews.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import edu.virginia.sde.reviews.Models.Course;
import edu.virginia.sde.reviews.Models.Review;
import edu.virginia.sde.reviews.database.DatabaseManager;
import edu.virginia.sde.reviews.utils.WindowConstants;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewController {
    private DatabaseManager databaseManager = new DatabaseManager();
    private Course course;
    private Review userReview;

    @FXML private Label courseInfoLabel;
    @FXML private Label averageRatingLabel;
    @FXML private VBox reviewForm;
    @FXML private Label reviewFormLabel;
    @FXML private ComboBox<Integer> ratingComboBox;
    @FXML private TextArea commentTextArea;
    @FXML private Button submitButton;
    @FXML private Button deleteButton;
    @FXML private TableView<Review> reviewsTable;
    @FXML private TableColumn<Review, Integer> ratingColumn;
    @FXML private TableColumn<Review, String> timestampColumn;
    @FXML private TableColumn<Review, String> commentColumn;

    // This should be called by the previous scene to set the course
    public void setCourse(Course course) {
        this.course = course;
        loadCourseData();
    }

    @FXML
    public void initialize() {
        // Set up rating choices
        if (ratingComboBox != null) {
            ratingComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        }

        // Set up table columns
        if (ratingColumn != null) {
            ratingColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getRating()).asObject());
        }
        if (timestampColumn != null) {
            timestampColumn.setCellValueFactory(cellData -> {
                Timestamp ts = cellData.getValue().getTimestamp();
                String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
                return new javafx.beans.property.SimpleStringProperty(formatted);
            });
        }
        if (commentColumn != null) {
            commentColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getComment() == null ? "" : cellData.getValue().getComment()));
        }
    }

    private void loadCourseData() {
        if (course == null) return;

        // Set course info
        courseInfoLabel.setText(course.getSubject() + " " + course.getNumber() + ": " + course.getTitle());
        
        // Update average rating display
        if (course.getAverageRating() == 0.0) {
            averageRatingLabel.setText("Average Rating: ");
        } else {
            averageRatingLabel.setText(String.format("Average Rating: %.2f", course.getAverageRating()));
        }

        // Load all reviews for this course
        List<Review> reviews = databaseManager.getCourseReviews(course.getId());
        ObservableList<Review> reviewList = FXCollections.observableArrayList(reviews);
        reviewsTable.setItems(reviewList);

        // Check if current user has a review for this course
        userReview = null;
        int currentUserId = databaseManager.getCurrentUser().getId();
        for (Review r : reviews) {
            if (r.getUserId() == currentUserId) {
                userReview = r;
                break;
            }
        }

        // Show/hide form and set up for add/edit
        if (userReview == null) {
            reviewFormLabel.setText("Add Your Review");
            submitButton.setText("Submit");
            deleteButton.setVisible(false);
            ratingComboBox.getSelectionModel().clearSelection();
            commentTextArea.clear();
        } else {
            reviewFormLabel.setText("Edit Your Review");
            submitButton.setText("Update");
            deleteButton.setVisible(true);
            ratingComboBox.getSelectionModel().select(Integer.valueOf(userReview.getRating()));
            commentTextArea.setText(userReview.getComment());
        }
        reviewForm.setVisible(true);
    }

    @FXML
    private void handleSubmitReview() {
        Integer rating = ratingComboBox.getValue();
        String comment = commentTextArea.getText();

        if (rating == null) {
            showError("Please select a rating.");
            return;
        }

        boolean success;
        if (userReview == null) {
            // Add new review
            success = databaseManager.addReview(course.getId(), rating, comment);
        } else {
            // Update existing review
            success = databaseManager.updateReview(userReview.getId(), rating, comment);
        }

        if (!success) {
            showError("Failed to submit review.");
        } else {
            // Refresh data
            loadCourseData();
        }
    }

    @FXML
    private void handleDeleteReview() {
        if (userReview == null) return;
        boolean success = databaseManager.deleteReview(userReview.getId());
        if (!success) {
            showError("Failed to delete review.");
        } else {
            loadCourseData();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/search.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) courseInfoLabel.getScene().getWindow();
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
