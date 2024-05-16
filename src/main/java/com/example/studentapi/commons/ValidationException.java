package com.example.studentapi.commons;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends RuntimeException {

    private List<ApiError> errors;

    public ValidationException(List<ApiError> errors) {
        this.errors = errors;
    }

}