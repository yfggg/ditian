package com.pms.log.controller;

import com.pms.log.entity.AppUsage;
import com.pms.log.service.IAppService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yang fan
 * @since 2021-12-13
 */
@Api(tags="spring项目监测")
@Slf4j
@RestController
@RequestMapping("/appUsage")
public class AppController {

    @Autowired
    private IAppService appService;

    /**
     * @param from 0
     * @param size 5
     * @param startTime 2021-12-31T08:00
     * @param endTime   2022-01-04T16:00
     * @param interval  30 min
     * @return
     */
    @GetMapping(value = "/home")
    public AppUsage home(Integer from, Integer size, String startTime, String endTime, Integer interval) {
        return appService.selectAppUsage(from, size, startTime, endTime, interval);
    }


}