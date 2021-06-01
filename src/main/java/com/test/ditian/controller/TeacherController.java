package com.test.ditian.controller;

import com.test.ditian.domain.Teacher;
import com.test.ditian.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TeacherController {

    @Autowired
    TeacherMapper teacherMapper;

    @GetMapping("/select")
    @ResponseBody
    public Teacher select() {
        long id = 2;
        Teacher teacher = teacherMapper.selectTeacherById(id);
        return teacher;
    }

    @GetMapping("/insert")
    @ResponseBody
    public int insert() {
        long id = 2;
        long school_id = 2;
        long teacher_code = 444;
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setSchoolId(school_id);
        teacher.setTeacherCode(teacher_code);
        teacherMapper.insertTeacher(teacher);
        return 1;
    }

    @GetMapping("/update")
    @ResponseBody
    public int update() {
        long id = 2;
        long school_id = 3455;
        long teacher_code = 44478;
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setSchoolId(school_id);
        teacher.setTeacherCode(teacher_code);
        teacherMapper.updateTeacher(teacher);
        return 1;
    }

    @GetMapping("/delete")
    @ResponseBody
    public int delete() {
        long id = 2;
        teacherMapper.deleteTeacherById(id);
        return 1;
    }
}
