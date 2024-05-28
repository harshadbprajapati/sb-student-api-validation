package com.example.studentapi.student;

import com.example.studentapi.commons.ApiError;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StudentErrorMapperTest {
    @Test
    void testMapErrors_WithValidErrors_ShouldMapCorrectly() {
        // Arrange
        Map<String, String> errors = new HashMap<>();
        errors.put("firstName", "First name is required");
        errors.put("lastName", "Last name is required");
        errors.put("email", "Email should be valid");

        // Act
        List<ApiError> apiErrors = StudentErrorMapper.mapErrors(errors);

        // Assert
        assertEquals(3, apiErrors.size());
        assertEquals("studentFirstName", apiErrors.get(0).getField());
        assertEquals("First name is required", apiErrors.get(0).getMessage());
        assertEquals("studentLastName", apiErrors.get(1).getField());
        assertEquals("Last name is required", apiErrors.get(1).getMessage());
        assertEquals("studentEmail", apiErrors.get(2).getField());
        assertEquals("Email should be valid", apiErrors.get(2).getMessage());
    }

    @Test
    void testMapErrors_WithEmptyErrors_ShouldReturnEmptyList() {
        // Arrange
        Map<String, String> errors = new HashMap<>();

        // Act
        List<ApiError> apiErrors = StudentErrorMapper.mapErrors(errors);

        // Assert
        assertEquals(0, apiErrors.size());
    }

}