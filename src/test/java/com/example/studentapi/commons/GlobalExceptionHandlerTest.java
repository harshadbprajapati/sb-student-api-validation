package com.example.studentapi.commons;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testValidationException_ShouldReturnBadRequestWithErrors() {
        // Arrange
        ValidationException exception = new ValidationException(List.of(new ApiError("field", "error")));

        // Act
        ResponseEntity<?> response = exceptionHandler.handleValidationException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(List.of(new ApiError("field", "error")), response.getBody());
    }

    @Test
    void testResourceNotFoundException_ShouldReturnNotFoundWithErrorMessage() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceNotFoundException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody());
    }

}