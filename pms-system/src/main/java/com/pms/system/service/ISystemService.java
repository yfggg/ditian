package com.pms.system.service;


import com.pms.system.entity.SystemUsage;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang fan
 * @since 2021-12-13
 */
public interface ISystemService {

    SystemUsage selectTotalUsage(String startTime, String endTime, Integer interval);

}
