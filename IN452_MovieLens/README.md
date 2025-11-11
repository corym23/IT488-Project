# IN452_MovieLens

This project provides a small Swing GUI and a multi-threaded simulator that demonstrates concurrent access to a MovieLens-style database. The project includes a lightweight demo database implementation so you can run the GUI without a real SQL Server.

This README documents how to build, clean and run the project, plus notes about the demo mode and the JDBC driver requirement for connecting to a real SQL Server database.

---

## Important files (new/modified)

- `src/main/java/com/pug/in452/IN452_MovieLens/MovieLensSimulator.java`

  - Multi-threaded simulator (movie thread, rating thread, monitor thread).
  - Public API: `startSimulation()`, `stopSimulation()`, `setSimulationSpeed(int)`.
  - Prints status to `System.out` (GUI redirects output to the text area).

- `src/main/java/com/pug/in452/IN452_MovieLens/DemoMovieLensDB.java`

  - Lightweight in-memory demo implementation of `MovieLensDB` that returns canned responses.
  - Intended for development and UI demonstration when a real database is not available.

- `src/main/java/com/pug/in452/IN452_MovieLens/MovieLensThreadApp.java`

  - Swing GUI that provides controls to connect to a DB (or demo), start/stop the simulator and adjust speed via a labeled slider.
  - The GUI redirects `System.out`/`System.err` into the text log area.

- `src/main/java/com/pug/in452/IN452_MovieLens/HeadlessDemoLauncher.java` (deprecated stub)

  - Deprecated stub left in the tree for reference; no main method present.

- `src/main/java/com/pug/in452/IN452_MovieLens/MovieLensDB.java` (minor diagnostic change)

  - Attempts to load the SQL Server JDBC driver class to provide clearer diagnostics if the driver is missing.

- `pom.xml`
  - The Microsoft SQL Server JDBC dependency was changed from `test` scope to normal scope so the driver is available at runtime when launching the GUI via Maven.

---

## MovieLensClient (CLI) — live database only

The MovieLensClient is a small command-line client that queries the live MovieLens database via the MovieLensAdapter and prints four sections to standard output:

- Movie Count
- Top N rated movies (by average rating)
- Popular genres (by counts)
- Popular tags (by counts)

Important details:
- The current implementation requires a live SQL Server database accessible via JDBC. There is no fallback canned data in this build.
- Default JDBC URL (used when you do not supply a JDBC URL on the command line):

  jdbc:sqlserver://localhost;databaseName=IN452;user=IN452_User;password=P@55W0rd!;encrypt=false;

- You can override the JDBC URL by passing it as the first program argument to the client (either via Maven exec or java -cp).

Example output format:

Movie Count: 9742

Top 5 Movies:
12 Angry Men (1997) - 5.0
...

Popular Genres:
Drama - 4361
...

Popular Tags:
In Netflix queue - 131
...

### How to run (recommended: Maven exec)

This ensures dependencies (including the MS SQL JDBC driver) are on the runtime classpath.

From project root:

mvn -DskipTests -Dexec.mainClass=com.pug.in452.IN452_MovieLens.MovieLensClient \
    -Dexec.args="<your-jdbc-url>" exec:java

If you omit -Dexec.args the client will use the default JDBC URL declared in the source.

### How to run (alternate: java -cp)

1. Build the project

   mvn -DskipTests package

2. Build a classpath file (Maven helper)

   mvn dependency:build-classpath -Dmdep.outputFile=cp.txt

3. Run the client with the generated classpath

   java -cp target/classes:$(cat cp.txt) com.pug.in452.IN452_MovieLens.MovieLensClient "<your-jdbc-url>"

Note: if you prefer to reference the JDBC driver jar explicitly, append it to the classpath.

---

## MovieRating (domain object)

MovieRating is a simple immutable POJO used by the adapter and the client to represent a movie and its average rating. Its API:

- MovieRating(String title, double rating)
- String getTitle()
- double getRating()
- String toString()  // returns "<title> - <rating>"

This class is declared at `src/main/java/com/pug/in452/IN452_MovieLens/MovieRating.java`.

---

## Quick build after changes

- Save your files.
- Build (runs tests):

      mvn clean package

- Build but skip tests (faster):

      mvn clean package -DskipTests

- Run only tests:

      mvn test

---

## Prerequisites

- JDK 17+ installed (macOS examples below assume `java` and `/usr/libexec/java_home` are available).
- Maven 3.6+ installed for the recommended build+run workflow.

If you do not have JDK installed, install (example using Homebrew):

brew install openjdk@17

Then set `JAVA_HOME` in your shell (temporary):

export JAVA_HOME=$(/usr/libexec/java_home -v 17)

To persist it in zsh (~/.zshrc):

echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
source ~/.zshrc

Verify java and mvn:

java -version
mvn -v

---

## Troubleshooting — "No suitable driver found"

If you see `Connection failed: No suitable driver found for jdbc:sqlserver://...`, then the JDBC driver is not available at runtime. Solutions:

- Use the Maven exec approach above (it places the driver on the classpath).
- If using `java -cp`, include the SQL Server JDBC jar in the classpath.
- Ensure the driver JAR version matches your Java runtime (the declared driver in `pom.xml` is `mssql-jdbc:12.6.1.jre11`).

The code attempts to load the driver class explicitly and prints a diagnostic if it is not found.

---
