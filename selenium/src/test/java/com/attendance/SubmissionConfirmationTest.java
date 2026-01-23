package com.attendance;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@DisplayName("ATS-Confirm: Verify submission confirmation shows name and UTC timestamp")
public class SubmissionConfirmationTest {
    private WebDriver driver;

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testConfirmationDisplaysNameAndTimestamp() throws Exception {
        String url = "http://localhost:5173";
        driver.get(url);
        Thread.sleep(500);

        // Select a name from dropdown or radio and remember the value
        String selectedName = null;
        boolean selected = false;

        try {
            WebElement select = driver.findElement(By.tagName("select"));
            select.click();
            List<WebElement> options = select.findElements(By.tagName("option"));
            for (WebElement option : options) {
                String val = option.getAttribute("value");
                if (val != null && !val.isEmpty()) {
                    option.click();
                    selectedName = option.getText();
                    selected = true;
                    break;
                }
            }
        } catch (NoSuchElementException e) {
            // no dropdown
        }

        if (!selected) {
            try {
                WebElement radio = driver.findElement(By.cssSelector("input[type='radio']"));
                radio.click();
                // label wraps the input; parent text contains the name
                selectedName = radio.findElement(By.xpath("..")).getText().trim();
                selected = true;
            } catch (NoSuchElementException ex) {
                Assertions.fail("No selectable name found for confirmation test");
            }
        }

        // Click submit
        WebElement submit;
        try {
            submit = driver.findElement(By.cssSelector("button[type='submit']"));
        } catch (NoSuchElementException e) {
            submit = driver.findElement(By.tagName("button"));
        }
        submit.click();

        Thread.sleep(500);

        // Verify success header
        WebElement header = driver.findElement(By.tagName("h2"));
        Assertions.assertTrue(header.getText().contains("Attendance Logged Successfully"), "Success header not found");

        // Extract displayed student name and submitted time
        WebElement nameDiv = driver
                .findElement(By.xpath("//strong[normalize-space()='Student Name:']/following-sibling::div"));
        WebElement timeDiv = driver
                .findElement(By.xpath("//strong[normalize-space()='Time Submitted:']/following-sibling::div"));

        String displayedName = nameDiv.getText().trim();
        String displayedTime = timeDiv.getText().trim();

        // Assert name matches selection
        Assertions.assertEquals(selectedName, displayedName, "Displayed student name does not match selected name");

        // Validate timestamp format and that it's recent (assume UTC trimmed to
        // seconds)
        Assertions.assertTrue(displayedTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "Timestamp format invalid: " + displayedTime);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(displayedTime, fmt);
        Instant displayedInstant = ldt.atOffset(ZoneOffset.UTC).toInstant();
        Instant now = Instant.now();
        long secondsDiff = Math.abs(Duration.between(displayedInstant, now).getSeconds());

        Assertions.assertTrue(secondsDiff <= 15,
                "Timestamp differs from now by more than 15 seconds (diff=" + secondsDiff + "s)");
    }
}
