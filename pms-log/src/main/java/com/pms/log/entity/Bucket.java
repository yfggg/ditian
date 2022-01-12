package com.pms.log.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Bucket {
    private List<String> dates;
    private List<String> values;
}
