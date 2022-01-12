package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ErrorLog {

    private String date;
    private String level;
    private String message;
}
