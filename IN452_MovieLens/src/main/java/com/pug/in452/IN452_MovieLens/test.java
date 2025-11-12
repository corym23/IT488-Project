package com.pug.in452.IN452_MovieLens;

public class test {

    public test() {
        // no-op
    }

    public static void main(String[] args) {
        // Use the singleton accessor instead of calling new MovieLensDB
        MovieLensDB mldb = MovieLensDB.getInstance("Server", "your_username", "your_password");
        try {
            mldb.getConnection();
            System.out.println("Connection method invoked (test)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}