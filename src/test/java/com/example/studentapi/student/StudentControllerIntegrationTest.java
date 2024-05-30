package com.example.studentapi.student;

import com.example.studentapi.commons.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentControllerIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentRepository studentRepository;

    @Test
    void testGetAllStudents_ShouldReturnListOfStudentDto() throws Exception {
        //Arrange
        List<Student> students = new ArrayList<>();
        students.add(new Student(1L, "Tom", "Cruise", "tom.cruise@example.com"));
        students.add(new Student(2L, "Will", "Smith", "will.smith@example.com"));
        when(studentRepository.findAll()).thenReturn(students);

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
    void testGetStudentById_ExistingId_ShouldReturnStudentDto() throws Exception {
        //Arrange
        long studentId = 1L;
        Student student = new Student(studentId, "Tom", "Cruise", "tom.cruise@example.com");
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        //Act
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/students/{studentId}",
                        studentId)
        );

        //Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.studentFirstName").value("Tom"))
                .andExpect(jsonPath("$.studentLastName").value("Cruise"))
                .andExpect(jsonPath("$.studentEmail").value("tom.cruise@example.com"));
    }

    @Test
    void testGetStudentById_NonExistingId_ShouldThrowResourceNotFoundException() throws Exception {
        // Arrange
        long nonExistingId = 1000L;
        when(studentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act and Assert
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/students/{studentId}",
                        nonExistingId)
        );
        // checks404 Not Found
        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                // checks that the exception thrown by the controller is an instance of ResourceNotFoundException.
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
                // checks that the message of the thrown ResourceNotFoundException matches the expected message
                .andExpect(result -> assertEquals("Student not found with id: " + nonExistingId,
                        result.getResolvedException().getMessage()));
    }

    @Test
    void testCreateStudent_ValidInput_ShouldReturnCreatedStudentDto() throws Exception {
        //Arrange
        StudentDto requestDto = new StudentDto(null,
                "Tom",
                "Cruise",
                "tom.cruise@example.com");
        Student createdStudent = new Student(1L,
                "Tom",
                "Cruise",
                "tom.cruise@example.com");
        when(studentRepository.save(any(Student.class))).thenReturn(createdStudent);
        StudentDto createdStudentDto = new StudentDto(1L,
                "Tom",
                "Cruise",
                "tom.cruise@example.com");

        //Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)));
        //Assert
        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$.studentFirstName").value(createdStudentDto.getStudentFirstName()))
                .andExpect(jsonPath("$.studentLastName").value(createdStudentDto.getStudentLastName()))
                .andExpect(jsonPath("$.studentEmail").value(createdStudentDto.getStudentEmail()));
    }

    @Test
    void testCreateStudent_MissingFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        StudentDto studentDto = new StudentDto(); // Empty student DTO

        // Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)));
        // Assert
        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void testDeleteStudent_ExistingId_ShouldReturnSuccessMessage() throws Exception {
        //Arrange
        Long studentId = 1L;
        when(studentRepository.existsById(studentId)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(anyLong());

        //Act
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/students/{studentId}", studentId));

        //Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Student with studentId "+studentId+" is deleted"));
    }

    @Test
    void testDeleteStudent_NonExistingId_ShouldThrowResourceNotFoundException() throws Exception {
        // Arrange
        long nonExistingId = 1000L;
        doThrow(new ResourceNotFoundException("Student not found with id: " + nonExistingId))
                .when(studentRepository).deleteById(nonExistingId);

        // Act
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/students/{studentId}", nonExistingId)
        );

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException))
                .andExpect(result -> assertEquals("Student not found with id: " + nonExistingId,
                        result.getResolvedException().getMessage()));
    }

    @Test
    void testUpdateStudent_ExistingIdAndValidInput_ShouldReturnUpdatedStudentDto() throws Exception {
        //Arrange
        long studentId = 1L;
        Student oldStudent = new Student(studentId,
                "Tom",
                "Cruise",
                "tom.cruise@example.com");
        StudentDto updatedStudentDto = new StudentDto(studentId,
                "Tomkumar",
                "Cruise",
                "tom.cruise@example.com");
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(oldStudent));
        when(studentRepository.save(Mockito.any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/students/{studentId}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedStudentDto)));
        //Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.studentFirstName").value(updatedStudentDto.getStudentFirstName()))
                .andExpect(jsonPath("$.studentLastName").value(updatedStudentDto.getStudentLastName()))
                .andExpect(jsonPath("$.studentEmail").value(updatedStudentDto.getStudentEmail()));
    }

    @Test
    void testUpdateStudent_InvalidInput_ShouldReturnBadRequest() throws Exception {
        // Arrange
        long studentId = 1L;
        StudentDto studentDto = new StudentDto(studentId,
                "Tom123",
                "Cruise",
                "tom.cruise@example.com"); // Providing invalid input (name contains digits)
        String requestBody = objectMapper.writeValueAsString(studentDto);
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(new Student())); // Mocking student found for update

        // Act
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.patch("/api/students/{studentId}", studentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody));

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
