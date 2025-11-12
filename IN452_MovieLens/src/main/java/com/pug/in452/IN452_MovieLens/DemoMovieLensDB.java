package com.pug.in452.IN452_MovieLens;

import java.util.ArrayList;
import java.util.List;

/**
 * DemoMovieLensDB provides lightweight, deterministic, in-memory responses
 * that emulate a MovieLens database. This class is intended for development
 * and UI demonstration where a real JDBC database is not available or
 * desired.
 *
 * It implements the MovieDatabase interface rather than extending
 * MovieLensDB so it doesn't require access to MovieLensDB constructors.
 */
public class DemoMovieLensDB implements MovieDatabase {

    public DemoMovieLensDB() {
        // no-op; uses in-memory canned responses
    }

    @Override
    public int getMovieCount() {
        return 100;
    }

    @Override
    public String[] getMovieTitles(int limit) {
        List<String> list = new ArrayList<>();
        list.add("Movie A");
        list.add("Movie B");
        list.add("Movie C");
        return list.subList(0, Math.min(limit, list.size())).toArray(new String[0]);
    }

    @Override
    public int getRatingsCount() {
        return 1000;
    }

    @Override
    public MovieRating[] getTopRatedMovies(int limit) {
        List<MovieRating> out = new ArrayList<>();
        out.add(new MovieRating("Top Movie 1", 4.8));
        out.add(new MovieRating("Top Movie 2", 4.7));
        return out.subList(0, Math.min(limit, out.size())).toArray(new MovieRating[0]);
    }

    @Override
    public int getUserCount() {
        return 50;
    }

    @Override
    public GenreCount[] getPopularGenres(int limit) {
        List<GenreCount> out = new ArrayList<>();
        out.add(new GenreCount("Drama", 50));
        out.add(new GenreCount("Comedy", 45));
        return out.subList(0, Math.min(limit, out.size())).toArray(new GenreCount[0]);
    }

    @Override
    public int getTagsCount() {
        return 200;
    }

    @Override
    public TagCount[] getPopularTags(int limit) {
        List<TagCount> out = new ArrayList<>();
        out.add(new TagCount("tag1", 30));
        out.add(new TagCount("tag2", 25));
        return out.subList(0, Math.min(limit, out.size())).toArray(new TagCount[0]);
    }
}