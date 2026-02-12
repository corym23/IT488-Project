package com.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import java.time.Instant;
import java.util.List;

@DisplayName("Attendance: Additional practical tests")
public class AdditionalAttendanceTests extends TestBase {

    private void navigateHome() throws InterruptedException {
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
                loginBtn = driver.findElement(By
                        .xpath("//button[normalize-space()='Log In' or normalize-space()='Login' or @type='submit']"));
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
    @DisplayName("Already-logged names are disabled after reload")
    public void testAlreadyLoggedDisable() throws Exception {
        navigateHome();
        loginIfPresent();
        Thread.sleep(300);

        // find first available name
        List<WebElement> labels = driver.findElements(By.xpath("//label[input[@type='checkbox']]"));
        Assertions.assertFalse(labels.isEmpty(), "No names found in roster to test");
        String name = labels.get(0).getText().replace("(already logged)", "").trim();

        // set sessionStorage to mark this name as logged today
        String iso = Instant.now().toString();
        String js = String.format(
                "window.sessionStorage.setItem('cumulativeSubmitted', JSON.stringify({\"%s\":[\"%s\"]}));", name, iso);
        ((JavascriptExecutor) driver).executeScript(js);

        // reload page to pick up sessionStorage
        driver.get("http://localhost:5173/home");
        Thread.sleep(400);

        // find checkbox for the name and assert disabled or already-logged text
        List<WebElement> inputs = driver
                .findElements(By.xpath("//label[contains(., '" + name + "')]/input[@type='checkbox']"));
        Assertions.assertFalse(inputs.isEmpty(), "Checkbox for name not found after reload");
        WebElement cb = inputs.get(0);
        Assertions
                .assertTrue(
                        !cb.isEnabled() || driver
                                .findElements(By.xpath(
                                        "//label[contains(., '" + name + "') and contains(., '(already logged)')]"))
                                .size() > 0,
                        "Name should be disabled or marked as already logged");
    }

    @Test
    @DisplayName("Logout clears stored session data")
    public void testLogoutClearsSession() throws Exception {
        navigateHome();
        loginIfPresent();
        Thread.sleep(300);

        // set a test key
        ((JavascriptExecutor) driver).executeScript("window.sessionStorage.setItem('testKey','testVal');");

        // click logout button in nav
        WebElement logout = driver.findElement(By.xpath("//button[normalize-space()='Logout']"));
        logout.click();
        Thread.sleep(200);

        // verify sessionStorage no longer contains testKey or cumulativeSubmitted
        Object val = ((JavascriptExecutor) driver).executeScript("return window.sessionStorage.getItem('testKey');");
        Assertions.assertNull(val, "Session key should be cleared after logout");
    }

    @Test
    @DisplayName("Submission network failure falls back to local save message")
    public void testApiFailureFallbackToLocal() throws Exception {
        navigateHome();
        loginIfPresent();
        Thread.sleep(300);

        // select first checkbox
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        Assertions.assertFalse(checks.isEmpty(), "No checkbox available to test submission fallback");
        checks.get(0).click();

        // override fetch to always fail
        ((JavascriptExecutor) driver).executeScript(
                "window.fetch = function(){ return Promise.reject('network'); }; window.fetch.__replaced = true;");

        // click submit
        WebElement submit = driver.findElements(By.cssSelector("button[type='submit']")).get(0);
        submit.click();
        Thread.sleep(600);

        // check inline message id
        List<WebElement> messages = driver.findElements(By.id("error-message"));
        Assertions.assertFalse(messages.isEmpty(), "Expected message element after network-failed submit");
        String txt = messages.get(0).getText().toLowerCase();
        Assertions.assertTrue(
                txt.contains("submission saved locally") || txt.contains("submission saved locally (network error)"),
                "Expected fallback submission message, got: " + txt);
    }
}
