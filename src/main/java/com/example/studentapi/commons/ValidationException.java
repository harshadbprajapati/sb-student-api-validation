package com.example.studentapi.commons;

import java.util.List;

public class ValidationException extends RuntimeException {

    private List<ApiError> errors;

    public ValidationException(List<ApiError> errors) {
        this.errors = errors;
    }

    public List<ApiError> getErrors() {
        return errors;
    }
}