package com.example.studentapi.student;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class StudentE2ETest {
    @LocalServerPort
    private int port;

    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.3.0");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    private String baseUrl = "http://localhost";

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    public static void init() {
        mysql.start();
    }
    @AfterAll
    static void afterAll() {
        mysql.stop();
    }

    @BeforeEach
    public void setUp(){
        baseUrl = baseUrl.concat(":").concat(port+"").concat("/api/students");
    }

    @BeforeEach
    public void clearDatabase() {
        studentRepository.deleteAll();
    }

    @Test
    void testGetAllStudents_ShouldReturnListOfStudentDto() {
        //Arrange
        Student student1 = new Student(null, "Tom", "Cruise", "tom.cruise@gmail.com");
        Student student2 = new Student(null, "Will", "Smith", "will.smith@gmail.com");
        studentRepository.saveAll(Arrays.asList(student1, student2));

        // Act
        ResponseEntity<StudentDto[]> response = restTemplate.getForEntity(baseUrl, StudentDto[].class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<StudentDto> students = Arrays.asList(response.getBody());
        assertEquals(2, students.size());
    }

    @Test
    void testGetStudentById_ExistingId_ShouldReturnStudentDto() {
        //Arrange
        Student student = new Student(null,
                "Tom",
                "Cruise",
                "tom.cruise@gmail.com");
        Student storedStudent = studentRepository.save(student);
        Long studentId = storedStudent.getId();
        String getUrl = baseUrl + "/" + studentId;
        System.out.println("getUrl:" + getUrl);

        // Act
        ResponseEntity<StudentDto> response = restTemplate.getForEntity("/api/students/{studentId}",
                StudentDto.class, studentId);

        System.out.println("Response body: " + response.getBody());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        StudentDto studentDto = response.getBody();
        assertNotNull(studentDto);
        assertEquals("Tom", studentDto.getStudentFirstName());
        assertEquals("Cruise", studentDto.getStudentLastName());
        assertEquals("tom.cruise@gmail.com", studentDto.getStudentEmail());
    }

    @Test
    void testGetStudentById_NonExistingId_ShouldReturnNotFound() {
        // Arrange
        Long nonExistingId = 999L;
        String getUrl = baseUrl + "/" + nonExistingId;

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(getUrl, String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Student not found with id: "+nonExistingId, response.getBody());
    }


    @Test
    void testCreateStudent_ValidInput_ShouldReturnCreatedStudentDto() {
        // Arrange
        StudentDto studentDto = new StudentDto(null,
                "Tom",
                "Cruise",
                "tom.cruise@gmail.com");

        try {
            // Act
            ResponseEntity<StudentDto> response = restTemplate.postForEntity(baseUrl, studentDto, StudentDto.class);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());

            StudentDto createdStudent = response.getBody();
            assertNotNull(createdStudent);
            assertEquals("Tom", createdStudent.getStudentFirstName());
            assertEquals("Cruise", createdStudent.getStudentLastName());
            assertEquals("tom.cruise@gmail.com", createdStudent.getStudentEmail());
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }

    }

    @Test
    void testCreateStudent_IncompleteInput_ShouldReturnBadRequestWithFieldErrors() throws JsonProcessingException {
        // Arrange
        StudentDto incompleteStudentDto = new StudentDto(null, null, null, null);

        // Act
        ResponseEntity<List> response = restTemplate.postForEntity(baseUrl, incompleteStudentDto, List.class);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        List<Map<String, String>> errorList = response.getBody();
        assertNotNull(errorList);

        // Assert that the error list is not empty
        assertFalse(errorList.isEmpty());

        // Assert that we get errors for three fields
        assertEquals(3, errorList.size());

        // Iterate over each map in the error list and assert the field and message are present
        for (Map<String, String> error : errorList) {
            assertTrue(error.containsKey("field"));
            assertTrue(error.containsKey("message"));
        }
    }

    @Test
    void testDeleteStudent_ExistingId_ShouldReturnSuccessMessage() {
        // Arrange
        Student student = new Student(null,
                "Tom",
                "Cruise",
                "tom.cruise@gmail.com");
        Student storedStudent = studentRepository.save(student);
        Long studentId = storedStudent.getId();
        String deleteUrl = baseUrl + "/" + studentId;

        // Act
        ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, null, String.class);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Student with studentId " + studentId + " is deleted", response.getBody());
    }

    @Test
    void testDeleteStudent_NonExistingId_ShouldReturnNotFound() {
        // Arrange
        Long nonExistingId = 999L;
        String deleteUrl = baseUrl + "/" + nonExistingId;

        // Act
        ResponseEntity<String> response = restTemplate.exchange(deleteUrl,
                HttpMethod.DELETE,
                null,
                String.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Student not found with id: "+nonExistingId, response.getBody());
    }

    @Test
    void testUpdateStudent_ExistingIdAndValidInput_ShouldReturnUpdatedStudentDto() {
        // Arrange
        Student student = new Student(null,
                "Tom",
                "Cruise",
                "tom.cruise@gmail.com");
        Student storedStudent = studentRepository.save(student);
        Long studentId = storedStudent.getId();
        String patchUrl = baseUrl + "/" + studentId;

        StudentDto updatedStudentDto = new StudentDto(null,
                "Tomkumar",
                "Cruise",
                "tom.cruise@gmail.com");
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
        assertEquals("tom.cruise@gmail.com", response.getBody().getStudentEmail());
    }

    @Test
    void testUpdateStudent_InvalidAllInput_ShouldReturnBadRequestWithFieldErrors() {
        // Arrange
        Student student = new Student(null,
                "Tom",
                "Cruise",
                "tom.cruise@gmail.com");
        Student storedStudent = studentRepository.save(student);
        Long studentId = storedStudent.getId();

        StudentDto invalidStudentDto = new StudentDto(null,
                "Tom123",
                "Cruise123",
                "tom.cruise@@gmail.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StudentDto> requestEntity = new HttpEntity<>(invalidStudentDto, headers);

        // Act
        ResponseEntity<List> response = restTemplate.exchange(baseUrl + "/{studentId}",
                HttpMethod.PATCH, requestEntity, List.class, studentId);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        List<Map<String, String>> errorList = response.getBody();
        assertNotNull(errorList);

        // Assert that the error list is not empty
        assertFalse(errorList.isEmpty());

        // Assert that we get errors for three fields
        assertEquals(3, errorList.size());

        // Iterate over each map in the error list and assert the field and message are present
        for (Map<String, String> error : errorList) {
            assertTrue(error.containsKey("field"));
            assertTrue(error.containsKey("message"));
        }
    }
}

