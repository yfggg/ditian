package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RedisSlowLog {
    private String time;
    private String hostName;
    private String message;
    private Integer slowlogDurationUs;
    private String slowlogKey;
}
