package com.attendance;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * UI acceptance test (summary)
 *
 * What it verifies:
 * - Opens the web UI (default `http://localhost:5173`).
 * - Selects a roster name from the dropdown (or a radio button fallback).
 * - Submits the attendance form.
 * - Asserts that the success confirmation "Attendance Logged Successfully"
 * appears.
 */
@DisplayName("Web UI tests")
public class UIAcceptanceTest {
    private WebDriver driver;

    @BeforeEach
    public void setup() {
        // Ensure chromedriver is available
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
    @DisplayName("ATS-47 - submit attendance through UI")
    public void testSubmitAttendance() throws Exception {
        System.out.println("Running ATS-47 test: Attendance Tracking System (ATS)");

        // Point test at local dev server (adjust if your environment uses a different
        // port)
        String url = "http://localhost:5173";
        driver.get(url);

        // small pause to allow client to render
        Thread.sleep(500);

        // --- Login step: enter demo credentials and sign in if login form present ---
        try {
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            if (inputs.size() >= 2) {
                WebElement nameInput = inputs.get(0);
                WebElement passwordInput = inputs.get(1);
                nameInput.sendKeys("Ahmad Kassem");
                passwordInput.sendKeys("IT488");

                WebElement loginBtn = null;
                try {
                    loginBtn = driver.findElement(By.xpath("//button[normalize-space()='Login']"));
                } catch (Exception ex) {
                    List<WebElement> buttons = driver.findElements(By.tagName("button"));
                    if (!buttons.isEmpty())
                        loginBtn = buttons.get(0);
                }

                if (loginBtn != null) {
                    loginBtn.click();
                    Thread.sleep(400);
                }
            }
        } catch (Exception ignored) {
        }

        // Try to select from dropdown first, otherwise click a radio
        boolean selected = false;
        try {
            WebElement select = driver.findElement(By.tagName("select"));
            select.click();
            List<WebElement> options = select.findElements(By.tagName("option"));
            for (WebElement option : options) {
                String val = option.getAttribute("value");
                if (val != null && !val.isEmpty()) {
                    option.click();
                    selected = true;
                    break;
                }
            }
        } catch (NoSuchElementException e) {
            // fallback to radio
        }

        if (!selected) {
            try {
                WebElement radio = driver.findElement(By.cssSelector("input[type='radio']"));
                radio.click();
                selected = true;
            } catch (NoSuchElementException ex) {
                Assertions.fail("No selectable name found for ATS-47 test");
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

        // Verify success message exists
        String page = driver.getPageSource();
        Assertions.assertTrue(page.contains("Attendance Logged Successfully"), "Success message not found - ATS-47");
    }
}
