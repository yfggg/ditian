package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RedisLog {
    private String time;
    private String hostName;
    private String level;
    private String role;
    private String message;
}
