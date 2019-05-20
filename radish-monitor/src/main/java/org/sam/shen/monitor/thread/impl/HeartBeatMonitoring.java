package org.sam.shen.monitor.thread.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.Alarm;
import org.sam.shen.core.model.MonitorInfo;
import org.sam.shen.monitor.entity.Notifier;
import org.sam.shen.monitor.service.NotifierService;
import org.sam.shen.monitor.thread.AbsMonitoring;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author clock
 * @date 2019-05-15 09:20
 */
@Slf4j
public class HeartBeatMonitoring extends AbsMonitoring {

    private final String timeoutKey = "timeout";

    private ConcurrentHashMap<String, MonitorInfo> monitorInfoMap;

    private TimeoutListener listener;

    public HeartBeatMonitoring() {
        monitorInfoMap = new ConcurrentHashMap<>();
        listener = new TimeoutListener();
        listener.start();
    }

    @Override
    public void process(MonitorInfo monitorInfo) {
        String bizId = monitorInfo.getBizId();
        Map<String, String> extra = monitorInfo.getExtra();
        if (extra != null && !StringUtils.isEmpty(bizId) && isInteger(extra.get(timeoutKey))) {
            monitorInfoMap.put(bizId, monitorInfo);
        }
    }

    /**
     * 优雅关闭线程
     */
    @Override
    public void close() {
        listener.close();
        super.close();
    }

    /**
     * 监听心跳超时的线程
     */
    private class TimeoutListener extends Thread {

        private boolean isRunning;

        TimeoutListener() {
            this.isRunning = true;
        }

        @Override
        public void run() {
            while (isRunning) {
                for (String key : monitorInfoMap.keySet()) {
                    try {
                        MonitorInfo monitorInfo = monitorInfoMap.get(key);
                        long timeout = Long.parseLong(monitorInfo.getExtra().get(timeoutKey));
                        long createTime = monitorInfo.getCreateTime().getTime();
                        if (System.currentTimeMillis() - createTime > timeout) {
                            // 获取告警人
                            Notifier notifier = NotifierService.getNotifierOfAgent(monitorInfo.getBizId());
                            if (notifier == null || (StringUtils.isEmpty(notifier.getPhone()) && StringUtils.isEmpty(notifier.getEmail()))) {
                                log.warn("event-{}未设置报警人联系方式！", monitorInfo.getBizId());
                            } else {
                                Alarm alarm = new Alarm();
                                alarm.setId(UUID.randomUUID().toString().replaceAll("-", ""));
                                alarm.setAlarmType(monitorInfo.getAlarmType());
                                alarm.setNotifier(notifier.getName());
                                alarm.setContent("agent-" + monitorInfo.getBizId() + "已断开连接!");
                                if ("EMAIL".equals(alarm.getAlarmType()) && StringUtils.isNotEmpty(notifier.getEmail())) {
                                    alarm.setEmail(notifier.getEmail());
                                    alarmCenter.offerAlarm(alarm);
                                } else if ("SMS".equals(alarm.getAlarmType()) && StringUtils.isNotEmpty(notifier.getPhone())) {
                                    alarm.setPhone(notifier.getPhone());
                                    alarmCenter.offerAlarm(alarm);
                                } else {
                                    log.warn("event-{}未设置报警人联系方式！", monitorInfo.getBizId());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("agent[{}] occurred exception when check whether agent is offline, {}", key, e.getMessage());
                    } finally {
                        monitorInfoMap.remove(key);
                    }
                }
            }
        }

        void close() {
            isRunning = false;
        }
    }

}
