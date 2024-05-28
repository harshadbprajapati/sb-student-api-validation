package com.example.studentapi.student;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class StudentRepositoryTest {
    @Autowired
    private StudentRepository studentRepository;

    @Test
    void testFindAll_ShouldReturnAllStudents() {
        // Arrange
        Student student1 = new Student(null, "Tom", "Cruise", "tom.cruise@example.com");
        Student student2 = new Student(null, "Will", "Smith", "will.smith@example.com");
        studentRepository.save(student1);
        studentRepository.save(student2);

        // Act
        List<Student> students = studentRepository.findAll();

        // Assert
        assertEquals(2, students.size());
        assertTrue(students.stream().anyMatch(s -> s.getFirstName().equals("Tom")));
        assertTrue(students.stream().anyMatch(s -> s.getFirstName().equals("Will")));
    }

    @Test
    void testFindById_ExistingId_ShouldReturnStudent() {
        // Arrange
        Student student = new Student(null, "Tom", "Cruise", "tom.cruise@example.com");
        Student savedStudent = studentRepository.save(student);

        // Act
        Optional<Student> optionalStudent = studentRepository.findById(savedStudent.getId());

        // Assert
        assertTrue(optionalStudent.isPresent());
        assertEquals("Tom", optionalStudent.get().getFirstName());
        assertEquals("Cruise", optionalStudent.get().getLastName());
        assertEquals("tom.cruise@example.com", optionalStudent.get().getEmail());
    }
    @Test
    void testSave_ShouldSaveStudent() {
        // Arrange
        Student student = new Student(null, "Tom", "Cruise", "tom.cruise@example.com");

        // Act
        Student savedStudent = studentRepository.save(student);

        // Assert
        assertNotNull(savedStudent.getId());
        assertEquals("Tom", savedStudent.getFirstName());
        assertEquals("Cruise", savedStudent.getLastName());
        assertEquals("tom.cruise@example.com", savedStudent.getEmail());
    }

    @Test
    void testDelete_ShouldDeleteStudent() {
        // Arrange
        Student student = new Student(null, "Tom", "Cruise", "tom.cruise@example.com");
        Student savedStudent = studentRepository.save(student);

        // Act
        studentRepository.delete(savedStudent);

        // Assert
        Optional<Student> optionalStudent = studentRepository.findById(savedStudent.getId());
        assertTrue(optionalStudent.isEmpty());
    }

}