package com.example.studentapi.student;

import com.example.studentapi.commons.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class StudentServiceIntegrationTest {
    @Autowired
    private StudentService studentService;

    @Test
    void testCreateStudent_ShouldReturnStudentDto() {
        // Arrange
        StudentDto studentDto = new StudentDto();
        studentDto.setStudentFirstName("Tom");
        studentDto.setStudentLastName("Cruise");
        studentDto.setStudentEmail("tom.cruise@example.com");

        // Act
        StudentDto createdStudentDto = studentService.createStudent(studentDto);

        // Assert
        assertNotNull(createdStudentDto);
        assertNotNull(createdStudentDto.getId());
        assertEquals(studentDto.getStudentFirstName(), createdStudentDto.getStudentFirstName());
        assertEquals(studentDto.getStudentLastName(), createdStudentDto.getStudentLastName());
        assertEquals(studentDto.getStudentEmail(), createdStudentDto.getStudentEmail());
    }

    @Test
    void testGetAllStudents_ShouldReturnStudentDtos() {
        // Arrange: Populate the database with sample student data
        StudentDto student1 = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto student2 = new StudentDto(null, "Will", "Smith", "will.smith@example.com");
        studentService.createStudent(student1);
        studentService.createStudent(student2);

        // Act: Call the getAllStudents method
        List<StudentDto> students = studentService.getAllStudents();

        // Assert: Verify that the method returns a non-null list of students
        assertNotNull(students);
        assertEquals(2, students.size());
        // Optionally, assert more specific conditions on the returned list
        assertEquals("Tom", students.get(0).getStudentFirstName());
        assertEquals("Cruise", students.get(0).getStudentLastName());
        assertEquals("Will", students.get(1).getStudentFirstName());
        assertEquals("Smith", students.get(1).getStudentLastName());
    }

    @Test
    void testGetStudentById_WithValidId_ShouldReturnStudentDto() {
        // Arrange: Populate the database with sample student data
        StudentDto student = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto savedStudent = studentService.createStudent(student);

        // Act: Call the getStudentById method with the ID of the saved student
        StudentDto retrievedStudent = studentService.getStudentById(savedStudent.getId());

        // Assert: Verify that the retrieved student matches the saved student
        assertNotNull(retrievedStudent);
        assertEquals(savedStudent.getId(), retrievedStudent.getId());
        assertEquals(savedStudent.getStudentFirstName(), retrievedStudent.getStudentFirstName());
        assertEquals(savedStudent.getStudentLastName(), retrievedStudent.getStudentLastName());
        assertEquals(savedStudent.getStudentEmail(), retrievedStudent.getStudentEmail());
    }

    @Test
    void testDeleteStudent_WithValidId_ShouldDeleteStudent() {
        // Arrange: Populate the database with a sample student
        StudentDto student = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto savedStudent = studentService.createStudent(student);

        // Act: Call the deleteStudent method with the ID of the saved student
        studentService.deleteStudent(savedStudent.getId());

        // Assert: Verify that the student has been deleted from the database
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(savedStudent.getId()));
    }

    @Test
    void testUpdateStudent_WithValidIdAndDto_ShouldReturnUpdatedStudentDto() {
        // Arrange: Populate the database with a sample student
        StudentDto student = new StudentDto(null, "Tom", "Cruise", "tom.cruise@example.com");
        StudentDto savedStudent = studentService.createStudent(student);

        // Create an updated student DTO
        StudentDto updatedStudentDto = new StudentDto();
        updatedStudentDto.setStudentFirstName("Will");
        updatedStudentDto.setStudentLastName("Smith");
        updatedStudentDto.setStudentEmail("will.smith@example.com");

        // Act: Call the updateStudent method with the ID of the saved student and the updated DTO
        StudentDto updatedStudent = studentService.updateStudent(savedStudent.getId(), updatedStudentDto);

        // Assert: Verify that the student has been updated correctly
        assertNotNull(updatedStudent);
        assertEquals(savedStudent.getId(), updatedStudent.getId());
        assertEquals(updatedStudentDto.getStudentFirstName(), updatedStudent.getStudentFirstName());
        assertEquals(updatedStudentDto.getStudentLastName(), updatedStudent.getStudentLastName());
        assertEquals(updatedStudentDto.getStudentEmail(), updatedStudent.getStudentEmail());
    }
}
