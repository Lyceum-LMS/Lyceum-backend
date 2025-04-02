package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentControllerSystemTest {

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

    // what assumptions, if any, are made by these tests?

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
    public void systemTestAddGrades() throws Exception {
        /*
        System test to enter enrollment grades for enrolled students
        The test uses Selenium to navigate from the home page for an instructor,
        to view a list of Sections, enter year and semester and view the list of sections,
        then select the link to view enrollments,
        and the grade field for each enrolled student is updated with a final letter grade value.
        Then the grades are saved.
        There are assert statements to verify that the grades were saved.
        */

        // click link to navigate to home
        WebElement we = driver.findElement(By.xpath("//a[text()='Home']"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // enter 2025, Spring and click show sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.xpath("//a[text()='Show Sections']")).click();
        Thread.sleep(SLEEP_DURATION);

        // select View Enrollments for secNo "8"
        WebElement enrollmentsLink = driver.findElement(
                By.xpath("//tr[td[text()='cst363'] and td[text()='1']]//a[text()='View Enrollments']")
        );
        enrollmentsLink.click();
        Thread.sleep(SLEEP_DURATION);

        // set up OS specific modifier key
        String os = System.getProperty("os.name").toLowerCase();
        Keys modifierKey = (os.contains("mac")) ? Keys.COMMAND : Keys.CONTROL;
        // create array of students and their new grades
        String[] emails = {"tedison@csumb.edu", "lsimpson@csumb.edu", "bsimpson@csumb.edu", "hsimpson@csumb.edu"};
        String[] gradesNew = {"", "A", "A", "B"};
        String[] gradesOrg = {"B", "A", "B", ""};

        // update grades
        int index = 0;
        while (index < emails.length) {
            // if student email does not exist, increment index and loop
            try {
                // get student email and grade from list
                String email = emails[index];
                String grade = gradesNew[index];
                // find student email, update grade
                WebElement row = driver.findElement(By.xpath("//tr[td[text()='" + email + "']]"));
                WebElement inputGrade = row.findElement(By.cssSelector("input[name='grade']"));
                // clear inputGrade
                inputGrade.click();  // focus the field
                inputGrade.clear();  // attempt native clear
                inputGrade.sendKeys(Keys.chord(modifierKey, "a")); // select all
                inputGrade.sendKeys(Keys.DELETE); // then delete
                Thread.sleep(SLEEP_DURATION);
                // send grade with whitespace otherwise react repopulates field with previous value
                inputGrade.sendKeys(grade + " ");
                inputGrade.sendKeys(Keys.BACK_SPACE);
            } catch (NoSuchElementException e) {
                String email = emails[index];
                System.err.println("Skipping index/email: " + index + "/" + email + " — missing element.");
            } finally {
                index++;
                Thread.sleep(SLEEP_DURATION);
            }
        } // while()

        // save grades
        try {
            driver.findElement(By.xpath("//button[text()='Save Grades']")).click();
            Thread.sleep(SLEEP_DURATION);
        } catch (NoSuchElementException e) {
            System.err.println("missing expected save element.");
        } finally {
        }

        // go to home page and then come back in
        we.click();
        Thread.sleep(SLEEP_DURATION);
        // enter 2025, Spring and click show sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.xpath("//a[text()='Show Sections']")).click();
        Thread.sleep(SLEEP_DURATION);

        enrollmentsLink = driver.findElement(
//                By.xpath("//tr[@data-secno='8']//a[text()='View Enrollments']")
                By.xpath("//tr[td[text()='cst363'] and td[text()='1']]//a[text()='View Enrollments']")
        );
        enrollmentsLink.click();
        Thread.sleep(SLEEP_DURATION);


        // assert grades have been updated
        index = 0;
        while (index < emails.length) {
            try {
                String email = emails[index];
                String expectedGrade = gradesNew[index];

                WebElement row = driver.findElement(By.xpath("//tr[td[text()='" + email + "']]"));
                WebElement inputGrade = row.findElement(By.cssSelector("input[name='grade']"));
                String actualGrade = inputGrade.getAttribute("value");
                assertEquals(expectedGrade, actualGrade, "Grade mismatch for " + email);

            } catch (NoSuchElementException e) {
                String email = emails[index];
                System.err.println("Could not verify grade for row/email: " + index + "/" + email + " — missing element.");
            } finally {
                index++;
                Thread.sleep(SLEEP_DURATION);
            }
        } // while()

        // restore grades
        index = 0;
        while (index < emails.length) {
            // if student email does not exist, increment index and loop
            try {
                // get student email and grade from list
                String email = emails[index];
                String grade = gradesOrg[index];
                // find student email, update grade
                WebElement row = driver.findElement(By.xpath("//tr[td[text()='" + email + "']]"));
                WebElement inputGrade = row.findElement(By.cssSelector("input[name='grade']"));
                // clear inputGrade
                inputGrade.click();  // focus the field
                inputGrade.clear();  // attempt native clear
                inputGrade.sendKeys(Keys.chord(modifierKey, "a")); // select all
                inputGrade.sendKeys(Keys.DELETE); // then delete
                Thread.sleep(SLEEP_DURATION);
                // send grade with whitespace otherwise react repopulates field with previous value
                inputGrade.sendKeys(grade + " ");
                inputGrade.sendKeys(Keys.BACK_SPACE);
            } catch (NoSuchElementException e) {
                String email = emails[index];
                System.err.println("Skipping index/email: " + index + "/" + email + " — missing element.");
            } finally {
                index++;
                Thread.sleep(SLEEP_DURATION);
            }
        } // while()

        // save grades
        try {
            driver.findElement(By.xpath("//button[text()='Save Grades']")).click();
            Thread.sleep(SLEEP_DURATION);
        } catch (NoSuchElementException e) {
            System.err.println("missing expected save element.");
        } finally {
        }

        // return to home page
        we.click();
        Thread.sleep(SLEEP_DURATION);
    } // systemTestAddGrades()
} // InstructorControllerClassSystemTest

