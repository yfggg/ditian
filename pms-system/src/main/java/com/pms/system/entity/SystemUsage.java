package com.pms.system.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="系统监测Entity", description="")
public class SystemUsage implements Serializable {

    private static final long serialVersionUID=1L;

    private String diskPctUsage;
    private String memoryPctUsage;
    private String cpuPctUsage;
    private String memoryTotalUsage;
    private String memoryUsedUsage;
    private String inboundTraffic;
    private String outboundTraffic;
    private String inTotalTransferred;
    private String outTotalTransferred;
    private String processesUsage;

    private CpuHistogram cpuHistogramUsage;
    private MemoryHistogram memoryHistogramUsage;
    private SystemLoadHistogram systemLoadHistogramUsage;
    private NetworkTrafficHistogram networkTrafficHistogramUsage;


}
