package org.sam.shen.monitor.thread;


import org.sam.shen.core.model.MonitorInfo;

/**
 * @author clock
 * @date 2019-05-15 09:16
 */
public interface IMonitoring {

    void setAlarmCenter(AlarmCenter alarmCenter);

    boolean offerMonitorInfo(MonitorInfo monitorInfo);

    void close();

}
