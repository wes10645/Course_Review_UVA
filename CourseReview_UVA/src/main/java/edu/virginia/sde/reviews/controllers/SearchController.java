package edu.virginia.sde.reviews.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;

import edu.virginia.sde.reviews.database.DatabaseManager;
import edu.virginia.sde.reviews.Models.Course;
import edu.virginia.sde.reviews.utils.WindowConstants;

import java.io.IOException;

public class SearchController {
    private DatabaseManager databaseManager;

    @FXML
    private TextField subjectField;
    @FXML
    private TextField numberField;
    @FXML
    private TextField titleField;
    @FXML
    private TableView<Course> courseTable;
    @FXML
    private TableColumn<Course, String> subjectColumn;
    @FXML
    private TableColumn<Course, Integer> numberColumn;
    @FXML
    private TableColumn<Course, String> titleColumn;
    @FXML
    private TableColumn<Course, Double> ratingColumn;

    public SearchController() {
        this.databaseManager = new DatabaseManager();
    }

    @FXML
    public void initialize() {
        // Set up table columns
        subjectColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSubject()));

        numberColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getNumber()).asObject());

        titleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitle()));

        ratingColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAverageRating()).asObject());

        ratingColumn.setCellFactory(column -> new TableCell<Course, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("");
                } else {
                    setText(String.format("%.1f", item));
                }
            }
        });

        // Add double-click handler
        courseTable.setRowFactory(tv -> {
            TableRow<Course> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    Course selectedCourse = row.getItem();
                    openReviewScene(selectedCourse);
                }
            });
            return row;
        });

        // Load initial data
        handleSearch();
    }

    @FXML
    private void handleSearch() {
        String subject = subjectField.getText();
        String numberStr = numberField.getText();
        String title = titleField.getText();

        Integer number = null;
        if (!numberStr.isEmpty()) {
            try {
                number = Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                showError("Course number must be a 4-digit number.");
                return;
            }
        }

        var courses = databaseManager.searchCourses(
                subject.isEmpty() ? null : subject,
                number,
                title.isEmpty() ? null : title
        );

        ObservableList<Course> courseList = FXCollections.observableArrayList(courses);
        courseTable.setItems(courseList);
    }

    @FXML
    private void handleAddCourse() {
        TextInputDialog subjectDialog = new TextInputDialog();
        subjectDialog.setTitle("Add Course");
        subjectDialog.setHeaderText("Enter course subject (2 - 4 letters)");
        subjectDialog.setContentText("Subject:");
        String subject = subjectDialog.showAndWait().orElse("").toUpperCase();

        if (!subject.matches("[A-Za-z]{2,4}")) {
            showError("Subject must be 2 - 4 letters.");
            return;
        }

        TextInputDialog numberDialog = new TextInputDialog();
        numberDialog.setTitle("Add Course");
        numberDialog.setHeaderText("Enter 4 digit course number");
        numberDialog.setContentText("Number:");
        String numberStr = numberDialog.showAndWait().orElse("");

        if (!numberStr.matches("\\d{4}")) {
            showError("Number must be a 4 digit number.");
            return;
        }

        int number = Integer.parseInt(numberStr);

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Add Course");
        titleDialog.setHeaderText("Enter course title (1 - 50 characters)");
        titleDialog.setContentText("Title:");
        String title = titleDialog.showAndWait().orElse("");

        if (title.length() < 1 || title.length() > 50) {
            showError("Title must be between 1 and 50 characters.");
            return;
        }

        Course course = new Course(subject, number, title);
        boolean success = databaseManager.addCourse(course.getSubject(), course.getNumber(), course.getTitle()); // You must implement this method in your DB class

        if (!success) {
            showError("Failed to add course. It may already exist.");
        } else {
            handleSearch(); // Refresh list
        }
    }

    @FXML
    private void handleMyReviews() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/myreviews.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) courseTable.getScene().getWindow();
            stage.setScene(new Scene(root, WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT));
        } catch (IOException e) {
            showError("Could not load My Reviews screen.");
        }
    }

    @FXML
    private void handleLogout() {
        databaseManager.setCurrentUser(null);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) courseTable.getScene().getWindow();
            stage.setScene(new Scene(root, WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT));
        } catch (IOException e) {
            showError("Could not load login screen.");
        }
    }

    private void openReviewScene(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/virginia/sde/reviews/reviews.fxml"));
            Parent root = loader.load();
            ReviewController controller = loader.getController();
            controller.setCourse(course);

            Stage stage = (Stage) courseTable.getScene().getWindow();
            stage.setScene(new Scene(root, WindowConstants.WINDOW_WIDTH, WindowConstants.WINDOW_HEIGHT));
        } catch (IOException e) {
            showError("Could not load Course Review screen.");
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