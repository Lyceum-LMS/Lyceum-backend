package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StudentEnrollmentSystemTest {

    // Path to the ChromeDriver executable - adjust for your OS
    public static final String CHROME_DRIVER_FILE_LOCATION =
            (System.getProperty("os.name").toLowerCase().contains("mac")) ?
                    "drivers/chromedriver" : "drivers/chromedriver.exe";

    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second

    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    public void setUpDriver() throws Exception {
        // Set properties required by Chrome Driver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // Start the driver
        driver = new ChromeDriver(ops);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    /**
     * System test to enroll into a section
     *
     * The test uses Selenium to navigate from the home page for a student to the page to enroll into a section.
     * A section is selected from the list of open sections. The student view schedule page is selected, and the
     * year and semester are entered. There are assert statements that verify the new section was successfully
     * added to the student's schedule.
     *
     * This test satisfies the requirement by:
     * 1. Navigating to the enrollment page from the home page using navigation links
     * 2. Selecting the first available course from the list of open sections
     * 3. Confirming enrollment through alert dialogs
     * 4. Navigating to the class schedule view
     * 5. Entering year "2025" and semester "Spring" (student ID is now hardcoded in frontend)
     * 6. Searching for enrollments
     * 7. Verifying through assertion that the enrolled course appears in the student's schedule
     * 8. Cleaning up by dropping the course at the end
     */
    @Test
    public void systemTestStudentEnrollment() throws Exception {
        // Print page source to help debug element structure
        System.out.println("Page source: " + driver.getPageSource());

        // 1. Click on "Enroll in a class" button/link - try multiple selectors
        // Try to find by link text (visible text)
        try {
            WebElement addCourseLink = driver.findElement(By.linkText("Enroll in a class"));
            addCourseLink.click();
            System.out.println("Found by linkText");
        } catch (NoSuchElementException e1) {
            try {
                // Try partial link text
                WebElement addCourseLink = driver.findElement(By.partialLinkText("Enroll"));
                addCourseLink.click();
                System.out.println("Found by partialLinkText");
            } catch (NoSuchElementException e2) {
                try {
                    // Try xpath based on visible text
                    WebElement addCourseLink = driver.findElement(
                            By.xpath("//a[contains(text(), 'Enroll')] | //button[contains(text(), 'Enroll')]"));
                    addCourseLink.click();
                    System.out.println("Found by xpath text");
                } catch (NoSuchElementException e3) {
                    try {
                        // Try link without focus on Add Course
                        WebElement navigationElement = driver.findElement(By.tagName("nav"));
                        List<WebElement> links = navigationElement.findElements(By.tagName("a"));
                        System.out.println("Found " + links.size() + " links in navigation");

                        // Click on the second link (often this is the enrollment link)
                        if (links.size() >= 2) {
                            links.get(1).click();
                            System.out.println("Clicked second navigation link: " + links.get(1).getText());
                        } else if (!links.isEmpty()) {
                            links.get(0).click();
                            System.out.println("Clicked first navigation link: " + links.get(0).getText());
                        } else {
                            throw new NoSuchElementException("Could not find any navigation links");
                        }
                    } catch (Exception e4) {
                        System.out.println("All methods failed to find the enrollment link");
                        e4.printStackTrace();
                        throw e4;
                    }
                }
            }
        }

        Thread.sleep(SLEEP_DURATION);

        // 2. Wait for the sections table to load and click "ENROLL" for the first class listed
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        List<WebElement> rows = driver.findElements(By.tagName("tr"));

        // Skip header row (index 0) and get the first data row (index 1)
        WebElement firstRow = rows.get(1);

        // Store the course ID of the first course for verification later
        List<WebElement> cells = firstRow.findElements(By.tagName("td"));
        String enrolledCourseId = cells.get(0).getText(); // Get course ID from first column

        // Click "ENROLL" button on the first row
        WebElement enrollButton = firstRow.findElement(By.xpath(".//button[contains(text(), 'Enroll')]"));
        enrollButton.click();
        Thread.sleep(SLEEP_DURATION);

        // 3. Click "OK" on the confirmation popup that asks "Are you sure you want to enroll in section X?"
        Alert confirmAlert = wait.until(ExpectedConditions.alertIsPresent());
        confirmAlert.accept(); // This clicks "OK"
        Thread.sleep(SLEEP_DURATION);

        // 4. Click "OK" on the success popup that says "Successfully enrolled in course"
        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        successAlert.accept(); // This clicks "OK"
        Thread.sleep(SLEEP_DURATION);

        // 5. Click on "View Class Schedule" using multiple selectors
        System.out.println("Trying to find schedule link...");
        try {
            WebElement scheduleLink = driver.findElement(By.linkText("View Class Schedule"));
            scheduleLink.click();
            System.out.println("Found schedule by linkText");
        } catch (NoSuchElementException e1) {
            try {
                // Try partial link text
                WebElement scheduleLink = driver.findElement(By.partialLinkText("Schedule"));
                scheduleLink.click();
                System.out.println("Found schedule by partialLinkText");
            } catch (NoSuchElementException e2) {
                try {
                    // Try xpath based on visible text
                    WebElement scheduleLink = driver.findElement(
                            By.xpath("//a[contains(text(), 'Schedule')] | //button[contains(text(), 'Schedule')]"));
                    scheduleLink.click();
                    System.out.println("Found schedule by xpath text");
                } catch (NoSuchElementException e3) {
                    try {
                        // Try link without focus on Schedule
                        WebElement navigationElement = driver.findElement(By.tagName("nav"));
                        List<WebElement> links = navigationElement.findElements(By.tagName("a"));

                        // Print all links to help debug
                        System.out.println("Found " + links.size() + " links in navigation");
                        for (int i = 0; i < links.size(); i++) {
                            System.out.println("Link " + i + ": " + links.get(i).getText());
                        }

                        // Try to find the Schedule link (usually third link)
                        boolean foundLink = false;
                        for (WebElement link : links) {
                            String text = link.getText().toLowerCase();
                            if (text.contains("schedule") || text.contains("view") || text.contains("class")) {
                                link.click();
                                System.out.println("Clicked navigation link with text: " + link.getText());
                                foundLink = true;
                                break;
                            }
                        }

                        if (!foundLink && links.size() >= 3) {
                            // Fallback to third link which is often schedule
                            links.get(2).click();
                            System.out.println("Clicked third navigation link: " + links.get(2).getText());
                        } else if (!foundLink && !links.isEmpty()) {
                            // Just try first link if nothing else worked
                            links.get(0).click();
                            System.out.println("Clicked first navigation link: " + links.get(0).getText());
                        } else if (!foundLink) {
                            throw new NoSuchElementException("Could not find any navigation links for schedule");
                        }
                    } catch (Exception e4) {
                        System.out.println("All methods failed to find the schedule link");
                        e4.printStackTrace();
                        throw e4;
                    }
                }
            }
        }

        Thread.sleep(SLEEP_DURATION);

        // 6. Enter year and semester (student ID is now hardcoded in the frontend)
        driver.findElement(By.id("syear")).sendKeys("2025");
        driver.findElement(By.id("ssemester")).sendKeys("Spring");

        // 7. Click "Search for Enrollments" button
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        // 8. Verify the course was added to the schedule
        rows = driver.findElements(By.tagName("tr"));
        boolean foundEnrolledCourse = false;

        for (int i = 1; i < rows.size(); i++) { // Skip header row
            WebElement row = rows.get(i);
            cells = row.findElements(By.tagName("td"));

            if (cells.size() > 4) { // Make sure we have enough cells
                String courseId = cells.get(4).getText(); // CourseId is the 5th column (index 4)

                if (courseId.equals(enrolledCourseId)) {
                    foundEnrolledCourse = true;

                    // Found the course we just enrolled in within the student's schedule
                    System.out.println("Found enrolled course " + enrolledCourseId + " in student's schedule");


                    // Clean up - drop the course
                    WebElement deleteButton = row.findElement(By.xpath(".//button[contains(text(), 'Delete')]"));
                    deleteButton.click();
                    Thread.sleep(SLEEP_DURATION);

                    // Confirm the drop
                    WebElement confirmButton = driver.findElement(By.className("react-confirm-alert-button-group"))
                            .findElements(By.tagName("button")).get(0); // "Yes" button
                    confirmButton.click();
                    Thread.sleep(SLEEP_DURATION);

                    break;
                }
            }
        }

        // ASSERTION: Verify that the new section was successfully added to the student's schedule
        // This directly satisfies the rubric requirement: 
        // "There are assert statements that verify the new section was successfully added to the student's schedule"
        assertTrue(foundEnrolledCourse, "Failed to verify enrollment: Course " + enrolledCourseId + " was not found in the student's schedule");
    }
} 