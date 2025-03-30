package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

public class EnrollmentControllerStudentEnrollSectionSystemTest {

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

        // return to home page
        we_home.click();
        Thread.sleep(SLEEP_DURATION);

        // confirm course delete via mesage
        // String message = driver.findElement(By.id("message")).getText();
//        WebElement we_message = driver.findElement(By.xpath("//h4[text()='Enrollment deleted']"));
////      assertTrue(message.startsWith("section added"));
//        assertNotNull(we_message);
//        Thread.sleep(1500);






//        // enter 2025, Spring and click show sections
//        driver.findElement(By.id("year")).sendKeys("2025");
//        driver.findElement(By.id("semester")).sendKeys("Spring");
//        driver.findElement(By.id("search")).click();
//        Thread.sleep(SLEEP_DURATION);
//
//        // select View Enrollments for secNo "8"
//        WebElement enrollmentsLink = driver.findElement(
//                By.xpath("//tr[@data-secno='8']//a[text()='View Enrollments']")
//        );
//        enrollmentsLink.click();
//        Thread.sleep(SLEEP_DURATION);
//
//        // set up OS specific modifier key
//        String os = System.getProperty("os.name").toLowerCase();
//        Keys modifierKey = (os.contains("mac")) ? Keys.COMMAND : Keys.CONTROL;
//        // create array of students
//        String[] emails = {"tedison@csumb.edu", "lsimpson@csumb.edu", "bsimpson@csumb.edu", "hsimpson@csumb.edu"};
//        String[] grades = {"A", "A", "", "B"};
//
//        int index = 0;
//
//        while (index < emails.length) {
//            // if student email does not exist, increment index and loop
//            try {
//                // get student email and grade from list
//                String email = emails[index];
//                String grade = grades[index];
//                // find student email, update grade
//                WebElement row = driver.findElement(By.xpath("//tr[td[text()='" + email + "']]"));
//                WebElement inputGrade = row.findElement(By.cssSelector("input[name='grade']"));
//                // clear inputGrade
//                inputGrade.click();  // focus the field
//                inputGrade.clear();  // attempt native clear
//                inputGrade.sendKeys(Keys.chord(modifierKey, "a")); // select all
//                inputGrade.sendKeys(Keys.DELETE); // then delete
//                Thread.sleep(SLEEP_DURATION);
//                // send grade with whitespace otherwise react repopulates field with previous value
//                inputGrade.sendKeys(grade + " ");
//                inputGrade.sendKeys(Keys.BACK_SPACE);
//            } catch (NoSuchElementException e) {
//                String email = emails[index];
//                System.err.println("Skipping index/email: " + index + "/" + email + " — missing element.");
//            } finally {
//                index++;
//                Thread.sleep(SLEEP_DURATION);
//            }
//        } // while()
//
//        try {
//            driver.findElement(By.id("btn-save-grades")).click();
//            Thread.sleep(SLEEP_DURATION);
//        } catch (NoSuchElementException e) {
//            System.err.println("missing expected save element.");
//        } finally {
//        }
//
//        // assert grades have been updated
//        index = 0;
//        while (index < emails.length) {
//            try {
//                String email = emails[index];
//                String expectedGrade = grades[index];
//
//                WebElement row = driver.findElement(By.xpath("//tr[td[text()='" + email + "']]"));
//                WebElement inputGrade = row.findElement(By.cssSelector("input[name='grade']"));
//                String actualGrade = inputGrade.getAttribute("value");
//                assertEquals(expectedGrade, actualGrade, "Grade mismatch for " + email);
//
//            } catch (NoSuchElementException e) {
//                String email = emails[index];
//                System.err.println("Could not verify grade for row/email: " + index + "/" + email + " — missing element.");
//            } finally {
//                index++;
//                Thread.sleep(SLEEP_DURATION);
//            }
//        } // while()
//        // return to home page
//        we.click();
//        Thread.sleep(SLEEP_DURATION);
    } // systemTestEnrollClass()
} // InstructorControllerClassGradesSystemTest


