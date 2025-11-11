package com.pug.in452.IN452_MovieLens;

public class MovieRating {
    private final String title;
    private final double rating;

    public MovieRating(String title, double rating) {
        this.title = title;
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public double getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return title + " - " + rating;
    }
}
