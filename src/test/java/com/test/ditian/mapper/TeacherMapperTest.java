package com.test.ditian.mapper;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.*;

@SpringBootTest
public class TeacherMapperTest {

    @Autowired
    TeacherMapper teacherMapper;

    @Test
    public void selectTeacherById() {
        System.out.println("123");
        long id = 1;
        teacherMapper.selectTeacherById(id);
    }
}