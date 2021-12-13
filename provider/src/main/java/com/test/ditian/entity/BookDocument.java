package com.test.ditian.entity;

import io.searchbox.annotations.JestId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookDocument {

    @JestId
    private String id;
    private String bookName;
    private String bookAuthor;
    private Integer pages;
    private String desc;
}

