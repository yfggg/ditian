package com.pms.log.controller;

import com.pms.log.entity.AppUsage;
import com.pms.log.entity.MysqlUsage;
import com.pms.log.service.IAppService;
import com.pms.log.service.IMysqlService;
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
@Api(tags="mysql项目监测")
@Slf4j
@RestController
@RequestMapping("/mysqlUsage")
public class MysqlController {

    @Autowired
    private IMysqlService mysqlService;

    /**
     *
     * @param from 0
     * @param size 50
     * @param startTime 2021-12-28T08:00
     * @param endTime   2022-01-04T16:00
     * @param interval  30 min
     * @return
     */
    @GetMapping(value = "/home")
    public MysqlUsage home(Integer from, Integer size, String startTime, String endTime, Integer interval) {
        return mysqlService.selectAppUsage(from, size, startTime, endTime, interval);
    }


}