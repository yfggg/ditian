package com.test.ditian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//使用此批注来注册MyBatis映射器接口。
@MapperScan(basePackages = {"com.test.ditian.mapper"})
public class DitianApplication {

    public static void main(String[] args) {
        SpringApplication.run(DitianApplication.class, args);
        System.out.println("启动成功");
    }

}
