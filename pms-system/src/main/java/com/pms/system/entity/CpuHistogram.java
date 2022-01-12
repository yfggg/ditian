package com.pms.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CpuHistogram {

    private Bucket user;
    private Bucket system;
    private Bucket nice;
    private Bucket irq;
    private Bucket softirq;
    private Bucket iowait;
}
