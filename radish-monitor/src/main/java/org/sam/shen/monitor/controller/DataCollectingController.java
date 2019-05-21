package org.sam.shen.monitor.controller;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.MonitorInfo;
import org.sam.shen.core.model.Resp;
import org.sam.shen.monitor.thread.MonitoringCenter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据采集
 * @author clock
 * @date 2019-05-13 18:06
 */
@RestController
public class DataCollectingController {

    private final MonitoringCenter monitoringCenter;

    public DataCollectingController(MonitoringCenter monitoringCenter) {
        this.monitoringCenter = monitoringCenter;
    }

    /**
     * 采集监控信息
     * @author clock
     * @date 2019-05-21 14:51
     * @param monitorInfo 监控信息
     * @return 请求结果
     */
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
