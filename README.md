# Lyceum LMS Back-End

**Note:** This `secure` branch represents the default, monolithic architecture of the Lyceum LMS backend which was favored for downstream AWS deployment requirement. For the microservice-architected version, please refer to the `SOAgradebook` branch.

This repository contains the backend services for the Lyceum Learning Management System (LMS). Lyceum is a comprehensive platform for managing university-level academic operations. The system is engineered to provide distinct, secure functionalities for three primary user roles, as defined by the system requirements:

  * **Administrators:** Manage the complete lifecycle of Users (students, instructors), Courses, and academic Sections.
  * **Instructors:** Manage Assignments for their assigned sections, input assignment grades, and submit final course grades for enrolled students.
  * **Students:** Enroll in and drop courses, view their class schedules, monitor assignment due dates and scores, and access their academic transcripts.

This backend is implemented as a distributed system, consisting of two core microservices that communicate asynchronously via a message broker to ensure high cohesion and loose coupling.

-----

## System Architecture

The Lyceum backend employs a Service-Oriented Architecture (SOA) consisting of two primary microservices, a message queue, and a shared database. This design decouples the core responsibilities of the system: the "Registrar" (handling enrollments and user identity) is separated from the "Gradebook" (handling academic performance and assignments).

All communication from the React frontend is directed to the appropriate service based on the nature of the request. The services then synchronize essential data (like new enrollments or final grades) asynchronously using RabbitMQ, ensuring the system remains resilient and responsive.

### Architectural Evolution: From Monolith to Microservices

This project's architecture is the result of a deliberate, incremental refactoring process, evolving from a single application into a more robust, service-oriented system.

  * **Initial State (Monolith):** The project began as a traditional monolithic Spring Boot application. A single REST API served all functions, and the React frontend was built to consume this single, unified API.
  * **The Refactoring (SOA Split):** To improve maintainability, scalability, and separation of concerns, the monolith was refactored into an SOA. The original codebase was duplicated, and then responsibilities were selectively pruned from each new service to create two distinct, specialized applications: the Registrar Service and the Gradebook Service.
  * **The Result (Two Services):** The backend now consists of these two services running independently on different ports (`8080` and `8081`). They are decoupled by RabbitMQ, which separates the business logic of enrollment and user management from the logic of grading and academic activity.

-----

## Technology Stack

  * **Core:** Java 17, Spring Boot
  * **Database:** Spring Data JPA, MySQL 8.x
  * **Messaging:** Spring AMQP, RabbitMQ
  * **Security:** Spring Security (with JWT)
  * **Build:** Apache Maven
  * **Testing:** JUnit, Mockito, Spring Boot Test, Selenium WebDriver
  * **Containerization:** Docker
  * **Cloud:** AWS ECS / RDS / Load Balancer (example stack)

-----

## Core Services

The backend's responsibilities are divided into two distinct services:

### 1\. Registrar Service (Port: 8080)

This service manages the core "identity" and "catalog" data for the university. It is the source of truth for users, courses, and enrollment status.

  * **Core Responsibilities:**
      * **Admin:** Full CRUD on Users, Courses, and Sections.
      * **Student:** Enroll in courses, drop courses, view class schedule, and view transcripts (which includes final grades).
  * **Primary Controllers:** `CourseController`, `SectionController`, `UserController`, `StudentScheduleController`.

### 2\. Gradebook Service (Port: 8081)

This service manages all academic activities within a course, primarily focusing on assignments and grading.

  * **Core Responsibilities:**
      * **Instructor:** Full CRUD on Assignments, grade individual assignments, and enter final course grades.
      * **Student:** View assignments and their scores.
  * **Primary Controllers:** `AssignmentController`, `GradeController`, `EnrollmentController` (for final grade submission).

-----

## Asynchronous Communication (RabbitMQ)

