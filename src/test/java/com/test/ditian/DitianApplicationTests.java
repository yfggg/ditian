package com.test.ditian;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
class DitianApplicationTests {

    @Autowired
    DataSource dataSource;

    @Test
    void contextLoads() throws SQLException {
        //druid数据源
        //class com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceWrapper
        System.out.println(dataSource.getClass());
        //jdbc连接
        //com.alibaba.druid.proxy.jdbc.ConnectionProxyImpl
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        connection.close();
    }

}
