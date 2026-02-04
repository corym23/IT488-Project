package com.attendance;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Login test
 * - Opens the web UI
 * - Enters hard-coded credentials (Ahmad Kassem / IT488)
 * - Clicks the Login button
 * - Verifies the attendance UI is displayed
 */
@DisplayName("Login flow tests")
public class LoginTest {
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
    @DisplayName("Login with demo credentials and show attendance UI")
    public void testLoginShowsAttendance() throws Exception {
        String url = "http://localhost:5173";
        driver.get(url);

        // allow app to render
        Thread.sleep(500);

        // Find visible input fields (login screen should show two inputs: name and
        // password)
        java.util.List<WebElement> inputs = driver.findElements(By.tagName("input"));
        if (inputs.size() < 2) {
            Assertions.fail("Login inputs not found on page");
        }

        WebElement nameInput = inputs.get(0);
        WebElement passwordInput = inputs.get(1);

        // Enter the hard-coded credentials
        nameInput.sendKeys("Ahmad Kassem");
        passwordInput.sendKeys("IT488");

        // Click the Login button (button text 'Login')
        WebElement loginButton = null;
        try {
            loginButton = driver.findElement(By.xpath("//button[normalize-space()='Login']"));
        } catch (Exception e) {
            // fallback: first button
            java.util.List<WebElement> buttons = driver.findElements(By.tagName("button"));
            if (buttons.isEmpty()) {
                Assertions.fail("No button found to submit login");
            }
            loginButton = buttons.get(0);
        }

        loginButton.click();

        Thread.sleep(500);

        // After successful login the attendance form should be visible
        String page = driver.getPageSource();
        Assertions.assertTrue(page.contains("Attendance Form") || page.contains("Select your name"),
                "Attendance UI not shown after login");
    }
}
