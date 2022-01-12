package com.pms.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MemoryHistogram {

    private Bucket used;
    private Bucket free;
    private Bucket cached;
}