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

## Build (recommended: Maven)

From project root:

  mvn -DskipTests package

This will:
- Compile the code into `target/classes`.
- Download dependencies (including the Microsoft SQL Server JDBC driver declared in `pom.xml`).

---

## Run (GUI) using Maven exec (recommended)

This runs the GUI with dependencies automatically placed on the classpath:

  mvn -DskipTests exec:java -Dexec.mainClass="com.pug.in452.IN452_MovieLens.MovieLensThreadApp" -Dexec.cleanupDaemonThreads=false

Notes:
- Click `Connect (Demo)` to use the demo database and enable the `Start Simulation` button.
- Click `Start Simulation` to begin the threads and watch output in the text area.
- Adjust the speed slider (labels show ms values) and release to apply the new speed.
- To connect to a real SQL Server instance, use `Connect to DB` and update the connection string or credentials in the GUI code if necessary.

---

## Run (GUI) using java -cp (alternate)

1. Build: `mvn -DskipTests package`
2. Create a runtime classpath with dependencies (example using Maven plugin):

   mvn dependency:build-classpath -Dmdep.outputFile=cp.txt

3. Run with the generated classpath:

   java -cp target/classes:$(cat cp.txt) com.pug.in452.IN452_MovieLens.MovieLensThreadApp

If you prefer to explicitly reference the SQL Server driver jar:

   java -cp target/classes:/path/to/mssql-jdbc-<version>.jar com.pug.in452.IN452_MovieLens.MovieLensThreadApp

---

## Clean

  mvn clean

---

## Troubleshooting — "No suitable driver found"

If you click `Connect to DB` and see `Connection failed: No suitable driver found for jdbc:sqlserver://...`, then the JDBC driver is not available at runtime. Solutions:

- Use the Maven exec approach above (it places the driver on the classpath).
- If using `java -cp`, include the SQL Server JDBC jar in the classpath.
- Ensure the driver JAR version matches your Java runtime (the declared driver in `pom.xml` is `mssql-jdbc:12.6.1.jre11`).

The code also attempts to load the driver class explicitly and prints a diagnostic if it is not found.

---

## Developer notes

- The demo DB (`DemoMovieLensDB`) provides deterministic outputs and is safe for UI testing without database access.
- If you add a real DB driver or change the driver version, update `pom.xml` accordingly.
- If you want a single executable JAR that bundles dependencies, I can add a Maven Shade plugin configuration and produce a fat JAR.

---

If you want, I can:
- Add a Maven `exec` plugin configuration to `pom.xml` to simplify running.
- Create a shaded (fat) JAR for a single-file distribution.
- Remove the deprecated headless stub file entirely.

---

## IN452_MovieLens — Test JVM agent handling

What I changed
- The build now copies the byte-buddy agent into target (maven-dependency-plugin) and sets surefire to preload it. This avoids Mockito/Byte‑Buddy dynamically attaching an agent at test runtime.
- The byte-buddy agent version is controlled via the bytebuddy.version property in pom.xml.

How to run tests
- mvn test

If you need to change the agent version
- Edit the bytebuddy.version property in pom.xml. The build will copy that agent JAR into target and surefire will use it.

Notes
- Tests should run without the Mockito self-attach warning after this change. A JVM bootstrap classpath sharing warning may still appear; it is unrelated to the agent.