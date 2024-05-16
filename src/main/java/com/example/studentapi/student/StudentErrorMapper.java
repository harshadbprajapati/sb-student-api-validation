package com.example.studentapi.student;

import com.example.studentapi.commons.ApiError;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StudentErrorMapper {
    private StudentErrorMapper() {}

    private static final Map<String, String> FIELD_MAPPING = new HashMap<>();

    static {
        FIELD_MAPPING.put("firstName", "studentFirstName");
        FIELD_MAPPING.put("lastName", "studentLastName");
        FIELD_MAPPING.put("email", "studentEmail");
    }

    public static List<ApiError> mapErrors(Map<String, String> errors) {
        return errors.entrySet().stream()
                .map(entry -> new ApiError(FIELD_MAPPING.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue()))
                .toList();
    }
}