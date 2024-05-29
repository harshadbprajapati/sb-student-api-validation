package com.example.studentapi.student;

import com.example.studentapi.commons.ResourceNotFoundException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {
    @Mock
    private Validator validator;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void testGetAllStudents_ShouldReturnAllStudentDto() {
        // Arrange
        List<Student> students = List.of(
                new Student(1L, "Tom", "Cruise", "tom.cruise@example.com"),
                new Student(2L, "Will", "Smith", "will.smith@example.com")
        );
        when(studentRepository.findAll()).thenReturn(students);
        when(modelMapper.map(any(Student.class), eq(StudentDto.class))).thenAnswer(
                invocation -> {
                    Student student = invocation.getArgument(0);
                    return new StudentDto(student.getId(),
                            student.getFirstName(),
                            student.getLastName(),
                            student.getEmail());
                });

        // Act
        List<StudentDto> result = studentService.getAllStudents();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Tom", result.get(0).getStudentFirstName());
        assertEquals("Cruise", result.get(0).getStudentLastName());
        assertEquals("tom.cruise@example.com", result.get(0).getStudentEmail());
        assertEquals("Will", result.get(1).getStudentFirstName());
        assertEquals("Smith", result.get(1).getStudentLastName());
        assertEquals("will.smith@example.com", result.get(1).getStudentEmail());
    }

    @Test
    void testGetStudentById_ExistingId_ShouldReturnExistingStudentDto() {
        // Arrange
        Long studentId = 1L;
        Student student = new Student(studentId, "Tom", "Cruise", "tom.cruise@example.com");
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(modelMapper.map(student, StudentDto.class)).thenReturn(new StudentDto(
                studentId,
                "Tom",
                "Cruise",
                "tom.cruise@example.com"));

        // Act
        StudentDto result = studentService.getStudentById(studentId);

        // Assert
        assertNotNull(result);
        assertEquals(studentId, result.getId());
        assertEquals("Tom", result.getStudentFirstName());
        assertEquals("Cruise", result.getStudentLastName());
        assertEquals("tom.cruise@example.com", result.getStudentEmail());
    }

    @Test
    void testCreateStudent_WithValidDto_ShouldReturnCreatedStudentDto() {
        // Arrange
        StudentDto studentDto = new StudentDto();
        studentDto.setStudentFirstName("Tom");
        studentDto.setStudentLastName("Cruise");
        studentDto.setStudentEmail("Tom.Cruise@example.com");

        Student studentEntity = new Student();
        studentEntity.setId(1L);
        studentEntity.setFirstName(studentDto.getStudentFirstName());
        studentEntity.setLastName(studentDto.getStudentLastName());
        studentEntity.setEmail(studentDto.getStudentEmail());

        when(modelMapper.map(studentDto, Student.class)).thenReturn(studentEntity);
        when(studentRepository.save(studentEntity)).thenReturn(studentEntity);
        when(modelMapper.map(studentEntity, StudentDto.class)).thenReturn(studentDto);
        when(validator.validate(studentEntity)).thenReturn(Collections.emptySet());

        // Act
        StudentDto createdStudentDto = studentService.createStudent(studentDto);

        // Assert
        assertNotNull(createdStudentDto);
        assertEquals(studentDto, createdStudentDto);
        assertEquals(studentDto.getStudentFirstName(), createdStudentDto.getStudentFirstName());
        assertEquals(studentDto.getStudentLastName(), createdStudentDto.getStudentLastName());
        assertEquals(studentDto.getStudentEmail(), createdStudentDto.getStudentEmail());

        verify(studentRepository, times(1)).save(studentEntity);
    }

    @Test
    void testDeleteStudent_ExistingId_ShouldDeleteStudent() {
        // Arrange
        Long studentId = 1L;
        Student student = new Student(studentId, "Tom", "Cruise", "tom.cruise@example.com");
        when(studentRepository.existsById(studentId)).thenReturn(true);

        // Act
        studentService.deleteStudent(studentId);

        // Assert
        verify(studentRepository, times(1)).deleteById(studentId);
    }

    @Test
    void testDeleteStudent_NonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long studentId = 1L;
        when(studentRepository.existsById(studentId)).thenReturn(false);

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudent(studentId));
    }

    @Test
    void testUpdateStudent_ExistingId_ShouldUpdateStudent() {
        // Arrange
        Long studentId = 1L;
        Student existingStudent = new Student(studentId,
                "Tom",
                "Cruise",
                "tom.cruise@example.com");
        StudentDto studentDto = new StudentDto(
                studentId,
                "Will",
                "Smith",
                "will.smith@example.com");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));
        when(modelMapper.map(studentDto, Student.class)).thenReturn(existingStudent);
        when(studentRepository.save(existingStudent)).thenReturn(existingStudent);
        when(modelMapper.map(existingStudent, StudentDto.class)).thenReturn(studentDto);

        // Act
        StudentDto updatedStudentDto = studentService.updateStudent(studentId, studentDto);

        // Assert
        assertNotNull(updatedStudentDto);
        assertEquals(studentId, updatedStudentDto.getId());
        assertEquals("Will", updatedStudentDto.getStudentFirstName());
        assertEquals("Smith", updatedStudentDto.getStudentLastName());
        assertEquals("will.smith@example.com", updatedStudentDto.getStudentEmail());
    }

    @Test
    void testUpdateStudent_NonExistingId_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long studentId = 1L;
        StudentDto studentDto = new StudentDto(
                studentId,
                "Tom",
                "Cruise",
                "tom.cruise@example.com");

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class,
                () -> studentService.updateStudent(studentId, studentDto));
    }
}