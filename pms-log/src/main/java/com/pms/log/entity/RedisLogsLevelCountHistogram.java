package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RedisLogsLevelCountHistogram {
    private Bucket verbose;
    private Bucket debug;
    private Bucket warning;
    private Bucket notice;
}