package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SlowQueriesCountHistogram {

    private Bucket slowQueriesCount;

}