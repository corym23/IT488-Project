package com.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@DisplayName("ATS-Confirm: Verify submission confirmation shows name and UTC timestamp")
public class SubmissionConfirmationTest extends TestBase {

    @Test
    public void testConfirmationDisplaysNameAndTimestamp() throws Exception {
        String url = "http://localhost:5173/home";
        driver.get(url);
        Thread.sleep(500);

        // --- Login step: attempt to sign in with demo credentials. The app may
        // show a Home screen with a 'Sign In' CTA, so click that first if needed.
        try {
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
        }

        // Select a name from dropdown, radio, or checkbox and remember the value
        String selectedName = null;
        boolean selected = false;

        // try select dropdown
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

        // try radio
        if (!selected) {
            try {
                WebElement radio = driver.findElement(By.cssSelector("input[type='radio']"));
                radio.click();
                selectedName = radio.findElement(By.xpath("..")).getText().trim();
                selected = true;
            } catch (NoSuchElementException ex) {
                // no radio
            }
        }

        // try checkbox (the app uses checkboxes for selection)
        if (!selected) {
            try {
                WebElement checkbox = driver.findElement(By.cssSelector("input[type='checkbox']"));
                checkbox.click();
                // label text contains the name
                selectedName = checkbox.findElement(By.xpath("..")).getText().trim();
                selected = true;
            } catch (NoSuchElementException ex) {
                // no checkbox
            }
        }

        if (!selected) {
            Assertions.fail("No selectable name found for confirmation test");
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

        // Extract displayed submitted time from the block that contains 'Time
        // Submitted:'
        WebElement timeBlock = driver.findElement(By.xpath("//div[contains(., 'Time Submitted:')]"));
        String timeText = timeBlock.getText();
        // Expect something like 'Time Submitted: 2026-02-03 22:00:00'
        String displayedTime = null;
        int idx = timeText.indexOf("Time Submitted:");
        if (idx >= 0) {
            displayedTime = timeText.substring(idx + "Time Submitted:".length()).trim();
            // in case there are other lines, take first token-ish part
            if (displayedTime.contains("\n")) {
                displayedTime = displayedTime.split("\\n")[0].trim();
            }
        }

        Assertions.assertNotNull(displayedTime, "Could not locate displayed submission time");

        // Verify selected name appears in the Selected Students area
        boolean foundName = false;
        List<WebElement> selectedBoxes = driver.findElements(By.xpath(
                "//div[.//strong[normalize-space()='Selected Students:']]/following-sibling::div | //div[contains(., 'Selected Students:')]/following-sibling::div"));
        // Fallback: search for any element that contains the name text
        if (selectedBoxes.isEmpty()) {
            List<WebElement> any = driver.findElements(By.xpath("//*[contains(text(), '" + selectedName + "')]"));
            foundName = !any.isEmpty();
        } else {
            for (WebElement el : selectedBoxes) {
                if (el.getText().trim().equals(selectedName)) {
                    foundName = true;
                    break;
                }
            }
        }

        Assertions.assertTrue(foundName, "Selected student name not displayed in confirmation: " + selectedName);

        // Validate timestamp format and that it's recent (assume UTC trimmed to
        // seconds)
        Assertions.assertTrue(displayedTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "Timestamp format invalid: " + displayedTime);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(displayedTime, fmt);
        Instant displayedInstant = ldt.atOffset(ZoneOffset.UTC).toInstant();
        Instant now = Instant.now();
        long secondsDiff = Math.abs(Duration.between(displayedInstant, now).getSeconds());

        Assertions.assertTrue(secondsDiff <= 30,
                "Timestamp differs from now by more than 30 seconds (diff=" + secondsDiff + "s)");
    }
}
