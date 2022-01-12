package com.pms.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class InboundAndOutbound {

//    private String inboundTraffic;
//    private String outboundTraffic;
    private String inTotalTransferred;
    private String outTotalTransferred;
}
