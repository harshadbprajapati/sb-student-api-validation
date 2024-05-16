package com.example.studentapi.student;

import com.example.studentapi.commons.ResourceNotFoundException;
import com.example.studentapi.commons.ValidationException;
import jakarta.validation.Validator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentServiceImpl implements StudentService {

    private final Validator validator;

    private final StudentRepository studentRepository;

    private final ModelMapper modelMapper;

    private final StudentErrorMapper studentErrorMapper;

    public StudentServiceImpl(Validator validator,
                                StudentRepository studentRepository,
                                ModelMapper modelMapper,
                                StudentErrorMapper studentErrorMapper) {
        this.validator = validator;
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
        this.studentErrorMapper = studentErrorMapper;
    }

    @Override
    public List<StudentDto> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public StudentDto getStudentById(Long id) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (optionalStudent.isEmpty()) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        return convertToDto(optionalStudent.get());
    }

    @Override
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    @Override
    public StudentDto createStudent(StudentDto studentDto) {
        Student student = convertToEntity(studentDto);

        // Validate the student entity
        Map<String, String> errorsMap = validateStudentEntity(student);
        if(!errorsMap.isEmpty()){
            throw new ValidationException(StudentErrorMapper.mapErrors(errorsMap));
        }

        Student savedStudent = studentRepository.save(student);
        return convertToDto(savedStudent);
    }

    @Override
    public StudentDto updateStudent(Long id, StudentDto studentDto) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (!optionalStudent.isPresent()) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }

        Student existingStudent = optionalStudent.get();
        Student updateRequestStudent = modelMapper.map(studentDto, Student.class);
        // Merge non-null properties of update request with existingStudent
        mergeNonNullProperties(updateRequestStudent, existingStudent);

        // Validate the updated student entity
        Map<String, String> errorsMap = validateStudentEntity(existingStudent);
        if(!errorsMap.isEmpty()){
            throw new ValidationException(StudentErrorMapper.mapErrors(errorsMap));
        }

        Student updatedStudent = studentRepository.save(existingStudent);
        return convertToDto(updatedStudent);
    }
    private Map<String, String> validateStudentEntity(Student student) {
        return validator.validate(student).stream()
                .map(violation -> Map.entry(violation.getPropertyPath().toString(),violation.getMessage()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void mergeNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    private Student convertToEntity(StudentDto studentDto) {
        return modelMapper.map(studentDto, Student.class);
    }

    private StudentDto convertToDto(Student student) {
        return modelMapper.map(student, StudentDto.class);
    }

    public static class StudentDtoToEntityMapper {
        public static void configure(ModelMapper modelMapper) {
            modelMapper.typeMap(StudentDto.class, Student.class)
                    .addMappings(mapper -> {
                        // Add mapping configurations here
                        mapper.map(StudentDto::getStudentFirstName, Student::setFirstName);
                        mapper.map(StudentDto::getStudentLastName, Student::setLastName);
                        mapper.map(StudentDto::getStudentEmail, Student::setEmail);
                    });
        }
    }

    public static class StudentEntityToDtoMapper {
        public static void configure(ModelMapper modelMapper) {
            modelMapper.typeMap(Student.class, StudentDto.class)
                    .addMappings(mapper -> {
                        // Add mapping configurations here
                        mapper.map(Student::getFirstName, StudentDto::setStudentFirstName);
                        mapper.map(Student::getLastName, StudentDto::setStudentLastName);
                        mapper.map(Student::getEmail, StudentDto::setStudentEmail);
                    });
        }
    }
}