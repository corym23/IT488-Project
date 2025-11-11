package com.pug.in452.IN452_MovieLens;

public class MovieLensAdapter implements MovieDatabase {
    private final MovieLensDB adaptee;

    public MovieLensAdapter(String dbConnection) {
        this.adaptee = MovieLensDB.getInstance(dbConnection);
    }

    @Override
    public int getMovieCount() {
        return adaptee.fetchMovieCount();
    }

    @Override
    public String[] getMovieTitles(int limit) {
        return adaptee.fetchMovieTitles(limit);
    }

    @Override
    public int getRatingsCount() {
        return adaptee.fetchRatingsCount();
    }

    @Override
    public MovieRating[] getTopRatedMovies(int limit) {
        return adaptee.fetchTopRatedMovies(limit);
    }

    @Override
    public int getUserCount() {
        return adaptee.fetchUserCount();
    }

    @Override
    public GenreCount[] getPopularGenres(int limit) {
        return adaptee.fetchPopularGenres(limit);
    }

    @Override
    public int getTagsCount() {
        return adaptee.fetchTagsCount();
    }

    @Override
    public TagCount[] getPopularTags(int limit) {
        return adaptee.fetchPopularTags(limit);
    }
}
