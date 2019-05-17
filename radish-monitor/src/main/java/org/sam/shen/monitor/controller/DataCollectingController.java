package org.sam.shen.monitor.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.MonitorInfo;
import org.sam.shen.core.model.Resp;
import org.sam.shen.monitor.thread.MonitoringCenter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author clock
 * @date 2019-05-13 18:06
 */
@Slf4j
@RestController
public class DataCollectingController {

    private final MonitoringCenter monitoringCenter;

    public DataCollectingController(MonitoringCenter monitoringCenter) {
        this.monitoringCenter = monitoringCenter;
    }

    @PostMapping("collect")
    public Resp<String> dataCollect(@RequestBody MonitorInfo monitorInfo) {
        if (StringUtils.isNotEmpty(monitorInfo.getBizId())) {
            for (String bizId : monitorInfo.getBizId().split(",")) {
                MonitorInfo monitor = new MonitorInfo(monitorInfo.getId(), bizId, monitorInfo.getMonitorType(),
                        monitorInfo.getExtra(), monitorInfo.getCreateTime(), monitorInfo.getAlarmType());
                monitoringCenter.offerMonitorInfo(monitor);
            }
        }
        return Resp.SUCCESS;
    }


}
