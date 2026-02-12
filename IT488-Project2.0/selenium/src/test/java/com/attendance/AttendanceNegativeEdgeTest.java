package com.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import java.util.List;

@DisplayName("Attendance: Negative & Edge-case tests")
public class AttendanceNegativeEdgeTest extends TestBase {

    private void navigate() throws InterruptedException {
        driver.get("http://localhost:5173/home");
        Thread.sleep(400);
    }

    private void loginIfPresent() throws InterruptedException {
        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        if (inputs.size() < 2) {
            try {
                WebElement signIn = driver.findElement(By.xpath(
                        "//button[normalize-space()='Sign In' or @aria-label='Sign in to Attendance Tracking Application']"));
                signIn.click();
                Thread.sleep(300);
                inputs = driver.findElements(By.tagName("input"));
            } catch (Exception ignored) {
            }
        }

        if (inputs.size() >= 2) {
            WebElement nameInput = inputs.get(0);
            WebElement passwordInput = inputs.get(1);
            nameInput.clear();
            passwordInput.clear();
            nameInput.sendKeys("Ahmad Kassem");
            passwordInput.sendKeys("IT488");

            WebElement loginBtn = null;
            try {
                loginBtn = driver.findElement(By.xpath(
                        "//button[normalize-space()='Log In' or normalize-space()='Login' or @type='submit']"));
            } catch (Exception e) {
                List<WebElement> buttons = driver.findElements(By.tagName("button"));
                if (!buttons.isEmpty())
                    loginBtn = buttons.get(0);
            }

            if (loginBtn != null) {
                loginBtn.click();
                Thread.sleep(400);
            }
        }
    }

    @Test
    @DisplayName("Empty submit shows validation message")
    public void testEmptySubmitShowsValidation() throws Exception {
        navigate();
        loginIfPresent();

        // Click submit without selecting or typing
        WebElement submit;
        List<WebElement> submits = driver.findElements(By.cssSelector("button[type='submit']"));
        if (!submits.isEmpty())
            submit = submits.get(0);
        else
            submit = driver.findElement(By.tagName("button"));

        submit.click();
        Thread.sleep(300);

        List<WebElement> matches = driver.findElements(By.id("error-message"));
        Assertions.assertFalse(matches.isEmpty(), "Expected inline validation element after empty submit");
        WebElement err = matches.get(0);
        Assertions.assertTrue(err.isDisplayed(), "Error message should be visible");
        Assertions.assertEquals("please enter or select a name from the list.", err.getText().trim().toLowerCase());
    }

    @Test
    @DisplayName("Typing into search clears the validation")
    public void testTypingClearsValidation() throws Exception {
        navigate();
        loginIfPresent();

        // trigger validation
        WebElement submit = driver.findElements(By.cssSelector("button[type='submit']")).get(0);
        submit.click();
        Thread.sleep(300);

        WebElement search = driver.findElements(By.tagName("input")).get(0);
        search.sendKeys("a");
        Thread.sleep(200);

        List<WebElement> matches = driver.findElements(By.id("error-message"));
        if (!matches.isEmpty()) {
            Assertions.assertTrue(matches.get(0).getText().trim().isEmpty(),
                    "Error message should be cleared after typing");
        }
    }

    @Test
    @DisplayName("Selecting a checkbox clears the validation")
    public void testCheckboxSelectionClearsValidation() throws Exception {
        navigate();
        loginIfPresent();

        // trigger validation
        WebElement submit = driver.findElements(By.cssSelector("button[type='submit']")).get(0);
        submit.click();
        Thread.sleep(300);

        // click first checkbox if present
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        Assertions.assertFalse(checks.isEmpty(), "No checkboxes available to test selection");
        checks.get(0).click();
        Thread.sleep(200);

        List<WebElement> matches = driver.findElements(By.id("error-message"));
        if (!matches.isEmpty()) {
            Assertions.assertTrue(matches.get(0).getText().trim().isEmpty(),
                    "Error message should be cleared after selecting a name");
        }
    }

    @Test
    @DisplayName("Very long search input does not cause horizontal scroll")
    public void testVeryLongSearchNoHScroll() throws Exception {
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1024, 900));
        navigate();
        loginIfPresent();
        Thread.sleep(300);

        WebElement search = driver.findElements(By.tagName("input")).get(0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++)
            sb.append('x');
        search.clear();
        search.sendKeys(sb.toString());
        Thread.sleep(200);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        Boolean has = (Boolean) js.executeScript(
                "return (document.documentElement.scrollWidth > window.innerWidth) || (document.body.scrollWidth > window.innerWidth);");
        Assertions.assertFalse(Boolean.TRUE.equals(has), "Very long input caused horizontal scroll");
    }

    @Test
    @DisplayName("Special-character search does not crash and roster UI remains")
    public void testSpecialCharsSearchSafety() throws Exception {
        navigate();
        loginIfPresent();
        Thread.sleep(200);

        WebElement search = driver.findElements(By.tagName("input")).get(0);
        search.clear();
        search.sendKeys("<script>alert(1)</script>");
        Thread.sleep(200);

        // Verify the roster list area still exists (role=radiogroup)
        List<WebElement> groups = driver.findElements(By.cssSelector("div[role='radiogroup']"));
        Assertions.assertFalse(groups.isEmpty(), "Roster list not present after special-character search");
    }
}
