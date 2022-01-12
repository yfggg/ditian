package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TopSlowestQueries {

    private String query;
    private String user;
    private String queryTime;
}
