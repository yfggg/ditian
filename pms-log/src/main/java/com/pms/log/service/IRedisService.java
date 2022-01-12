package com.pms.log.service;

import com.pms.log.entity.RedisUsage;

public interface IRedisService {

    RedisUsage selectAppUsage(Integer from, Integer size, String startTime, String endTime, Integer interval);

}