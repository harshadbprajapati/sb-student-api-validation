package com.example.studentapi.commons;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {
    @Test
    void testValidationException_ConstructorAndGetErrors() {
        // Arrange
        List<ApiError> errors = new ArrayList<>();
        errors.add(new ApiError("field1", "error1"));
        errors.add(new ApiError("field2", "error2"));

        // Act
        ValidationException exception = new ValidationException(errors);

        // Assert
        assertNotNull(exception);
        assertEquals(errors, exception.getErrors());
    }

}