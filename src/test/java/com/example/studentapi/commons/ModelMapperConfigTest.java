package com.example.studentapi.commons;

import com.example.studentapi.student.Student;
import com.example.studentapi.student.StudentDto;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;

import static org.junit.jupiter.api.Assertions.*;

class ModelMapperConfigTest {
    @Test
    void testModelMapperBeanCreation() {
        // Arrange
        ModelMapperConfig modelMapperConfig = new ModelMapperConfig();

        // Act
        ModelMapper modelMapper = modelMapperConfig.modelMapper();

        // Assert
        assertNotNull(modelMapper);
    }

    @Test
    void testModelMapperMappings() {
        // Arrange
        ModelMapperConfig modelMapperConfig = new ModelMapperConfig();
        ModelMapper modelMapper = modelMapperConfig.modelMapper();

        // Act
        TypeMap<StudentDto, Student> dtoToEntityMapping = modelMapper.getTypeMap(StudentDto.class, Student.class);
        TypeMap<Student, StudentDto> entityToDtoMapping = modelMapper.getTypeMap(Student.class, StudentDto.class);

        // Assert
        assertNotNull(dtoToEntityMapping,
                "Mapping from StudentDto to Student is not configured");
        assertNotNull(entityToDtoMapping,
                "Mapping from Student to StudentDto is not configured");
    }

}