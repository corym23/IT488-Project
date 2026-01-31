package com.attendance;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

/*
    * Unambiguous error feedback test for ATS-56

 */

@DisplayName("ATS-Error: Unambiguous error feedback tests")
public class UnambiguousErrorFeedbackTest {
    private WebDriver driver;

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--remote-allow-origins=*");
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
    public void testSubmitWithoutSelectionShowsClearErrorAndIsStyled() throws Exception {
        String url = "http://localhost:5173";
        driver.get(url);
        Thread.sleep(400);

        // --- Login step: enter demo credentials and sign in ---
        try {
            // find the two inputs (name, password)
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            if (inputs.size() >= 2) {
                WebElement nameInput = inputs.get(0);
                WebElement passwordInput = inputs.get(1);
                nameInput.sendKeys("Ahmad Kassem");
                passwordInput.sendKeys("IT488");

                // click Login button (by text) if present
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

        // Locate error message by exact text
        String errText = "Please select your name.";
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
            WebElement radio = driver.findElement(By.cssSelector("input[type='radio']"));
            radio.click();
            selected = true;
        }

        Thread.sleep(250);

        // After selection, the error message should no longer be present or should be
        // empty
        List<WebElement> postMatches = driver.findElements(By.xpath("//p[normalize-space(text())='" + errText + "']"));
        Assertions.assertTrue(postMatches.isEmpty(), "Error message still present after selecting a name");
    }
}
