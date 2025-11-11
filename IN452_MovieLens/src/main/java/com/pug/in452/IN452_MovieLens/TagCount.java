package com.pug.in452.IN452_MovieLens;

public class TagCount {
    private final String tag;
    private final int count;

    public TagCount(String tag, int count) {
        this.tag = tag;
        this.count = count;
    }

    public String getTag() {
        return tag;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return tag + " - " + count;
    }
}
