package org.sam.shen.monitor.thread;

import lombok.extern.slf4j.Slf4j;
import org.sam.shen.core.constants.MonitorType;
import org.sam.shen.core.model.MonitorInfo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控中心
 * @author clock
 * @date 2019-05-14 11:45
 */
@Slf4j
public class MonitoringCenter {

    private ConcurrentHashMap<String, IMonitoring> monitoringMap;

    private AlarmCenter alarmCenter;

    public MonitoringCenter(AlarmCenter alarmCenter) {
        this.alarmCenter = alarmCenter;
        monitoringMap = new ConcurrentHashMap<>();
    }

    public void offerMonitorInfo(MonitorInfo monitorInfo) {
        MonitorType monitorType = monitorInfo.getMonitorType();
        if (monitorType == null) {
            return;
        }
        if (monitoringMap.get(monitorType.name()) == null) {
            synchronized (this) {
                if (monitoringMap.get(monitorType.name()) == null) {
                    String className = "org.sam.shen.monitor.thread.impl." + monitorType.getClassName();
                    try {
                        Class clazz = Class.forName(className);
                        AbsMonitoring monitoring = (AbsMonitoring) clazz.newInstance();
                        monitoring.setAlarmCenter(alarmCenter);
                        monitoring.start();
                        monitoringMap.put(monitorType.name(), monitoring);
                    } catch (Exception e) {
                        log.error("{} class initialization has failed.[{}]", monitorType.getClassName(), e.getMessage());
                    }
                }
            }
        }
        if (monitoringMap.get(monitorType.name()) != null) {
            monitoringMap.get(monitorType.name()).offerMonitorInfo(monitorInfo);
        }
    }

}
