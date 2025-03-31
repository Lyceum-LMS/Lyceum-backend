package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

public class StudentScheduleControllerSystemTest_SLS {

    // TODO edit the following to give the location and file name
    // of the Chrome driver.
    //  for WinOS the file name will be chromedriver.exe
    //  for MacOS the file name will be chromedriver
    public static final String CHROME_DRIVER_FILE_LOCATION =
            (System.getProperty("os.name").toLowerCase().contains("mac")) ?
                    "drivers/chromedriver" : "drivers/chromedriver.exe";

    //public static final String CHROME_DRIVER_FILE_LOCATION =
    //        "~/chromedriver_macOS/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assumes that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
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
    public void systemTestEnrollClass() throws Exception {
        /*
            System test to enroll into a section
            The test uses Selenium to navigate from the home page for an student
            to the page to enroll into a section.
            A section is selected from the list of open sections.
            The student view schedule page is selected, and the year and semester are entered.
            There are assert statements that verify the new section was successfully
            added to the student's schedule.
        */


        // click link to navigate to home
//        WebElement we = driver.findElement(By.id("root"));
        WebElement we_home = driver.findElement(By.xpath("//a[text()='Home']"));
        we_home.click();
        Thread.sleep(SLEEP_DURATION);

        // click link to navigate to "Enroll in a class"
        WebElement we_enroll = driver.findElement(By.xpath("//a[text()='Enroll in a class']"));
        we_enroll.click();
        Thread.sleep(2500);

        // enroll in cst338 section 1 (secNo=6)
//        WebElement row = driver.findElement(By.cssSelector("tr[data-secno='6']"));
        WebElement row = driver.findElement(By.xpath("//tr[td[text()='cst338'] and td[text()='1']]"));
        WebElement enrollButton = row.findElement(By.xpath(".//button[text()='Enroll']"));
        enrollButton.click();
        Thread.sleep(2500);

        // Wait for prompt to enroll in course and select 'OK']
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        assertTrue(alertText.contains("Are you sure you want to enroll in section 6?"), "Unexpected alert message");
        alert.accept();
        Thread.sleep(1500);

        // Wait for prompt that course was enrolled and select 'OK'
        alert = driver.switchTo().alert();
        alertText = alert.getText();
        assertTrue(alertText.contains("Successfully enrolled in course"), "Unexpected alert message");
        alert.accept();
        Thread.sleep(1500);

        // go to View Class Schedule, verify course is added and then delete
        WebElement we_schedule = driver.findElement(By.xpath("//a[text()='VIew Class Schedule']"));
        we_schedule.click();
        Thread.sleep(SLEEP_DURATION);
        driver.findElement(By.id("syear")).sendKeys("2025");
        driver.findElement(By.id("ssemester")).sendKeys("Spring");
        driver.findElement(By.xpath("//button[text()='Search for Enrollments']")).click();
        Thread.sleep(SLEEP_DURATION);

        // assert course has been added
        WebElement we_row = driver.findElement(By.xpath("//tr[td[text()='cst338']]"));
        assertNotNull(we_row, "course not there");
        Thread.sleep(SLEEP_DURATION);

        // delete added course
        WebElement deleteButton = we_row.findElement(By.xpath(".//button[text()='Delete']"));
        deleteButton.click();
        Thread.sleep(1500);
        // Wait for prompt to delete course and select 'Yes']
        WebElement yesButton = driver.findElement(By.xpath("//div[contains(@class,'react-confirm-alert')]//button[text()='Yes']"));
        yesButton.click();
        Thread.sleep(1500);

        // confirm course delete via mesage
        // String message = driver.findElement(By.id("message")).getText();
        WebElement we_message = driver.findElement(By.xpath("//h4[text()='Enrollment deleted']"));
        assertTrue(we_message.getText().contains("Enrollment deleted"));
//        assertNotNull(we_message);
        Thread.sleep(1500);

        // return to home page
        we_home.click();
        Thread.sleep(SLEEP_DURATION);


    } // systemTestEnrollClass()
} // EnrollmentControllerStudentEnrollSectionSystemTest




