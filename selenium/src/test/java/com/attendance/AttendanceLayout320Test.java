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
public class AttendanceLayout320Test {
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
    @DisplayName("Layout test: no horizontal scrolling at 320px and above")
    public void testLayoutAt320AndAbove() throws Exception {
        String url = "http://localhost:5173";

        int[] widths = new int[] { 320, 375, 414, 768, 1024 };
        for (int w : widths) {
            // set viewport and load page
            driver.manage().window().setSize(new Dimension(w, 900));
            driver.get(url);
            Thread.sleep(500);

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
                List<WebElement> radios = driver.findElements(By.cssSelector("input[type='radio']"));
                if (!radios.isEmpty()) {
                    WebElement el = radios.get(0);
                    Assertions.assertTrue(el.isDisplayed(), "Radio not displayed at width " + w);
                    org.openqa.selenium.Rectangle rect = el.getRect();
                    Assertions.assertTrue(rect.getX() >= 0 && rect.getX() + rect.getWidth() <= innerWidth,
                            "Radio out of viewport at width " + w);
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
