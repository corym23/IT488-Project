# Selenium tests for Attendance Tracking System (ATS)

This folder contains Selenium UI tests for the Attendance Tracking System. The primary test added for Jira ticket ATS-47 is:

- `src/test/java/com/attendance/AttendanceATS47Test.java` — opens the local UI, selects a roster name (dropdown or radio), submits, and verifies the success confirmation.

Prerequisites

- Java 17 (or matching `maven.compiler.source` in `pom.xml`).
- Maven.
- Chrome browser installed (compatible with the chromedriver version managed by WebDriverManager).
- The web UI running locally (Vite dev server) before running tests. The tests expect the UI at `http://localhost:5173` by default.

Run tests

From the repository root or this folder, run:

```bash
# from repo root
cd selenium
mvn test -DskipTests=false
```

Notes and customization

- Base URL: The test currently targets `http://localhost:5173`. Update the `url` variable in `selenium/src/test/java/com/attendance/AttendanceATS47Test.java` if your UI runs on a different host/port.
- Headless mode: The test uses Chrome headless by default. You can edit the `ChromeOptions` in the test to remove `--headless=new` for visible runs.
- Logs & warnings: You may see SLF4J or Selenium CDP warnings; these do not prevent the test from running. To reduce noise, add an SLF4J binding (e.g., `slf4j-simple`) to the POM.
- CI: Make sure the CI machine has a Chrome installation and supports running headless Chrome.

Troubleshooting

- If Maven fails with browser/CDP warnings, ensure Chrome is up-to-date or add the appropriate Selenium devtools dependency matching your Chrome major version.
- If WebDriverManager can't download a driver, verify network access and proxy settings.

Contact
For questions about ATS-47 test behavior, ask the author or open ticket ATS-47 in Jira.
