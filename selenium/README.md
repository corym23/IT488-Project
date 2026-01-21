# Selenium tests

This module contains end-to-end Selenium tests for the Attendance Tracking System.

Prerequisites

- Java 11 or newer (JDK installed and on `PATH`)
- Maven (or use your IDE to run tests)
- Google Chrome installed (chromedriver is managed automatically by WebDriverManager)
- The web app running locally (the tests default to `http://localhost:5173`)

Quick run (from repository root)

Start the web client (recommended) so the tests have a server to exercise:

```bash
cd client
npm install
npm run dev
```

Then, in another terminal run the Selenium tests with Maven:

```bash
mvn -f selenium/pom.xml test
```

Common options

- Override the base URL the tests hit (default `http://localhost:5173`):

```bash
mvn -f selenium/pom.xml -DbaseUrl=http://localhost:3000 test
```

- Run a single test class (example):

```bash
mvn -f selenium/pom.xml -Dtest=AttendanceHappyPathTest test
```

- Run a single test method (example):

```bash
mvn -f selenium/pom.xml -Dtest=AttendanceHappyPathTest#happyPath_submitsNameAndShowsConfirmation_andPersistsRecord test
```

Notes & troubleshooting

- Tests use `io.github.bonigarcia:webdrivermanager` to download a chromedriver compatible with your Chrome.
- The test class `AttendanceHappyPathTest` is in `selenium/src/test/java/com/attendance` and defaults to `baseUrl` `http://localhost:5173`.
- If tests fail with driver errors, ensure Chrome is up to date and that a compatible JDK/Maven are installed.
- On CI or headless machines, the test launches Chrome in headless mode by default.

If you want, I can run the tests headlessly now and report the results — tell me to proceed.