//
//        // verify that cst499 is not in the list of sections
//        // if it exists, then delete it
//        // Selenium throws NoSuchElementException when the element is not found
//        try {
//            while (true) {
//                WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
//                List<WebElement> buttons = row499.findElements(By.tagName("button"));
//                // delete is the second button
//                assertEquals(2, buttons.size());
//                buttons.get(1).click();
//                Thread.sleep(SLEEP_DURATION);
//                // find the YES to confirm button
//                List<WebElement> confirmButtons = driver
//                        .findElement(By.className("react-confirm-alert-button-group"))
//                        .findElements(By.tagName("button"));
//                assertEquals(2, confirmButtons.size());
//                confirmButtons.get(0).click();
//                Thread.sleep(SLEEP_DURATION);
//            }
//        } catch (NoSuchElementException e) {
//           // do nothing, continue with test
//        }
//
//        // find and click button to add a section
//        driver.findElement(By.id("addSection")).click();
//        Thread.sleep(SLEEP_DURATION);
//
//        // enter data
//        //  courseId: cst499,
//        driver.findElement(By.id("ecourseId")).sendKeys("cst499");
//        //  secId: 1,
//        driver.findElement(By.id("esecId")).sendKeys("1");
//        //  year:2024,
//        driver.findElement(By.id("eyear")).sendKeys("2024");
//        //  semester:Spring,
//        driver.findElement(By.id("esemester")).sendKeys("Spring");
//        //  building:052,
//        driver.findElement(By.id("ebuilding")).sendKeys("052");
//        //  room:104,
//        driver.findElement(By.id("eroom")).sendKeys("104");
//        //  times:W F 1:00-2:50 pm,
//        driver.findElement(By.id("etimes")).sendKeys("W F 1:00-2:50 pm");
//        //  instructorEmail jgross@csumb.edu
//        driver.findElement(By.id("einstructorEmail")).sendKeys("jgross@csumb.edu");
//        // click Save
//        driver.findElement(By.id("save")).click();
//        Thread.sleep(SLEEP_DURATION);
//
//        String message = driver.findElement(By.id("addMessage")).getText();
//        assertTrue(message.startsWith("section added"));
//
//        // close the dialog
//        driver.findElement(By.id("close")).click();
//
//        // verify that new Section shows up on Sections list
//        // find the row for cst499
//        WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
//        List<WebElement> buttons = row499.findElements(By.tagName("button"));
//        // delete is the second button
//        assertEquals(2, buttons.size());
//        buttons.get(1).click();
//        Thread.sleep(SLEEP_DURATION);
//        // find the YES to confirm button
//        List<WebElement> confirmButtons = driver
//                .findElement(By.className("react-confirm-alert-button-group"))
//                .findElements(By.tagName("button"));
//        assertEquals(2, confirmButtons.size());
//        confirmButtons.get(0).click();
//        Thread.sleep(SLEEP_DURATION);
//
//        // verify that Section list is now empty
//        assertThrows(NoSuchElementException.class, () ->
//                driver.findElement(By.xpath("//tr[td='cst499']")));

//    } // systemTestAddGrades()
//} // InstructorControllerClassGradesSystemTest

