package com.pms.system.controller;

import com.pms.system.entity.SystemUsage;
import com.pms.system.service.ISystemService;
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
@Api(tags="系统监测")
@Slf4j
@RestController
@RequestMapping("/systemUsage")
public class SystemController {

    @Autowired
    private ISystemService systemUsageService;

    /**
     *
     * @param startTime 2021-12-21T16:00
     * @param endTime   2021-12-23T10:05
     * @param interval  30 min
     * @return
     */
    @GetMapping(value = "/home")
    public SystemUsage home(String startTime, String endTime, Integer interval) {
        return systemUsageService.selectTotalUsage(startTime, endTime, interval);
    }


}

