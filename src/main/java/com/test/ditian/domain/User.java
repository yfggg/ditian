package com.test.ditian.domain;

import cn.afterturn.easypoi.excel.annotation.Excel;

public class User {

    @Excel(name = "姓名", width = 10)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