//   @Test
//    public void systemTestAddSectionBadCourse() throws Exception {
//        // attempt to add a section to course cst599 2024, Spring
//        // fails because course does not exist
//        // change courseId to cst499 and try again
//        // verify success
//        // delete the section
//
//       // click link to navigate to Sections
//       WebElement we = driver.findElement(By.id("sections"));
//       we.click();
//       Thread.sleep(SLEEP_DURATION);
//
//       // enter cst, 2024, Spring and click search sections
//       driver.findElement(By.id("scourseId")).sendKeys("cst");
//       driver.findElement(By.id("syear")).sendKeys("2024");
//       driver.findElement(By.id("ssemester")).sendKeys("Spring");
//       driver.findElement(By.id("search")).click();
//       Thread.sleep(SLEEP_DURATION);
//
//       // verify that cst499 is not in the list of sections
//       // Selenium throws NoSuchElementException when the element is not found
//       try {
//           while (true) {
//               WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
//               List<WebElement> buttons = row499.findElements(By.tagName("button"));
//               // delete is the second button
//               assertEquals(2, buttons.size());
//               buttons.get(1).click();
//               Thread.sleep(SLEEP_DURATION);
//               // find the YES to confirm button
//               List<WebElement> confirmButtons = driver
//                       .findElement(By.className("react-confirm-alert-button-group"))
//                       .findElements(By.tagName("button"));
//               assertEquals(2, confirmButtons.size());
//               confirmButtons.get(0).click();
//               Thread.sleep(SLEEP_DURATION);
//           }
//       } catch (NoSuchElementException e) {
//           // do nothing, continue with test
//       }
//
//       // find and click button to add a section
//       driver.findElement(By.id("addSection")).click();
//       Thread.sleep(SLEEP_DURATION);
//
//       // enter data
//       //  courseId: cst599
//       driver.findElement(By.id("ecourseId")).sendKeys("cst599");
//       //  secId: 1,
//       driver.findElement(By.id("esecId")).sendKeys("1");
//       //  year:2024,
//       driver.findElement(By.id("eyear")).sendKeys("2024");
//       //  semester:Spring,
//       driver.findElement(By.id("esemester")).sendKeys("Spring");
//       //  building:052,
//       driver.findElement(By.id("ebuilding")).sendKeys("052");
//       //  room:104,
//       driver.findElement(By.id("eroom")).sendKeys("104");
//       //  times:W F 1:00-2:50 pm,
//       driver.findElement(By.id("etimes")).sendKeys("W F 1:00-2:50 pm");
//       //  instructorEmail jgross@csumb.edu
//       driver.findElement(By.id("einstructorEmail")).sendKeys("jgross@csumb.edu");
//       // click Save
//       driver.findElement(By.id("save")).click();
//       Thread.sleep(SLEEP_DURATION);
//
//        WebElement msg = driver.findElement(By.id("addMessage"));
//        String message = msg.getText();
//        assertEquals("course not found cst599", message);
//
//        // clear the courseId field and enter cst499
//        WebElement courseId = driver.findElement(By.id("ecourseId"));
//        String os = System.getProperty("os.name").toLowerCase();
//        Keys modifierKey = (os.contains("mac")) ? Keys.COMMAND : Keys.CONTROL;
//        courseId.sendKeys(Keys.chord(modifierKey, "a", Keys.DELETE));
////        courseId.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
//
//       Thread.sleep(SLEEP_DURATION);
//        courseId.sendKeys("cst499");
//        driver.findElement(By.id("save")).click();
//        Thread.sleep(SLEEP_DURATION);
//
//       message = driver.findElement(By.id("addMessage")).getText();
//       assertTrue(message.startsWith("section added"));
//
//       // close the dialog
//       driver.findElement(By.id("close")).click();
//       Thread.sleep(SLEEP_DURATION);
//
//        WebElement row = driver.findElement(By.xpath("//tr[td='cst499']"));
//        assertNotNull(row);
//       // find the delete button on the row from prior statement.
//       List<WebElement> deleteButtons = row.findElements(By.tagName("button"));
//       // delete is the second button
//       assertEquals(2, deleteButtons.size());
//       deleteButtons.get(1).click();
//       Thread.sleep(SLEEP_DURATION);
//       // find the YES to confirm button
//       List<WebElement> confirmButtons = driver
//               .findElement(By.className("react-confirm-alert-button-group"))
//               .findElements(By.tagName("button"));
//       assertEquals(2,confirmButtons.size());
//       confirmButtons.get(0).click();
//       Thread.sleep(SLEEP_DURATION);
//
//       // verify that Section list is empty
//       assertThrows(NoSuchElementException.class, () ->
//               driver.findElement(By.xpath("//tr[td='cst499']")));
//    }
//} // InstructorControllerClassGradesSystemTest

