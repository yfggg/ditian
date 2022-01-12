package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TopSlowestCommand {
    private String command;
    private String duration;
}
