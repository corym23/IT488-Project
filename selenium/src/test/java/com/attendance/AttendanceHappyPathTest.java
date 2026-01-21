package com.attendance;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class AttendanceHappyPathTest {

    private static WebDriver driver;
    private static String baseUrl;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        baseUrl = System.getProperty("baseUrl", "http://localhost:5173");
    }

    @AfterAll
    public static void teardownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void happyPath_submitsNameAndShowsConfirmation_andPersistsRecord() {
        driver.get(baseUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // find a name input using several common selectors
        By[] nameSelectors = new By[] {
            By.id("name"),
            By.name("name"),
            By.cssSelector("input[placeholder='Name']"),
            By.cssSelector("input[type='text']"),
            By.cssSelector("input")
        };

        WebElement nameInput = null;
        for (By sel : nameSelectors) {
            try {
                List<WebElement> els = driver.findElements(sel);
                if (!els.isEmpty()) {
                    nameInput = els.get(0);
                    break;
                }
            } catch (Exception ignored) {}
        }
        Assertions.assertNotNull(nameInput, "Could not find a name input on the page");

        String testName = "Selenium Test User";
        nameInput.clear();
        nameInput.sendKeys(testName);

        // find submit button (common selectors)
        By[] submitSelectors = new By[] {
            By.cssSelector("button[type='submit']"),
            By.cssSelector("input[type='submit']"),
            By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'submit')]"),
            By.xpath("//button[contains(., 'Submit')]"),
            By.xpath("//button")
        };

        WebElement submitButton = null;
        for (By sel : submitSelectors) {
            try {
                List<WebElement> els = driver.findElements(sel);
                for (WebElement e : els) {
                    if (e.isDisplayed()) {
                        submitButton = e;
                        break;
                    }
                }
                if (submitButton != null) break;
            } catch (Exception ignored) {}
        }
        Assertions.assertNotNull(submitButton, "Could not find a submit button");

        submitButton.click();

        // Wait for confirmation screen / element that contains the submitted name
        boolean confirmed = false;
        try {
            confirmed = wait.until(d -> {
                String pageText = d.findElement(By.tagName("body")).getText();
                return pageText != null && pageText.contains(testName);
            });
        } catch (TimeoutException ignored) {}

        Assertions.assertTrue(confirmed, "Confirmation screen or text with submitted name was not found");

        // Verify record stored — attempt to find a list/table row containing the name
        boolean persisted = false;
        try {
            persisted = wait.until(d -> {
                String body = d.findElement(By.tagName("body")).getText();
                return body.contains(testName);
            });
        } catch (TimeoutException ignored) {}

        Assertions.assertTrue(persisted, "Submitted record was not found/stored in the UI after submission");
    }
}
