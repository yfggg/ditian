package com.test.ditian.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookRequest {

    //删除文档用
    private String id;
    //查询用
    private String keyword;
    private String indexName;
    private String typeName;
    //新增文档用
    private BookDocument body;
}

