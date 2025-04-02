package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GradeControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            (System.getProperty("os.name").toLowerCase().contains("mac")) ?
                    "drivers/chromedriver" : "drivers/chromedriver.exe";
    /*public static final String CHROME_DRIVER_FILE_LOCATION = "/Users/harris/Documents/chromedriver-mac-arm64/chromedriver";*/

    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.

    WebDriver driver;

    @BeforeEach
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);
    }
    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestInstructorGradesAssignment() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.linkText("Show Sections")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement assignmentsLink = driver.findElement(By.linkText("View Assignments"));
        assignmentsLink.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement assignmentRow = driver.findElement(By.xpath("//tr[td[contains(text(),'db homework 1')]]"));
        List<WebElement> buttons = assignmentRow.findElements(By.tagName("button"));
        buttons.get(0).click();  // assuming the "Grade" button is the first button
        Thread.sleep(SLEEP_DURATION);

        List<WebElement> gradeInputs = driver.findElements(By.name("score"));
        String os = System.getProperty("os.name").toLowerCase();                // Mac/Win conditional
        Keys modifierKey = (os.contains("mac")) ? Keys.COMMAND : Keys.CONTROL;  // Set Command or Control key
        for (WebElement gradeInput : gradeInputs) {
            gradeInput.clear();
            gradeInput.sendKeys(Keys.chord(modifierKey,"a", Keys.DELETE));  // use modifierKey
            gradeInput.sendKeys("90"); // example grade
        }
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.xpath("//button[contains(text(),'Save')]")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement message = driver.findElement(By.xpath("//h4[contains(text(),'Grades successfully saved.')]"));
        assertTrue(message.getText().startsWith("Grades successfully saved."), "Grades save confirmation message should appear");

        driver.findElement(By.xpath("//button[contains(text(),'Close')]")).click();
    }

}
