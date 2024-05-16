package com.example.studentapi.student;

import java.util.List;

public interface StudentService {
    StudentDto createStudent(StudentDto studentDto);
    List<StudentDto> getAllStudents();
    StudentDto getStudentById(Long id);
    void deleteStudent(Long id);

    StudentDto updateStudent(Long id, StudentDto studentDto);
}