The two services are decoupled using RabbitMQ for asynchronous data replication. This ensures that, for example, the Gradebook service is aware of students who enroll via the Registrar service, and the Registrar service is aware of final grades submitted via the Gradebook service.

  * **Implementation (The "Proxy" Pattern):** This communication is abstracted using dedicated proxy services within each application:

      * **`GradebookServiceProxy` (in Registrar):** When a User, Course, Section, or Enrollment is created, updated, or deleted in the Registrar service, this proxy formats a message and publishes it to a RabbitMQ queue.
      * **`RegistrarServiceProxy` (in Gradebook):** This service is a consumer that listens for messages from RabbitMQ. When it receives an `addCourse` message, for instance, it creates a replicated copy of that course in the Gradebook's database. This proxy also sends messages back to the Registrar (e.g., when an instructor submits a final grade).

  * **Message Format:** The system uses a simple, custom string-based message format:
    `"[actionName] [jsonPayload]"`

      * **Example (Sending):** `String msg = “addCourse “ + asJsonString(courseDTO); rabbitTemplate.convertAndSend(msg);`
      * **Example (Receiving):** `String[] parts = msg.split(" ", 2); String action = parts[0]; if (action.equals("addCourse")) { CourseDTO dto = fromJsonString(parts[1], CourseDTO.class); //... processing logic }`

  * **Data Ownership Model:**

      * **Registrar (Source of Truth):** User, Course, Section, Term, Enrollment (including the final grade).
      * **Gradebook (Source of Truth):** Assignment, Grade (assignment scores).
      * **Gradebook (Replicated Data):** User, Course, Section, Enrollment (These are replicated from the Registrar service via RabbitMQ to provide context for grading).

-----

## Database Schema

Both services connect to a single MySQL database named `courses`. While the database is shared, the services operate under a logical separation of data ownership as described above.

| Table | Primary Key | Purpose | Logical Owner |
| :--- | :--- | :--- | :--- |
| **user\_table** | `id` (int) | Stores all users (Admins, Instructors, Students). | Registrar |
| **term** | `term_id` (int) | Defines academic terms and key dates (add/drop). | Registrar |
| **course** | `course_id` (varchar) | Stores the official course catalog (e.g., CST 438). | Registrar |
| **section** | `section_no` (int) | Stores specific instances of a course for a given term. | Registrar |
| **enrollment** | `enrollment_id` (int) | Links a `user_table` (student) to a `section`. Stores the final grade. | Registrar (updated by Gradebook) |
| **assignment** | `assignment_id` (int) | Stores assignment details (title, due date) for a section. | Gradebook |
| **grade** | `grade_id` (int) | Links an `enrollment` to an `assignment`. Stores the numeric score. | Gradebook |

-----

## REST API (Summary)

Typical endpoints (paths may vary slightly by implementation):

### Administrative (Registrar Service)

  * `GET /courses`
  * `POST /courses`
  * `PUT /courses/{courseId}`
  * `DELETE /courses/{courseId}`
  * `GET /sections`
  * `POST /sections`
  * `PUT /sections/{sectionId}`
  * `DELETE /sections/{sectionId}`
  * `GET /users`
  * `POST /users`
  * `PUT /users/{userId}`
  * `DELETE /users/{userId}`

### Instructor-Oriented (Gradebook & Registrar Services)

  * `GET /sections/mine` – (Registrar) sections for authenticated instructor
  * `GET /sections/{sectionId}/enrollments` – (Registrar) enrollments in a section
  * `GET /assignments?sectionId={sectionId}` – (Gradebook)
  * `POST /assignments` – (Gradebook)
  * `PUT /assignments/{assignmentId}` – (Gradebook)
  * `DELETE /assignments/{assignmentId}` – (Gradebook)
  * `GET /assignments/{assignmentId}/grades` – (Gradebook)
  * `PUT /grades/{gradeId}` – (Gradebook) update grade
  * `PUT /enrollments/{enrollmentId}/final-grade` – (Gradebook) record final course grade

### Student-Oriented (Registrar & Gradebook Services)

  * `GET /sections/open` – (Registrar)
  * `POST /enrollments/section/{sectionId}` – (Registrar) enroll authenticated student
  * `DELETE /enrollments/{enrollmentId}` – (Registrar) drop enrollment (subject to rules)
  * `GET /enrollments?year={year}&semester={term}` – (Registrar) schedule for authenticated student
  * `GET /assignments?year={year}&semester={term}` – (Gradebook) assignments and scores
  * `GET /transcripts` – (Registrar) completed courses and final grades

### Authentication & Health

  * `POST /login` – (Registrar) accepts credentials, returns JWT on success
  * `GET /check` – (Both) health endpoint for monitoring

-----

## Security (JWT)

