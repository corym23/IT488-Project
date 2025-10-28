package com.pug.in452.IN452_MovieLens;

public class test extends MovieLensDB{

	public test(String dbServer, String dbUsername, String dbPassword) {
		super(dbServer,dbUsername, dbPassword);
		
	}

	public static void main(String[] args) {
		
		MovieLensDB mldb = new MovieLensDB("Server", "your_username", "your_password");
		try {
			mldb.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}

}