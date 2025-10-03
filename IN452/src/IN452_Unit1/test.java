package IN452_Unit1;



public class test extends MovieLensDB{

	public static void main(String[] args) {
		
		MovieLensDB mldb = new MovieLensDB();
		
		try {
			mldb.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mldb.getMovieGenres();
		

	}

}
