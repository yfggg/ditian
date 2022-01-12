package com.pms.log.service;

import com.pms.log.entity.AppUsage;

public interface IAppService {

    AppUsage selectAppUsage(Integer from, Integer size, String startTime, String endTime, Integer interval);

}