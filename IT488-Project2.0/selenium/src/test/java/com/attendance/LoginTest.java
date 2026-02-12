package com.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Login test
 * - Opens the web UI
 * - Enters hard-coded credentials (Ahmad Kassem / IT488)
 * - Clicks the Login button
 * - Verifies the attendance UI is displayed
 */
@DisplayName("Login flow tests")
public class LoginTest extends TestBase {

    @Test
    @DisplayName("Login with demo credentials and show attendance UI")
    public void testLoginShowsAttendance() throws Exception {
        String url = "http://localhost:5173/home";
        driver.get(url);

        // allow app to render
        Thread.sleep(500);

        // Find visible input fields (login screen should show two inputs: name and
        // password). App may start on a Home screen with a 'Sign In' CTA, so
        // click that first if inputs are missing.
        java.util.List<WebElement> inputs = driver.findElements(By.tagName("input"));
        if (inputs.size() < 2) {
            try {
                WebElement signIn = driver.findElement(By.xpath(
                        "//button[normalize-space()='Sign In' or @aria-label='Sign in to Attendance Tracking Application']"));
                signIn.click();
                Thread.sleep(300);
                inputs = driver.findElements(By.tagName("input"));
            } catch (Exception e) {
                // still missing -> fail below
            }
        }

        if (inputs.size() < 2) {
            Assertions.fail("Login inputs not found on page");
        }

        WebElement nameInput = inputs.get(0);
        WebElement passwordInput = inputs.get(1);

        // Enter the hard-coded credentials
        nameInput.sendKeys("Ahmad Kassem");
        passwordInput.sendKeys("IT488");

        // Click the Login button (accept 'Log In' or 'Login' text, fallback to first
        // button)
        WebElement loginButton = null;
        try {
            loginButton = driver.findElement(By.xpath(
                    "//button[normalize-space()='Log In' or normalize-space()='Login' or @aria-label='Log In' or @aria-label='Login' or @type='submit']"));
        } catch (Exception e) {
            // fallback: first button
            java.util.List<WebElement> buttons = driver.findElements(By.tagName("button"));
            if (buttons.isEmpty()) {
                Assertions.fail("No button found to submit login");
            }
            loginButton = buttons.get(0);
        }

        loginButton.click();

        // allow navigation/render
        Thread.sleep(800);

        // After successful login the attendance UI should be visible. Check for
        // known UI text or presence of roster controls (select or checkboxes).
        String page = driver.getPageSource();
        boolean hasRosterControls = driver.findElements(By.cssSelector("select, input[type='checkbox']")).size() > 0;
        Assertions.assertTrue(
                page.contains("Type Student Name") || page.contains("OR Select The Name") || hasRosterControls,
                "Attendance UI not shown after login");
    }
}
