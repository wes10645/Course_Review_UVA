package edu.virginia.sde.reviews.database;

import edu.virginia.sde.reviews.Models.User;
import edu.virginia.sde.reviews.Models.Course;
import edu.virginia.sde.reviews.Models.Review;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:course_reviews.db";
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static User currentUser = null;
    private Connection connection;

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            addPrebuiltCourses();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            // Create users table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                )
            """);

            // Create courses table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    subject TEXT NOT NULL,
                    number INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    UNIQUE(subject, number, title)
                )
            """);

            // Create reviews table
            statement.execute("""
                CREATE TABLE IF NOT EXISTS reviews (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    course_id INTEGER NOT NULL,
                    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
                    comment TEXT,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (course_id) REFERENCES courses(id),
                    UNIQUE(user_id, course_id)
                )
            """);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating tables", e);
            throw new RuntimeException("Failed to create database tables", e);
        }
    }

    private boolean courseExists(String subject, int number, String title) {
        String query = "SELECT COUNT(*) FROM courses WHERE subject = ? AND number = ? AND title = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject);
            statement.setInt(2, number);
            statement.setString(3, title);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if course exists", e);
            return false;
        }
    }

    private void addPrebuiltCourses() {
        // List of courses to add if they don't exist
        Course[] prebuiltCourses = {
            new Course(0, "CS", 3140, "Software Development Essentials"),
            new Course(0, "CS", 4501, "Advanced Software Development"),
            new Course(0, "STS", 2600, "Engineering Ethics"),
            new Course(0, "CHEM", 1410, "Introductory College Chemistry I"),
            new Course(0, "MATH", 1310, "Calculus I"),
            new Course(0, "ECON", 2010, "Principles of Microeconomics"),
            new Course(0, "PHYS", 2010, "Principles of Physics 1")
        };

        for (Course course : prebuiltCourses) {
            if (!courseExists(course.getSubject(), course.getNumber(), course.getTitle())) {
                addCourse(course.getSubject(), course.getNumber(), course.getTitle());
            }
        }
    }

    public boolean userExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next() && resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user exists", e);
            throw new RuntimeException("Failed to check if user exists", e);
        }
    }

    public User getUser(String username, String password) {
        String query = "SELECT id, username FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                return user;
            }
            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user", e);
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public boolean createUser(String username, String password) {
        if (password.length() < 8) {
            return false;
        }

        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user", e);
            return false;
        }
    }

    public void setCurrentUser(User user) {
        currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    public boolean addCourse(String subject, int number, String title) {
        // Validate input
        if (subject == null || subject.length() < 2 || subject.length() > 4 || 
            !subject.matches("[A-Za-z]+") || number < 1000 || number > 9999 || 
            title == null || title.length() < 1 || title.length() > 50) {
            return false;
        }

        String query = "INSERT INTO courses (subject, number, title) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, subject.toUpperCase());
            statement.setInt(2, number);
            statement.setString(3, title);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding course", e);
            return false;
        }
    }

    public List<Course> searchCourses(String subject, Integer number, String title) {
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT c.*, COALESCE(AVG(r.rating), 0) as avg_rating " +
            "FROM courses c " +
            "LEFT JOIN reviews r ON c.id = r.course_id " +
            "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (subject != null && !subject.isEmpty()) {
            queryBuilder.append("AND UPPER(c.subject) = UPPER(?) ");
            params.add(subject);
        }
        if (number != null) {
            queryBuilder.append("AND c.number = ? ");
            params.add(number);
        }
        if (title != null && !title.isEmpty()) {
            queryBuilder.append("AND UPPER(c.title) LIKE UPPER(?) ");
            params.add("%" + title + "%");
        }

        queryBuilder.append("GROUP BY c.id");

        List<Course> courses = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Course course = new Course();
                course.setId(resultSet.getInt("id"));
                course.setSubject(resultSet.getString("subject"));
                course.setNumber(resultSet.getInt("number"));
                course.setTitle(resultSet.getString("title"));
                course.setAverageRating(resultSet.getDouble("avg_rating"));
                courses.add(course);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching courses", e);
            throw new RuntimeException("Failed to search courses", e);
        }
        return courses;
    }

    public boolean addReview(int courseId, int rating, String comment) {
        if (currentUser == null) {
            return false;
        }

        String query = "INSERT INTO reviews (user_id, course_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, currentUser.getId());
            statement.setInt(2, courseId);
            statement.setInt(3, rating);
            statement.setString(4, comment);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding review", e);
            return false;
        }
    }

    public List<Review> getCourseReviews(int courseId) {
        String query = "SELECT * FROM reviews WHERE course_id = ? ORDER BY timestamp DESC";
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, courseId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Review review = new Review();
                review.setId(resultSet.getInt("id"));
                review.setUserId(resultSet.getInt("user_id"));
                review.setCourseId(resultSet.getInt("course_id"));
                review.setRating(resultSet.getInt("rating"));
                review.setComment(resultSet.getString("comment"));
                review.setTimestamp(resultSet.getTimestamp("timestamp"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting course reviews", e);
            throw new RuntimeException("Failed to get course reviews", e);
        }
        return reviews;
    }

    public List<Review> getUserReviews() {
        if (currentUser == null) {
            return new ArrayList<>();
        }

        String query = "SELECT r.*, c.subject, c.number FROM reviews r " +
                      "JOIN courses c ON r.course_id = c.id " +
                      "WHERE r.user_id = ? ORDER BY r.timestamp DESC";
        List<Review> reviews = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, currentUser.getId());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Review review = new Review();
                review.setId(resultSet.getInt("id"));
                review.setUserId(resultSet.getInt("user_id"));
                review.setCourseId(resultSet.getInt("course_id"));
                review.setRating(resultSet.getInt("rating"));
                review.setComment(resultSet.getString("comment"));
                review.setTimestamp(resultSet.getTimestamp("timestamp"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user reviews", e);
            throw new RuntimeException("Failed to get user reviews", e);
        }
        return reviews;
    }

    public boolean updateReview(int reviewId, int rating, String comment) {
        if (currentUser == null) {
            return false;
        }

        String query = "UPDATE reviews SET rating = ?, comment = ?, timestamp = CURRENT_TIMESTAMP " +
                      "WHERE id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, rating);
            statement.setString(2, comment);
            statement.setInt(3, reviewId);
            statement.setInt(4, currentUser.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating review", e);
            return false;
        }
    }

    public boolean deleteReview(int reviewId) {
        if (currentUser == null) {
            return false;
        }

        String query = "DELETE FROM reviews WHERE id = ? AND user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, reviewId);
            statement.setInt(2, currentUser.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting review", e);
            return false;
        }
    }
}
