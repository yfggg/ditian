package com.pms.log.service;

import com.pms.log.entity.MysqlUsage;

public interface IMysqlService {

    MysqlUsage selectAppUsage(Integer from, Integer size, String startTime, String endTime, Integer interval);

}