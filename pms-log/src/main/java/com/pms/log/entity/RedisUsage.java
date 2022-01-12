package com.pms.log.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="redisEntity", description="")
public class RedisUsage implements Serializable {

    private static final long serialVersionUID=1L;

    private List<RedisSlowLog> slowLogs;

    private List<TopSlowestCommand> topSlowestCommands;

//    private Long errorLogsTotalCount;

    private List<RedisLog> logs;

    private RedisLogsLevelCountHistogram levelCountHistogram;

}
