package com.pug.in452.IN452_MovieLens;

public interface MovieDatabase {
    int getMovieCount();
    String[] getMovieTitles(int limit);
    int getRatingsCount();
    MovieRating[] getTopRatedMovies(int limit);
    int getUserCount();
    GenreCount[] getPopularGenres(int limit);
    int getTagsCount();
    TagCount[] getPopularTags(int limit);
}
