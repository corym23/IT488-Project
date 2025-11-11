package com.pug.in452.IN452_MovieLens;

public class GenreCount {
    private final String genre;
    private final int count;

    public GenreCount(String genre, int count) {
        this.genre = genre;
        this.count = count;
    }

    public String getGenre() {
        return genre;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return genre + " - " + count;
    }
}
