package com.example.studentapi.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {
    @Test
    void testApiError_ConstructorAndGetters() {
        // Arrange
        String field = "testField";
        String message = "testMessage";

        // Act
        ApiError apiError = new ApiError(field, message);

        // Assert
        assertNotNull(apiError);
        assertEquals(field, apiError.getField());
        assertEquals(message, apiError.getMessage());
    }

    @Test
    void testApiError_SetterAndGetters() {
        // Arrange
        ApiError apiError = new ApiError();
        String field = "testField";
        String message = "testMessage";

        // Act
        apiError.setField(field);
        apiError.setMessage(message);

        // Assert
        assertEquals(field, apiError.getField());
        assertEquals(message, apiError.getMessage());
    }

}