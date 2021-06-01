package com.test.ditian.mapper;

import com.test.ditian.domain.Teacher;

//@Mapper
public interface TeacherMapper
{

    /**
     * 通过用户ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象信息
     */
    public Teacher selectTeacherById(Long id);

    /**
     * 新增用户信息
     *
     * @param teacher 用户信息
     * @return 结果
     */
    public int insertTeacher(Teacher teacher);

    /**
     * 修改用户信息
     *
     * @param teacher 用户信息
     * @return 结果
     */
    public int updateTeacher(Teacher teacher);

    /**
     * 通过用户ID删除用户
     *
     * @param id 用户ID
     * @return 结果
     */
    public int deleteTeacherById(Long id);
}
