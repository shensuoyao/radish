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
 * 事件监控现线程
 * @author clock
 * @date 2019-05-16 15:35
 */
@Slf4j
public class EventMonitoring extends AbsMonitoring {

    private final String timeoutKey = "timeout";

    // 事件处理到哪个阶段
    private final String stepKey = "step";

    // 刚初始化的事件Map
    private ConcurrentHashMap<String, MonitorInfo> initialEventMap;

    // 正在处理的事件Map
    private ConcurrentHashMap<String, MonitorInfo> handleEventMap;

    // 监听初始化事件Map，如果超时未被其他客户端获取，则产生超时该告警
    private InitializationTimeout initializationListener;

    // 监听处理的事件Map，如果超时未执行完毕，则产生超时告警
    private HandleTimeout handleListener;

    public EventMonitoring() {
        initialEventMap = new ConcurrentHashMap<>();
        handleEventMap = new ConcurrentHashMap<>();
        initializationListener = new InitializationTimeout();
        handleListener = new HandleTimeout();
        initializationListener.start();
        handleListener.start();
    }

    @Override
    public void process(MonitorInfo monitorInfo) {
        String bizId = monitorInfo.getBizId();
        Map<String, String> extra = monitorInfo.getExtra();
        if (extra != null && !StringUtils.isEmpty(bizId) && isInteger(extra.get(timeoutKey)) && isInteger(extra.get(stepKey))) {
            String step = extra.get(stepKey);
            if ("1".equals(step)) {
                initialEventMap.put(bizId, monitorInfo);
            } else if ("2".equals(step)) {
                initialEventMap.remove(bizId);
                handleEventMap.put(bizId, monitorInfo);
            } else if ("3".equals(step)) {
                handleEventMap.remove(bizId);
            }
        }
    }

    @Override
    public void close() {
        initializationListener.close();
        handleListener.close();
        super.close();
    }

    /**
     * 监听创建的event，无客户端抢占导致的超时
     */
    private class InitializationTimeout extends Thread {
        private boolean isRunning;

        InitializationTimeout() {
            isRunning = true;
        }

        @Override
        public void run() {
            while (isRunning) {
                for (String key : initialEventMap.keySet()) {
                    try {
                        MonitorInfo monitorInfo = initialEventMap.get(key);
                        long timeout = Long.parseLong(monitorInfo.getExtra().get(timeoutKey));
                        long createTime = monitorInfo.getCreateTime().getTime();
                        if (System.currentTimeMillis() - createTime > timeout) {
                            // 获取告警人
                            Notifier notifier = NotifierService.getNotifierOfJob(monitorInfo.getBizId());
                            if (notifier == null || (StringUtils.isEmpty(notifier.getPhone()) && StringUtils.isEmpty(notifier.getEmail()))) {
                                log.warn("event[{}]未设置报警人联系方式！", monitorInfo.getBizId());
                            } else {
                                Alarm alarm = new Alarm();
                                alarm.setId(UUID.randomUUID().toString().replaceAll("-", ""));
                                alarm.setAlarmType(monitorInfo.getAlarmType());
                                alarm.setNotifier(notifier.getName());
                                if ("EMAIL".equals(alarm.getAlarmType()) && StringUtils.isNotEmpty(notifier.getEmail())) {
                                    alarm.setEmail(notifier.getEmail());
                                    alarm.setContent(notifier.getBizName() + "[EVENT-" + monitorInfo.getBizId() + "]已超时，未有客户端抢占处理!");
                                    alarmCenter.offerAlarm(alarm);
                                } else if ("SMS".equals(alarm.getAlarmType()) && StringUtils.isNotEmpty(notifier.getPhone())) {
                                    alarm.setPhone(notifier.getPhone());
                                    alarm.setTemplate("event_chao");
                                    alarm.setTemplateParams(new String[] {notifier.getBizName() + ":" + monitorInfo.getBizId()});
                                    alarmCenter.offerAlarm(alarm);
                                } else {
                                    log.warn("event[{}]未设置报警人联系方式！", monitorInfo.getBizId());
                                }
                            }
                            initialEventMap.remove(key);
                        }
                    } catch (Exception e) {
                        initialEventMap.remove(key);
                        log.error("event[{}] occurred exception when check whether initial event is timeout, {}", key, e.getMessage());
                    }
                }
            }
        }

        void close() {
            isRunning = false;
        }
    }

    /**
     * 监听处理中的event，未正常结束或阻塞线程
     */
    private class HandleTimeout extends Thread {
        private boolean isRunning;

        HandleTimeout() {
            isRunning = true;
        }

        @Override
        public void run() {
            while (isRunning) {
                for (String key : handleEventMap.keySet()) {
                    try {
                        MonitorInfo monitorInfo = handleEventMap.get(key);
                        long timeout = Long.parseLong(monitorInfo.getExtra().get(timeoutKey));
                        long createTime = monitorInfo.getCreateTime().getTime();
                        if (System.currentTimeMillis() - createTime > timeout) {
                            // 获取告警人
                            Notifier notifier = NotifierService.getNotifierOfJob(monitorInfo.getBizId());
                            if (notifier == null || (StringUtils.isEmpty(notifier.getPhone()) && StringUtils.isEmpty(notifier.getEmail()))) {
                                log.warn("event[{}]未设置报警人联系方式！", monitorInfo.getBizId());
                            } else {
                                Alarm alarm = new Alarm();
                                alarm.setId(UUID.randomUUID().toString().replaceAll("-", ""));
                                alarm.setAlarmType(monitorInfo.getAlarmType());
                                alarm.setNotifier(notifier.getName());

                                // 查询执行客户端信息
                                Map<String, Object> agent = NotifierService.selectAgentByEventId(monitorInfo.getBizId());
                                String agentName = agent == null ? null : String.valueOf(agent.get("agentName"));

                                // 根据告警方式生成告警通知
                                if ("EMAIL".equals(alarm.getAlarmType()) && StringUtils.isNotEmpty(notifier.getEmail())) {
                                    alarm.setContent(notifier.getBizName() + "[EVENT-" + monitorInfo.getBizId() + "]客户端[" + agentName + "]执行超时，处理该事件的线程被阻塞或异常结束!");
                                    alarm.setEmail(notifier.getEmail());
                                    alarmCenter.offerAlarm(alarm);
                                } else if ("SMS".equals(alarm.getAlarmType()) && StringUtils.isNotEmpty(notifier.getPhone())) {
                                    alarm.setTemplate("event_error");
                                    alarm.setTemplateParams(new String[] {notifier.getBizName() + ":" + monitorInfo.getBizId(), agentName});
                                    alarm.setPhone(notifier.getPhone());
                                    alarmCenter.offerAlarm(alarm);
                                } else {
                                    log.warn("event[{}]未设置报警人联系方式！", monitorInfo.getBizId());
                                }
                            }
                            handleEventMap.remove(key);
                        }
                    } catch (Exception e) {
                        handleEventMap.remove(key);
                        log.error("event[{}] occurred exception when check whether handle event is timeout, {}", key, e.getMessage());
                    }
                }
            }
        }

        void close() {
            isRunning = false;
        }
    }

}
