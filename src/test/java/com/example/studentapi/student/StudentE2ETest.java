package com.example.studentapi.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentE2ETest {
    @LocalServerPort
    private int port;
    private String baseUrl = "http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
        //Needed for PATCH request, not allowed by default HttpClient of JDK
        CloseableHttpClient client = HttpClients.createDefault();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
    }

    @BeforeEach
    public void setUp(){
        baseUrl = baseUrl.concat(":").concat(port+"").concat("/api/students");
    }

    @Test
    // Arrange
    @Sql(statements = {
            "INSERT INTO student (FIRST_NAME, LAST_NAME, EMAIL) VALUES ('Tom', 'Cruise', 'tom.cruise@example.com')",
            "INSERT INTO student (FIRST_NAME, LAST_NAME, EMAIL) VALUES ('Will', 'Smith', 'will.smith@example.com')"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "DELETE FROM student WHERE FIRST_NAME = 'Tom'",
            "DELETE FROM student WHERE FIRST_NAME = 'Will'"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGetAllStudents_ShouldReturnListOfStudentDto() {
        // Act
        ResponseEntity<StudentDto[]> response = restTemplate.getForEntity(baseUrl, StudentDto[].class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<StudentDto> students = Arrays.asList(response.getBody());
        assertEquals(2, students.size());
    }

    @Test
    //Arrange
    @Sql(statements = "INSERT INTO student (id, FIRST_NAME, LAST_NAME, EMAIL) VALUES (1000, 'Tom', 'Cruise', 'tom.cruise@example.com')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM student WHERE FIRST_NAME = 'Tom'",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGetStudentById_ExistingId_ShouldReturnStudentDto() {
        Long studentId = 1000L;
        String getUrl = baseUrl + "/" + studentId;

        // Act
        ResponseEntity<StudentDto> response = restTemplate.getForEntity(getUrl, StudentDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StudentDto student = response.getBody();
        assertNotNull(student);
        assertEquals("Tom", student.getStudentFirstName());
        assertEquals("Cruise", student.getStudentLastName());
        assertEquals("tom.cruise@example.com", student.getStudentEmail());
    }

    @Test
    // Arrange
    @Sql(statements = {
            "INSERT INTO student (FIRST_NAME, LAST_NAME, EMAIL) VALUES ('Tom', 'Cruise', 'tom.cruise@example.com')"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = {
            "DELETE FROM student WHERE FIRST_NAME = 'Tom'"
    }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testCreateStudent_ValidInput_ShouldReturnCreatedStudentDto() {
        StudentDto studentDto = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");

        // Act
        ResponseEntity<StudentDto> response = restTemplate.postForEntity(baseUrl, studentDto, StudentDto.class);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        StudentDto createdStudent = response.getBody();
        assertNotNull(createdStudent);
        assertEquals("Tom", createdStudent.getStudentFirstName());
        assertEquals("Cruise", createdStudent.getStudentLastName());
        assertEquals("tom.cruise@example.com", createdStudent.getStudentEmail());
    }

    @Test
    // Arrange
    @Sql(statements = {
            "INSERT INTO student (id, FIRST_NAME, LAST_NAME, EMAIL) VALUES (2000,'Tom', 'Cruise', 'tom.cruise@example.com')"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDeleteStudent_ExistingId_ShouldReturnSuccessMessage() {
        Long studentId = 2000L;
        String deleteUrl = baseUrl + "/" + studentId;

        // Act
        ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        String message = response.getBody();
        assertEquals("Student with studentId 2000 is deleted", message);
    }

    @Test
    // Arrange
    @Sql(statements = "INSERT INTO student (id, FIRST_NAME, LAST_NAME, EMAIL) VALUES (5000, 'John', 'Doe', 'john.doe@example.com')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM student WHERE id = 5000",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testUpdateStudent_ExistingIdAndValidInput_ShouldReturnUpdatedStudentDto() {
        // Arrange
        Long studentId = 5000L;
        String patchUrl = baseUrl + "/" + studentId;

        StudentDto updatedStudentDto = new StudentDto(null, "Tomkumar", "Cruise", "tom.cruise@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDto> requestEntity = new HttpEntity<>(updatedStudentDto, headers);

        // Act
        ResponseEntity<StudentDto> response = restTemplate.exchange(patchUrl, HttpMethod.PATCH, requestEntity, StudentDto.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tomkumar", response.getBody().getStudentFirstName());
        assertEquals("Cruise", response.getBody().getStudentLastName());
    }
}

