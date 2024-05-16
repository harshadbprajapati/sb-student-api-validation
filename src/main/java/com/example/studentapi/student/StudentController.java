package com.example.studentapi.student;

import com.example.studentapi.commons.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Get all students")
    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        List<StudentDto> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @Operation(summary = "Get a specific student specified by studentId")
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long studentId) {
        StudentDto student = studentService.getStudentById(studentId);
        return ResponseEntity.ok(student);
    }

    @Operation(summary = "Delete a specific student specified by studentId")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.ok("Student with studentId " + studentId + " is deleted");
    }

    @Operation(summary = "Create a new student specified by request body")
    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody StudentDto studentDto, BindingResult result) {
        if (result.hasErrors()) {
            List<ApiError> errors = result.getFieldErrors().stream()
                    .map(fieldError -> new ApiError(fieldError.getField(), fieldError.getDefaultMessage()))
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }
        StudentDto createdStudent = studentService.createStudent(studentDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @Operation(summary = "Partial update a student specified by studentId and by request body")
    @PatchMapping("/{studentId}")
    public ResponseEntity<?> updateStudent(@PathVariable("studentId") Long studentId, @RequestBody StudentDto studentDto) {
        StudentDto updatedStudent = studentService.updateStudent(studentId, studentDto);
        return ResponseEntity.ok(updatedStudent);
    }
}