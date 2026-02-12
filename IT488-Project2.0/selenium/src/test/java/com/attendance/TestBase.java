package com.attendance;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;

public class TestBase {
    protected WebDriver driver;

    @BeforeEach
    public void setup() {
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        try {
            switch (browser) {
                case "edge":
                    WebDriverManager.edgedriver().setup();
                    EdgeOptions eOpt = new EdgeOptions();
                    eOpt.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                    driver = new EdgeDriver(eOpt);
                    break;
                case "safari":
                    try {
                        driver = new SafariDriver();
                    } catch (Exception e) {
                        // Fallback to Chrome if SafariDriver not available
                        WebDriverManager.chromedriver().setup();
                        ChromeOptions cOpt = new ChromeOptions();
                        cOpt.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                        driver = new ChromeDriver(cOpt);
                    }
                    break;
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                    driver = new ChromeDriver(options);
                    break;
            }
        } catch (Exception e) {
            // In case driver creation fails, ensure it's null so tests fail fast
            driver = null;
            throw e;
        }
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
        }
    }
}