The entire backend is secured using Spring Security with JSON Web Tokens (JWT).

  * **Authentication:** The system provides a `/login` endpoint on the **Registrar service**. This endpoint accepts a user's email and password via **Basic Authentication**. Upon success, it returns a `LoginDTO` containing the JWT and the user's role (e.g., `ROLE_ADMIN`, `ROLE_INSTRUCTOR`, `ROLE_STUDENT`).
  * **Authorization:** Access to controller methods is restricted at the method level using Spring Security's `@PreAuthorize` annotations. This provides granular, role-based access control.
      * **Example:** `@PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")`
      * This annotation, using the `SCOPE_` prefix, is standard for JWT-based security and ensures only users with the "ADMIN" role can execute the protected method.
  * **Identity in Controllers:** The security model eliminated insecure `studentId` and `instructorEmail` request parameters. Instead, controller methods securely obtain the authenticated user's identity directly from the JWT by injecting the `java.security.Principal` object.
      * **Example:**
        ```java
        @GetMapping("/sections")
        public List<SectionDTO> getSectionsForInstructor(Principal principal) {
          String instructorEmail = principal.getName();
          //... logic to find sections for this instructor
        }
        ```

-----

## Setup & Local Run

To run the complete backend system, you must run **both** microservices and all supporting infrastructure.

### Prerequisites

  * **Java 17** (JDK)
  * **Apache Maven**
  * **MySQL 8.x**
  * **RabbitMQ**

### Database Setup

1.  Start your local MySQL server.
2.  Create the database: `CREATE DATABASE courses;`
3.  Connect to the database: `USE courses;`
4.  Run the schema script (located in the repository): `\. schema.sql`
5.  Populate with initial data: `\. data.sql`

### Start RabbitMQ

1.  Ensure your local RabbitMQ broker is running on the default port (5672).

### Configure Services

1.  Update the `application.properties` files in both the **Registrar** and **Gradebook** service modules.
2.  Ensure the following properties are set for your local environment:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/courses
    spring.datasource.username=<your_mysql_user>
    spring.datasource.password=<your_mysql_pass>

    spring.rabbitmq.host=localhost
    ```

### Run the Services

You must start **both services** simultaneously in separate terminals.

**Terminal 1 (Registrar Service):**

```bash
# From the root of the registrar service module
mvn spring-boot:run
# Service will start on its default port 8080
```
g
**Terminal 2 (Gradebook Service):**

```bash
# From the root of the gradebook service module
# Explicitly set the port as required by the architecture
mvn spring-boot:run -Dserver.port=8081
# Service will start on port 8081
```

-----

## Testing & Quality

### Unit & Integration Tests

  * Implemented with **JUnit** and **Spring Boot Test**:
      * Controller tests via `MockMvc`
      * Validation of enrollment rules, assignment/grade flows, and error handling.

### End-to-End Tests

  * Implemented with **Selenium WebDriver**:
      * Launch the UI
      * Perform real workflows (login, manage data, verify outcomes)
      * Assert both UI responses and backend state.

### API Functionality & Test Cases

The API's business logic and error handling were validated through a series of unit tests. The following scenarios represent key API functionalities:

| Feature | Scenario | API Call | Expected Outcome |
| :--- | :--- | :--- | :--- |
| Assignment | Instructor adds a valid assignment. | `POST /assignments` | 200 OK, assignment created. |
| Assignment | Add assignment w/ due date past term end. | `POST /assignments` | 400 Bad Request (business rule). |
| Assignment | Add assignment with invalid section number. | `POST /assignments` | 404 Not Found or 400 Bad Request. |
| Assignment | Grade an assignment. | `PUT /grades` | 200 OK, grades saved. |
| Assignment | Grade an invalid assignment ID. | `GET /assignments/{id}/grades` | 404 Not Found. |
| Enrollment | Student enrolls in an open section. | `POST /enrollments/sections/{id}` | 200 OK, enrollment created. |
| Enrollment | Student enrolls in a section they are already in. | `POST /enrollments/sections/{id}` | 400 Bad Request (duplicate). |
| Enrollment | Student enrolls in section with invalid ID. | `POST /enrollments/sections/{id}` | 404 Not Found. |
| Enrollment | Student enrolls past the add deadline. | `POST /enrollments/sections/{id}` | 400 Bad Request (business rule). |
| Grading | Instructor enters final grades. | `PUT /enrollments` | 200 OK, grades saved. |

-----

## Deployment (Container & Cloud)

1.  **Containerize Both Services:** Create Docker images for both the **Registrar Service** and the **Gradebook Service**.
2.  **AWS ECS:** Deploy two separate ECS Services (one for each container).
3.  **Load Balancing:** Configure an Application Load Balancer with listeners and target groups to route traffic to the correct service (e.g., routing to port `8080` or `8081` based on path or subdomain).
4.  **Database:** Both ECS services would connect to the shared **AWS RDS (MySQL)** instance.
5.  **Messaging:** An **Amazon MQ (for RabbitMQ)** instance would be provisioned and configured in the services' environment variables to handle the asynchronous communication.