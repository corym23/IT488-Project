package com.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

@DisplayName("Login negative & edge-case tests")
public class LoginErrorTest extends TestBase {

    private void navigateToLogin() throws InterruptedException {
        String url = "http://localhost:5173/home";
        driver.get(url);
        Thread.sleep(400);
    }

    private List<WebElement> findLoginInputs() throws InterruptedException {
        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        if (inputs.size() < 2) {
            try {
                WebElement signIn = driver.findElement(By.xpath(
                        "//button[normalize-space()='Sign In' or @aria-label='Sign in to Attendance Tracking Application']"));
                signIn.click();
                Thread.sleep(300);
                inputs = driver.findElements(By.tagName("input"));
            } catch (Exception e) {
                // ignore and return whatever found
            }
        }
        return inputs;
    }

    private void submitCredentials(String name, String password) throws Exception {
        navigateToLogin();
        List<WebElement> inputs = findLoginInputs();
        if (inputs.size() < 2) {
            Assertions.fail("Login inputs not found on page");
        }
        WebElement nameInput = inputs.get(0);
        WebElement passwordInput = inputs.get(1);
        nameInput.clear();
        passwordInput.clear();
        if (name != null && !name.isEmpty())
            nameInput.sendKeys(name);
        if (password != null && !password.isEmpty())
            passwordInput.sendKeys(password);

        WebElement loginButton = null;
        try {
            loginButton = driver.findElement(By.xpath(
                    "//button[normalize-space()='Log In' or normalize-space()='Login' or @aria-label='Log In' or @aria-label='Login' or @type='submit']"));
        } catch (Exception e) {
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            if (buttons.isEmpty()) {
                Assertions.fail("No button found to submit login");
            }
            loginButton = buttons.get(0);
        }

        loginButton.click();
        Thread.sleep(600);
    }

    private boolean attendanceShown() {
        String page = driver.getPageSource();
        boolean hasRosterControls = driver.findElements(By.cssSelector("select, input[type='checkbox']")).size() > 0;
        return page.contains("Type Student Name") || page.contains("OR Select The Name") || hasRosterControls;
    }

    private void assertLoginRejected(String caseDesc) {
        // If attendance UI appears, treat as unexpected success
        if (attendanceShown()) {
            Assertions.fail("Login unexpectedly succeeded for case: " + caseDesc);
        }

        // Prefer an explicit error indicator when available
        List<WebElement> alerts = driver
                .findElements(By.cssSelector("[role='alert'], .error, .alert, .validation-message"));
        if (!alerts.isEmpty())
            return; // good: explicit error shown

        // Otherwise accept common error text on page
        String page = driver.getPageSource().toLowerCase();
        Assertions.assertTrue(
                page.contains("invalid") || page.contains("required") || page.contains("incorrect")
                        || page.contains("please enter") || page.contains("error"),
                "No error message or validation shown for case: " + caseDesc);
    }

    @Test
    @DisplayName("Empty credentials should be rejected")
    public void testEmptyCredentialsRejected() throws Exception {
        submitCredentials("", "");
        assertLoginRejected("empty credentials");
    }

    @Test
    @DisplayName("Whitespace-only credentials should be rejected")
    public void testWhitespaceCredentialsRejected() throws Exception {
        submitCredentials("   ", "   ");
        assertLoginRejected("whitespace credentials");
    }

    @Test
    @DisplayName("Invalid credentials should be rejected")
    public void testInvalidCredentialsRejected() throws Exception {
        submitCredentials("not-a-user", "bad-password");
        assertLoginRejected("invalid credentials");
    }

    @Test
    @DisplayName("SQL injection attempt should not bypass login")
    public void testSqlInjectionRejected() throws Exception {
        submitCredentials("' OR '1'='1'", "anything");
        assertLoginRejected("sql injection");
    }

    @Test
    @DisplayName("Very long credentials should be handled (edge) and rejected")
    public void testVeryLongCredentialsRejected() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++)
            sb.append('a');
        String longStr = sb.toString();
        submitCredentials(longStr, longStr);
        assertLoginRejected("very long credentials");
    }

    @Test
    @DisplayName("Special characters / XSS-like input should be rejected")
    public void testSpecialCharactersRejected() throws Exception {
        submitCredentials("<script>alert(1)</script>", "!@#$%^&*()_+");
        assertLoginRejected("special chars/xss");
    }
}
