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

    public static final String CHROME_DRIVER_FILE_LOCATION =
            (System.getProperty("os.name").toLowerCase().contains("mac")) ?
                    "drivers/chromedriver" : "drivers/chromedriver.exe";

    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000;

    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    public void setup() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testInstructorAddsNewAssignmentSuccessfully() throws Exception {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("year")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("semester")));

        WebElement yearInput = driver.findElement(By.id("year"));
        WebElement semesterInput = driver.findElement(By.id("semester"));

        String os = System.getProperty("os.name").toLowerCase();
        Keys modifierKey = (os.contains("mac")) ? Keys.COMMAND : Keys.CONTROL;

        yearInput.sendKeys(Keys.chord(modifierKey, "a", Keys.DELETE));
        yearInput.sendKeys("2025");

        semesterInput.sendKeys(Keys.chord(modifierKey, "a", Keys.DELETE));
        semesterInput.sendKeys("Spring");

        WebElement showSections = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Show Sections")));
        wait.until(ExpectedConditions.elementToBeClickable(showSections)).click();
        Thread.sleep(SLEEP_DURATION);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("App")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tr")));

        WebElement viewAssignmentsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("/html/body/div/div/table/tbody/tr[1]/td[8]/a")));
        viewAssignmentsLink.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement addAssignmentBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("/html/body/div/div/button")));
        addAssignmentBtn.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='title']")));
        WebElement dueDateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='dueDate']")));
        titleInput.clear();
        titleInput.sendKeys("System Test Assignment");
        dueDateInput.clear();
        dueDateInput.sendKeys("2025-05-01");
        Thread.sleep(500);
        dueDateInput.sendKeys(Keys.TAB);

        WebElement saveButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("/html/body/div[2]/div[3]/div/div[2]/button[2]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", saveButton);
        Thread.sleep(500);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

        Thread.sleep(SLEEP_DURATION);

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("System Test Assignment"));

        List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//table/tbody/tr")));
        WebElement deleteBtn = null;

        for (WebElement row : rows) {
            if (row.getText().contains("System Test Assignment")) {
                List<WebElement> buttons = row.findElements(By.tagName("button"));
                deleteBtn = buttons.get(buttons.size() - 1);
                break;
            }
        }

        assertNotNull(deleteBtn, "Delete button for test assignment not found");
        wait.until(ExpectedConditions.elementToBeClickable(deleteBtn)).click();
        Thread.sleep(SLEEP_DURATION);

        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        pageSource = driver.getPageSource();
        assertFalse(pageSource.contains("System Test Assignment"));
    }
}
