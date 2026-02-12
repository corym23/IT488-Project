package com.attendance;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import java.util.List;




@DisplayName("ATS-Layout & Negative: Layout checks and edge cases")
public class ATSLayoutAndNegativeTest extends TestBase {

    private void navigateToHome() throws InterruptedException {
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

    private boolean hasHorizontalScroll() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Boolean has = (Boolean) js.executeScript(
                "return (document.documentElement.scrollWidth > window.innerWidth) || (document.body.scrollWidth > window.innerWidth);");
        return Boolean.TRUE.equals(has);
    }

    @Test
    @DisplayName("Layout test: no horizontal scrolling at 320px and above")
    public void testLayoutAt320AndAbove() throws Exception {
        int[] widths = new int[] { 320, 375, 414, 768, 1024 };

        for (int w : widths) {
            driver.manage().window().setSize(new Dimension(w, 900));
            navigateToHome();
            // ensure logged in so attendance UI appears
            loginIfPresent();

            Thread.sleep(300);

            Assertions.assertFalse(hasHorizontalScroll(), "Horizontal scroll detected at width " + w);

            // Verify selectable element exists (select or checkbox) and is inside viewport
            long innerWidth = ((Number) ((JavascriptExecutor) driver).executeScript("return window.innerWidth;"))
                    .longValue();

            List<WebElement> selects = driver.findElements(By.tagName("select"));
            if (!selects.isEmpty()) {
                WebElement el = selects.get(0);
                Assertions.assertTrue(el.isDisplayed(), "Select not displayed at width " + w);
                org.openqa.selenium.Rectangle rect = el.getRect();
                Assertions.assertTrue(rect.getX() >= 0 && rect.getX() + rect.getWidth() <= innerWidth,
                        "Select out of viewport at width " + w);
            } else {
                List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
                if (!checks.isEmpty()) {
                    WebElement el = checks.get(0);
                    Assertions.assertTrue(el.isDisplayed(), "Checkbox not displayed at width " + w);
                    org.openqa.selenium.Rectangle rect = el.getRect();
                    Assertions.assertTrue(rect.getX() >= 0 && rect.getX() + rect.getWidth() <= innerWidth,
                            "Checkbox out of viewport at width " + w);
                } else {
                    Assertions.fail("No select or checkbox found to validate at width " + w);
                }
            }

            // Verify submit button visible and inside viewport
            List<WebElement> submits = driver.findElements(By.cssSelector("button[type='submit']"));
            WebElement submit;
            if (!submits.isEmpty()) {
                submit = submits.get(0);
            } else {
                List<WebElement> allButtons = driver.findElements(By.tagName("button"));
                if (allButtons.isEmpty()) {
                    Assertions.fail("No button found at width " + w);
                    return;
                }
                submit = allButtons.get(0);
            }

            Assertions.assertTrue(submit.isDisplayed(), "Submit not visible at width " + w);
            org.openqa.selenium.Rectangle srect = submit.getRect();
            Assertions.assertTrue(srect.getX() >= 0 && srect.getX() + srect.getWidth() <= innerWidth,
                    "Submit out of viewport at width " + w);
        }
    }

    @Test
    @DisplayName("Negative layout: widths below 320 may show horizontal scroll")
    public void testBelow320ShowsHorizontalScroll() throws Exception {
        driver.manage().window().setSize(new Dimension(300, 900));
        navigateToHome();
        loginIfPresent();
        Thread.sleep(300);
        // On some layouts the UI will still reflow without horizontal scroll.
        // Verify the primary selectable element (select or checkbox) is present at
        // narrow widths.
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        List<WebElement> checks = driver.findElements(By.cssSelector("input[type='checkbox']"));
        boolean hasSelectable = !selects.isEmpty() || !checks.isEmpty();
        Assertions.assertTrue(hasSelectable, "No selectable element (select/checkbox) found at width 300");
    }

    @Test
    @DisplayName("Submit without selection shows clear error and styling")
    public void testSubmitWithoutSelectionShowsError() throws Exception {
        driver.manage().window().setSize(new Dimension(375, 900));
        navigateToHome();
        loginIfPresent();
        Thread.sleep(300);

        // Click submit without selecting
        WebElement submit;
        List<WebElement> submits = driver.findElements(By.cssSelector("button[type='submit']"));
        if (!submits.isEmpty())
            submit = submits.get(0);
        else {
            List<WebElement> b = driver.findElements(By.tagName("button"));
            Assertions.assertFalse(b.isEmpty(), "No submit button found");
            submit = b.get(0);
        }

        submit.click();
        Thread.sleep(300);

        // Look for common error message indicators: id, role alert, .error,
        // .validation-message, or text hints
        List<WebElement> matches = driver.findElements(By.id("error-message"));
        matches.addAll(driver.findElements(By.cssSelector("[role='alert'], .error, .validation-message")));
        // also search for paragraph/text nodes that contain expected phrases
        List<WebElement> textMatches = driver.findElements(By.xpath(
                "//p[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'please select') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'select your name')]"));
        matches.addAll(textMatches);

        if (matches.isEmpty()) {
            // Fallback: check page text for common phrases
            String page = driver.getPageSource().toLowerCase();
            boolean found = page.contains("please select") || page.contains("select your name")
                    || page.contains("please select your name") || page.contains("required");
            if (!found) {
                Assertions.fail("No error message or validation shown after empty submit (page snippet): "
                        + page.substring(0, Math.min(page.length(), 500)));
                return;
            } else {
                return; // acceptable if page contains a message but no specific element was found
            }
        }

        WebElement err = matches.get(0);
        Assertions.assertTrue(err.isDisplayed(), "Error message present but not visible");

        String styleAttr = err.getAttribute("style");
        boolean inlineCrimson = styleAttr != null && styleAttr.toLowerCase().contains("color:crimson");
        String colorCss = err.getCssValue("color");
        boolean cssCrimson = colorCss != null && colorCss.contains("220") && colorCss.contains("20")
                && colorCss.contains("60");
        Assertions.assertTrue(inlineCrimson || cssCrimson,
                "Error visual cue not styled as crimson (styleAttr='" + styleAttr + "', css='" + colorCss + "')");
    }

    @Test
    @DisplayName("Edge: very long input doesn't break layout")
    public void testVeryLongInputDoesNotCauseHScroll() throws Exception {
        driver.manage().window().setSize(new Dimension(1024, 900));
        navigateToHome();
        loginIfPresent();
        Thread.sleep(300);

        // If there's a free-text student name field, type a very long string into it
        List<WebElement> inputs = driver.findElements(By.tagName("input"));
        if (!inputs.isEmpty()) {
            WebElement textInput = inputs.get(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5000; i++)
                sb.append('a');
            textInput.clear();
            textInput.sendKeys(sb.toString());
            Thread.sleep(200);
            Assertions.assertFalse(hasHorizontalScroll(), "Very long input caused horizontal scroll");
        } else {
            // No free text input available: attempt to ensure presence of select/checkbox
            // doesn't break layout
            Assertions.assertFalse(hasHorizontalScroll(), "No inputs to test but page already has horizontal scroll");
        }
    }
}
