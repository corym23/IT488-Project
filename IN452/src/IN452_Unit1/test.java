package IN452_Unit1;

public class test extends MovieLensDB{

	public test(String dbUsername, String dbPassword) {
		super(dbUsername, dbPassword);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// Example usage with actual username and password strings
		MovieLensDB mldb = new MovieLensDB("your_username", "your_password");
		try {
			mldb.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mldb.getMovieGenres();
	}

}