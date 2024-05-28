package com.example.studentapi.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllStudents_ShouldReturnListOfStudents() throws Exception {
        // Arrange
        StudentDto student1 = new StudentDto(1L, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto student2 = new StudentDto(2L, "Will", "Smith", "will.smith@example.com");
        List<StudentDto> students = Arrays.asList(student1, student2);
        when(studentService.getAllStudents()).thenReturn(students);

        // Act
        ResultActions response = mockMvc.perform(get("/api/students"));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].studentFirstName", Matchers.is("Tom")))
                .andExpect(jsonPath("$[0].studentLastName", Matchers.is("Cruise")))
                .andExpect(jsonPath("$[0].studentEmail", Matchers.is("tom.cruise@example.com")))
                .andExpect(jsonPath("$[1].studentFirstName", Matchers.is("Will")))
                .andExpect(jsonPath("$[1].studentLastName", Matchers.is("Smith")))
                .andExpect(jsonPath("$[1].studentEmail", Matchers.is("will.smith@example.com")));
    }

    @Test
    void testGetStudentById_ExistingId_ShouldReturnStudent() throws Exception {
        // Arrange
        StudentDto student = new StudentDto(1L, "Tom", "Cruise", "tom.cruise@example.com");
        when(studentService.getStudentById(1L)).thenReturn(student);

        // Act
        ResultActions response = mockMvc.perform(get("/api/students/{studentId}", 1L));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.studentFirstName").value("Tom"))
                .andExpect(jsonPath("$.studentLastName").value("Cruise"))
                .andExpect(jsonPath("$.studentEmail").value("tom.cruise@example.com"));
    }

    @Test
    void testCreateStudent_ValidInput_ShouldReturnCreatedStudent() throws Exception {
        // Arrange
        StudentDto studentDto = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto createdStudentDto = new StudentDto(1L, "Tom", "Cruise", "tom.cruise@example.com");
        when(studentService.createStudent(studentDto)).thenReturn(createdStudentDto);

        // Act
        ResultActions response = mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studentDto)));

        //Assert
        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.studentFirstName",
                        CoreMatchers.is(createdStudentDto.getStudentFirstName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.studentLastName",
                        CoreMatchers.is(createdStudentDto.getStudentLastName())));
    }

    @Test
    void testDeleteStudent_ExistingId_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        doNothing().when(studentService).deleteStudent(1L);

        // Act
        ResultActions response = mockMvc.perform(delete("/api/students/{studentId}", 1L));

        // Assert
        response.andExpect(status().isOk())
                .andExpect(content().string("Student with studentId 1 is deleted"));
    }
    @Test
    void testUpdateStudent_ExistingIdAndValidInput_ShouldReturnUpdatedStudent() throws Exception {
        // Arrange
        StudentDto updateRequestStudentDto = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto updatedStudentDto = new StudentDto(1L, "Tom", "Cruise", "tom.cruise@example.com");
        when(studentService.updateStudent(anyLong(), Mockito.any(StudentDto.class))).thenReturn(updatedStudentDto);

        // Act
        ResultActions response = mockMvc.perform(patch("/api/students/{studentId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestStudentDto)));
        // Assert
        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.studentFirstName").value("Tom"))
                .andExpect(jsonPath("$.studentLastName").value("Cruise"))
                .andExpect(jsonPath("$.studentEmail").value("tom.cruise@example.com"));
    }
}