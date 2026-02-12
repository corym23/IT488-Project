package com.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import java.util.List;

/*
    * Unambiguous error feedback test for ATS-56 CM

 */

@DisplayName("ATS-Error: Unambiguous error feedback tests")
public class UnambiguousErrorFeedbackTest extends TestBase {

    @Test
    public void testSubmitWithoutSelectionShowsClearErrorAndIsStyled() throws Exception {
        String url = "http://localhost:5173/home";
        driver.get(url);
        Thread.sleep(400);

        // --- Login step: enter demo credentials and sign in ---
        try {
            // find the two inputs (name, password). If not present, click Home 'Sign In'.
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            if (inputs.size() < 2) {
                try {
                    WebElement signIn = driver.findElement(By.xpath(
                            "//button[normalize-space()='Sign In' or @aria-label='Sign in to Attendance Tracking Application']"));
                    signIn.click();
                    Thread.sleep(300);
                    inputs = driver.findElements(By.tagName("input"));
                } catch (Exception ex) {
                    // continue; inputs may still be missing
                }
            }

            if (inputs.size() >= 2) {
                WebElement nameInput = inputs.get(0);
                WebElement passwordInput = inputs.get(1);
                nameInput.sendKeys("Ahmad Kassem");
                passwordInput.sendKeys("IT488");

                // click Login button (by text) if present
                WebElement loginBtn = null;
                try {
                    loginBtn = driver
                            .findElement(By.xpath("//button[normalize-space()='Log In' or normalize-space()='Login']"));
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
            // continue; if login isn't present, the test will proceed (existing checks may
            // fail)
        }

        // Ensure no selection is made, click the submit button
        WebElement submit;
        try {
            submit = driver.findElement(By.cssSelector("button[type='submit']"));
        } catch (NoSuchElementException e) {
            submit = driver.findElement(By.tagName("button"));
        }
        submit.click();

        Thread.sleep(300);

        // Locate error message by id (app now renders inline error-message)
        List<WebElement> matches = driver.findElements(By.id("error-message"));
        Assertions.assertFalse(matches.isEmpty(), "Expected error message not found after empty submit");

        WebElement err = matches.get(0);
        Assertions.assertTrue(err.isDisplayed(), "Error message is present but not visible");

        // Visual cue: check inline style contains color:crimson OR CSS color contains
        // '220' (crimson rgb)
        String styleAttr = err.getAttribute("style");
        boolean inlineCrimson = styleAttr != null && styleAttr.toLowerCase().contains("color:crimson");
        String colorCss = err.getCssValue("color");
        boolean cssCrimson = colorCss != null && colorCss.contains("220") && colorCss.contains("20")
                && colorCss.contains("60");
        Assertions.assertTrue(inlineCrimson || cssCrimson,
                "Error visual cue not styled as crimson (styleAttr='" + styleAttr + "', css='" + colorCss + "')");

        // Interactivity: selecting a name should clear the message
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
            // no dropdown
        }

        if (!selected) {
            // App uses checkboxes for selection now
            WebElement checkbox = driver.findElement(By.cssSelector("input[type='checkbox']"));
            checkbox.click();
            selected = true;
        }

        Thread.sleep(250);

        // After selection, the error element should be removed or contain no text
        List<WebElement> postMatches = driver.findElements(By.id("error-message"));
        if (!postMatches.isEmpty()) {
            String txt = postMatches.get(0).getText();
            Assertions.assertTrue(txt == null || txt.trim().isEmpty(),
                    "Error message still present after selecting a name: '" + txt + "'");
        }
    }
}
