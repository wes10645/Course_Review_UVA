package edu.virginia.sde.reviews.Models;

public class Course {
    private int id;
    private String subject;
    private int number;
    private String title;
    private double averageRating;


    public Course() {
    }


    public Course(int id, String subject, int number, String title) {
        this.id = id;
        this.subject = subject;
        this.number = number;
        this.title = title;
    }
    public Course(String subject, int number, String title) {
        this.subject = subject;
        this.number = number;
        this.title = title;
    }
    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    @Override
    public String toString() {
        return String.format("%s %d: %s", subject, number, title);
    }
}