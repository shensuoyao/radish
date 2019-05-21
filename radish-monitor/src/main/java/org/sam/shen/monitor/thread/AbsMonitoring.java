package org.sam.shen.monitor.thread;


import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.MonitorInfo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author clock
 * @date 2019-05-15 10:29
 */
public abstract class AbsMonitoring extends Thread implements IMonitoring {

    private boolean isRunning = true;

    // 监控中心
    protected AlarmCenter alarmCenter;

    // 监控信息队列
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

    /**
     * 将监控信息添加到队列中
     * @author clock
     * @date 2019-05-21 16:26
     * @param monitorInfo 监控信息
     * @return 添加结果
     */
    @Override
    public boolean offerMonitorInfo(MonitorInfo monitorInfo) {
        return monitorInfoBlockingQueue.offer(monitorInfo);
    }

    /**
     * 设置告警中心
     * @author clock
     * @date 2019-05-21 16:28
     * @param alarmCenter 告警中心
     */
    @Override
    public void setAlarmCenter(AlarmCenter alarmCenter) {
        this.alarmCenter = alarmCenter;
    }

    /**
     * 优雅关闭当前线程
     * @author clock
     * @date 2019-05-21 16:29
     */
    @Override
    public void close() {
        isRunning = false;
    }

    /**
     * 处理监控信息的抽象方法
     * @author clock
     * @date 2019-05-21 16:29
     * @param monitorInfo 监控信息
     */
    public abstract void process(MonitorInfo monitorInfo);

    /**
     * 判断字符串是否为整数
     */
    protected boolean isInteger(String i) {
        if (StringUtils.isEmpty(i)) {
            return false;
        }
        try {
            Integer.parseInt(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
