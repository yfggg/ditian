package com.pms.log.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="mysqlEntity", description="")
public class MysqlUsage implements Serializable {

    private static final long serialVersionUID=1L;

    private List<TopSlowestQueries> topSlowQueries;

    private SlowQueriesCountHistogram slowQueriesHistogram;

//    private Long errorLogsTotalCount;

    private List<ErrorLog> errorLogs;

    private ErrorCountHistogram errorLogsOverTime;

}
