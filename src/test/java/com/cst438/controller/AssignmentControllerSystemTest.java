package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentControllerSystemTest {

    // Set Chrome driver path based on OS (Mac vs Windows)
    public static final String CHROME_DRIVER_FILE_LOCATION =
            (System.getProperty("os.name").toLowerCase().contains("mac")) ?
                    "drivers/chromedriver" : "drivers/chromedriver.exe";

    // URL of the web application
    public static final String URL = "http://localhost:3000";
    // Delay between steps (in milliseconds)
    public static final int SLEEP_DURATION = 1000;

    // WebDriver and WebDriverWait for Selenium operations
    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    public void setup() throws Exception {
        // Configure and launch Chrome browser instance
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION); // wait for page to load
    }

    @AfterEach
    public void teardown() {
        // Close the browser after test completion
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testInstructorAddsNewAssignmentSuccessfully() throws Exception {
        // Wait for year and semester dropdowns to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("semester")));

        // Locate input fields for year and semester
        WebElement yearInput = driver.findElement(By.id("year"));
        WebElement semesterInput = driver.findElement(By.id("semester"));

        // Determine platform-specific key for select-all shortcut
        String os = System.getProperty("os.name").toLowerCase();
        Keys modifierKey = (os.contains("mac")) ? Keys.COMMAND : Keys.CONTROL;

        // Clear existing text and input target year and semester
        yearInput.sendKeys(Keys.chord(modifierKey, "a", Keys.DELETE));
        yearInput.sendKeys("2025");

        semesterInput.sendKeys(Keys.chord(modifierKey, "a", Keys.DELETE));
        semesterInput.sendKeys("Spring");

        // Click "Show Sections" link
        WebElement showSections = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Show Sections")));
        wait.until(ExpectedConditions.elementToBeClickable(showSections)).click();
        Thread.sleep(SLEEP_DURATION);

        // Wait until table is loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("App")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tr")));

        // Click "View Assignments" link for first section in table
        WebElement viewAssignmentsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("/html/body/div/div/table/tbody/tr[1]/td[8]/a")));
        viewAssignmentsLink.click();
        Thread.sleep(SLEEP_DURATION);

        // Click "Add Assignment" button
        WebElement addAssignmentBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("/html/body/div/div/button")));
        addAssignmentBtn.click();
        Thread.sleep(SLEEP_DURATION);

        // Fill out title and due date fields in modal
        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='title']")));
        WebElement dueDateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='dueDate']")));
        titleInput.sendKeys(Keys.chord(modifierKey, "a", Keys.DELETE));
        titleInput.sendKeys("System Test Assignment");
        dueDateInput.sendKeys(Keys.chord(modifierKey, "a", Keys.DELETE));
        dueDateInput.sendKeys("2025-05-01");
        Thread.sleep(500);
        dueDateInput.sendKeys(Keys.TAB);

        // Click save button inside modal using JS to ensure success
        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("/html/body/div[2]/div[3]/div/div[2]/button[2]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
        Thread.sleep(500);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

        Thread.sleep(SLEEP_DURATION);

        // Assert assignment was added by checking page source
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("System Test Assignment"));

        // Find all table rows and locate the one with the new assignment
        List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//table/tbody/tr")));
        WebElement deleteBtn = null;

        // Search for row containing the test assignment and get its delete button
        for (WebElement row : rows) {
            if (row.getText().contains("System Test Assignment")) {
                List<WebElement> buttons = row.findElements(By.tagName("button"));
                deleteBtn = buttons.get(buttons.size() - 1);
                break;
            }
        }

        // Ensure delete button was found
        assertNotNull(deleteBtn, "Delete button for test assignment not found");
        wait.until(ExpectedConditions.elementToBeClickable(deleteBtn)).click();
        Thread.sleep(SLEEP_DURATION);

        // Confirm deletion in modal dialog
        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // Verify assignment is no longer on page
        pageSource = driver.getPageSource();
        assertFalse(pageSource.contains("System Test Assignment"));
    }
}
