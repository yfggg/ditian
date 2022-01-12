package com.pms.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SystemLoadHistogram {

    private Bucket load1;
    private Bucket load5;
    private Bucket load15;
}