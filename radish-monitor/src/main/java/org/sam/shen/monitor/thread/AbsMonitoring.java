package org.sam.shen.monitor.thread;


import org.sam.shen.core.model.MonitorInfo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author clock
 * @date 2019-05-15 10:29
 */
public abstract class AbsMonitoring extends Thread implements IMonitoring {

    private boolean isRunning = true;

    protected AlarmCenter alarmCenter;

    private BlockingQueue<MonitorInfo> monitorInfoBlockingQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while (isRunning) {
            MonitorInfo monitorInfo = monitorInfoBlockingQueue.poll();
            if (monitorInfo != null) {
                process(monitorInfo);
            }
        }
    }

    @Override
    public boolean offerMonitorInfo(MonitorInfo monitorInfo) {
        return monitorInfoBlockingQueue.offer(monitorInfo);
    }

    @Override
    public void setAlarmCenter(AlarmCenter alarmCenter) {
        this.alarmCenter = alarmCenter;
    }

    @Override
    public void close() {
        isRunning = false;
    }

    public abstract void process(MonitorInfo monitorInfo);

}
