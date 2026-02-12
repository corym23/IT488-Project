package com.attendance;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.List;

@DisplayName("ATS-Layout: Verify primary submission screen from 320px and above")
public class ScreenResponsivenessTest {
    private WebDriver driver;

    @BeforeEach
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless=new");
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
    @DisplayName("Layout test: no horizontal scrolling at 320px and above")
    public void testLayoutAt320AndAbove() throws Exception {
        String url = "http://localhost:5173/home";

        // First, navigate and attempt to log in so the attendance UI is visible for
        // checks.
        driver.manage().window().setSize(new Dimension(1024, 900));
        driver.get(url);
        Thread.sleep(500);
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
                    // ignore
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

        int[] widths = new int[] { 320, 375, 414, 768, 1024 };
        for (int w : widths) {
            // set viewport and load page
            driver.manage().window().setSize(new Dimension(w, 900));
            driver.get(url);
            Thread.sleep(500);

            // If the app starts on Home (no roster inputs), try to click Sign In to reveal
            // the attendance UI
            try {
                List<WebElement> inputs = driver.findElements(By.tagName("input"));
                if (inputs.size() < 1) {
                    try {
                        WebElement signIn = driver.findElement(By.xpath(
                                "//button[normalize-space()='Sign In' or @aria-label='Sign in to Attendance Tracking Application']"));
                        signIn.click();
                        Thread.sleep(300);
                    } catch (Exception ex) {
                        // ignore, continue checks
                    }
                }
            } catch (Exception ignored) {
            }

            JavascriptExecutor js = (JavascriptExecutor) driver;

            long scrollWidth = ((Number) js
                    .executeScript("return Math.max(document.documentElement.scrollWidth, document.body.scrollWidth);"))
                    .longValue();
            long innerWidth = ((Number) js.executeScript("return window.innerWidth;")).longValue();
            boolean hasHScroll = (Boolean) js.executeScript(
                    "return (document.documentElement.scrollWidth > window.innerWidth) || (document.body.scrollWidth > window.innerWidth);");

            Assertions.assertFalse(hasHScroll,
                    String.format("Horizontal scroll detected at width %d (scrollWidth=%d, innerWidth=%d)", w,
                            scrollWidth, innerWidth));

            // Verify primary selectable element is visible and inside viewport
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
                    Assertions.fail("No select or radio found to validate at width " + w);
                }
            }

            // Verify submit button is visible and inside viewport
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
}
