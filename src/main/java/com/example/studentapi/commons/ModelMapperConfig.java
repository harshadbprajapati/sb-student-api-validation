package com.example.studentapi.commons;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.studentapi.student.StudentServiceImpl;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        configureMappings(modelMapper);
        return modelMapper;
    }

    private void configureMappings(ModelMapper modelMapper) {
        StudentServiceImpl.StudentDtoToEntityMapper.configure(modelMapper);
        StudentServiceImpl.StudentEntityToDtoMapper.configure(modelMapper);
    }
}