package com.pms.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class NetworkTrafficHistogram {

    private Bucket inbound;
    private Bucket outbound;
}