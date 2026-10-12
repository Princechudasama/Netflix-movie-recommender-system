package com.prince.backend.Model;


public class MovieMatch {
    private String title;
    private String description;
    private double similarity;

    public MovieMatch() {
    }

    public MovieMatch(String description,  String title,double similarity) {
        this.description = description;
        this.similarity = similarity;
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
