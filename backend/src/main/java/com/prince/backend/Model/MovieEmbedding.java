package com.prince.backend.Model;

import java.util.List;

public class MovieEmbedding {
    private String title;
    private String description;
    private List<Double> embeddings;

    public MovieEmbedding(String description, List<Double> embeddings, String title) {
        this.description = description;
        this.embeddings = embeddings;
        this.title = title;
    }

    public MovieEmbedding() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Double> getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(List<Double> embeddings) {
        this.embeddings = embeddings;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
