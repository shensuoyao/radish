package org.sam.shen.monitor.thread;

import lombok.extern.slf4j.Slf4j;
import org.clock.sms.builder.SendSmsBuilder;
import org.clock.sms.builder.SmsRequestBodyBuilder;
import org.clock.sms.entity.SmsRequestBody;
import org.sam.shen.core.sendcloud.SendEmailClient;
import org.sam.shen.core.util.SystemUtil;
import org.sam.shen.core.model.Alarm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 告警中心
 * @author clock
 * @date 2019-05-15 17:31
 */
@Slf4j
public class AlarmCenter extends Thread {

    private boolean isRunning;

    private BlockingQueue<Alarm> alarms;

    private ExecutorService threadPool;

    public AlarmCenter() {
        isRunning = true;
        alarms = new LinkedBlockingQueue<>();
        threadPool = Executors.newFixedThreadPool(SystemUtil.cpuCount() + 1);
    }

    @Override
    public void run() {
        while (isRunning) {
            Alarm alarm = alarms.poll();
            if (alarm != null) {
                threadPool.execute(new AlarmSender(alarm));
            }
        }
    }

    public void offerAlarm(Alarm alarm) {
        alarms.offer(alarm);
    }

    public void close() {
        isRunning = false;
    }

    class AlarmSender implements Runnable {

        private Alarm alarm;

        AlarmSender(Alarm alarm) {
            this.alarm = alarm;
        }

        @Override
        public void run() {
            // 发送短信或者邮件
            try {
                if ("EMAIL".equals(alarm.getAlarmType())) {
                    SendEmailClient.sendEmail(alarm.getEmail(), "调度任务告警", alarm.getContent(), null);
                } else if ("SMS".equals(alarm.getAlarmType())) {
                    SmsRequestBody smsRequestBody = SmsRequestBodyBuilder.createNoticeBody();
                    smsRequestBody.addSmsContentWithTemplateKey(new String[] {alarm.getPhone()}, alarm.getTemplate(), alarm.getTemplateParams());
                    SendSmsBuilder.create().buildRequestBody(smsRequestBody).build().send();
                }
            } catch (Exception e) {
                log.error("Send alarm message[{}] failed. [{}]", alarm.toString(), e.getMessage());
            }
        }

    }

}
