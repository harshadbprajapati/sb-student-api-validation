package com.example.studentapi;

import com.example.studentapi.student.StudentController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StudentapiApplicationTests {
	@Autowired
	private StudentController studentController;

	@Test
	void contextLoads() {
		Assertions.assertThat(studentController).isNotNull();
	}

}
