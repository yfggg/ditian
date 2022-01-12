package com.pms.log.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="appEntity", description="")
public class AppUsage implements Serializable {

    private static final long serialVersionUID=1L;

    private List<ErrorLog> errorsQuerryLog;

    private ErrorCountHistogram errorCountHistogram;


}
