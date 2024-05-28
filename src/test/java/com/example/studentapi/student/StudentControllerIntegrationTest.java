package com.example.studentapi.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class StudentControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl = "http://localhost";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    //Arrange
    @Sql(statements = "INSERT INTO student (FIRST_NAME, LAST_NAME, EMAIL) VALUES ('Tom', 'Cruise', 'tom.cruise@example.com')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "INSERT INTO student (FIRST_NAME, LAST_NAME, EMAIL) VALUES ('Will', 'Smith', 'will.smith@example.com')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM student WHERE FIRST_NAME = 'Tom'",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(statements = "DELETE FROM student WHERE FIRST_NAME = 'Will'",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGetAllStudents_ShouldReturnListOfStudentDto() throws Exception {
        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/students"));

        //Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                // Assert the content of the response
                .andExpect(jsonPath("$", Matchers.hasSize(2))) // Expecting two items in the list
                .andExpect(jsonPath("$[0].studentFirstName", Matchers.is("Tom")))
                .andExpect(jsonPath("$[0].studentLastName", Matchers.is("Cruise")))
                .andExpect(jsonPath("$[0].studentEmail", Matchers.is("tom.cruise@example.com")))
                .andExpect(jsonPath("$[1].studentFirstName", Matchers.is("Will")))
                .andExpect(jsonPath("$[1].studentLastName", Matchers.is("Smith")))
                .andExpect(jsonPath("$[1].studentEmail", Matchers.is("will.smith@example.com")));
    }

    @Test
    //Arrange
    @Sql(statements = "INSERT INTO student (FIRST_NAME, LAST_NAME, EMAIL) VALUES ('Tom', 'Cruise', 'tom.cruise@example.com')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM student WHERE FIRST_NAME = 'Tom'",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGetStudentById_ExistingId_ShouldReturnStudentDto() throws Exception {
        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get("/api/students/{studentId}", 1L));

        //Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.studentFirstName").value("Tom"))
                .andExpect(jsonPath("$.studentLastName").value("Cruise"))
                .andExpect(jsonPath("$.studentEmail").value("tom.cruise@example.com"));
    }

    @Test
    void testCreateStudent_ValidInput_ShouldReturnCreatedStudentDto() throws Exception {
        //Arrange
        StudentDto studentDto = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto createdStudentDto = new StudentDto(1L, "Tom", "Cruise", "tom.cruise@example.com");

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)));
        //Assert
        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.id").value(createdStudentDto.getId()))
                .andExpect(jsonPath("$.studentFirstName").value(createdStudentDto.getStudentFirstName()))
                .andExpect(jsonPath("$.studentLastName").value(createdStudentDto.getStudentLastName()))
                .andExpect(jsonPath("$.studentEmail").value(createdStudentDto.getStudentEmail()));
    }

    @Test
    //Arrange
    @Sql(statements = "INSERT INTO student (id, FIRST_NAME, LAST_NAME, EMAIL) VALUES (1000,'Tom', 'Cruise', 'tom.cruise@example.com')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDeleteStudent_ExistingId_ShouldReturnSuccessMessage() throws Exception {
        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/api/students/{studentId}", 1000L));
        //Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Student with studentId 1000 is deleted"));
    }

    @Test
    //Arrange
    @Sql(statements = "INSERT INTO student (id, FIRST_NAME, LAST_NAME, EMAIL) VALUES (2000, 'Tom', 'Cruise', 'tom.cruise@example.com')",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM student WHERE FIRST_NAME = 'Tomkumar'",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testUpdateStudent_ExistingIdAndValidInput_ShouldReturnUpdatedStudentDto() throws Exception {
        //Arrange
        StudentDto studentDto = new StudentDto(2000L,"Tomkumar", "Cruise", "tom.cruise@example.com");
        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.patch("/api/students/{studentId}", 2000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)));
        //Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.studentFirstName").value("Tomkumar"))
                .andExpect(jsonPath("$.studentLastName").value("Cruise"))
                .andExpect(jsonPath("$.studentEmail").value("tom.cruise@example.com"));
    }
}
