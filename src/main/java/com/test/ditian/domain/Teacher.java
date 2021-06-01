package com.test.ditian.domain;

import java.util.Date;

public class Teacher
{
    private Long id;

    private Long schoolId;

    private Long teacherCode;

    private String avatar;

    private String userName;

    private String sex;

    private String title;

    private String subject;

    private Long phone;

    private Date startTeachDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getTeacherCode() {
        return teacherCode;
    }

    public void setTeacherCode(Long teacherCode) {
        this.teacherCode = teacherCode;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public Date getStartTeachDate() {
        return startTeachDate;
    }

    public void setStartTeachDate(Date startTeachDate) {
        this.startTeachDate = startTeachDate;
    }
}
