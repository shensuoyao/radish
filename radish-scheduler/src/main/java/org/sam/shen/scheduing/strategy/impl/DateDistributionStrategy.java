package org.sam.shen.scheduing.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.strategy.AbsDistributionStrategy;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author clock
 * @date 2018/12/28 上午9:39
 */
@Slf4j
public class DateDistributionStrategy extends AbsDistributionStrategy {

    @Override
    public List<JobEvent> doDistribute(JobInfo jobInfo, EventStatus status, String parentEventId, String parentGroupId) {
        /*
        日期规则的应为：startTime～endTime|period
        period只支持y、m、d、h
         */
        String distRule = jobInfo.getDistRule();
        try {
            List<JobEvent> jobEvents = new ArrayList<>();
            long time = System.currentTimeMillis();
            String groupId = UUID.randomUUID().toString().trim().replaceAll("-", "");

            String dateFormat = distRule.split("\\|")[0];
            String dateRange = distRule.split("\\|")[1];
            String periodStr = distRule.split("\\|")[2];
            if (!isCorrectPeriod(periodStr)) {
                throw new RuntimeException();
            }
            int period = Integer.parseInt(periodStr.replaceAll("[ymdh]", ""));

            String startTimeStr = dateRange.split("～")[0];
            String endTimeStr = dateRange.split("～")[1];
            DateTime startTime = DateTime.parse(startTimeStr, DateTimeFormat.forPattern(dateFormat));
            DateTime endTime = DateTime.parse(endTimeStr, DateTimeFormat.forPattern(dateFormat));
            // 根据开始时间、结束时间、时间间隔切分时间段
            for (int i = 0; startTime.isBefore(endTime); i++) {
                StringBuilder eventRule = new StringBuilder();
                eventRule.append("[").append(startTime.toString(dateFormat));
                if (periodStr.endsWith("h")) {
                    startTime = startTime.plusHours(period);
                } else if (periodStr.endsWith("d")) {
                    startTime = startTime.plusDays(period);
                } else if (periodStr.endsWith("m")) {
                    startTime = startTime.plusMonths(period);
                } else if (periodStr.endsWith("y")) {
                    startTime = startTime.plusYears(period);
                }
                // 如果超过结束时间，则以结束时间为时间下限
                if (startTime.isAfter(endTime)) {
                    eventRule.append(",").append(endTime.toString(dateFormat)).append("]");
                } else {
                    eventRule.append(",").append(startTime.toString(dateFormat)).append(")");
                }
                // 创建event
                JobEvent jobEvent = new JobEvent(jobInfo.getId(), time, i, groupId, jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
                        status, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams(), jobInfo.getDistType(), eventRule.toString());
                jobEvent.setParentJobId(jobInfo.getParentJobId());
                jobEvent.setParentEventId(parentEventId);
                jobEvent.setParentGroupId(parentGroupId);
                jobEvent.setParamFilePath(jobInfo.getParamFilePath());
                jobEvents.add(jobEvent);
            }
            return jobEvents;
        } catch (Exception e) {
            log.error("job[{}]: rule[{}] format is incorrect.", jobInfo.getId(), jobInfo.getDistRule());
        }
        return Collections.emptyList();
    }

    /**
     * 校验时间段字符串是否符合规范
     * @author clock
     * @date 2019/1/2 下午2:38
     * @param period 时间段规则
     * @return 是否符合规范
     */
    private boolean isCorrectPeriod(String period) {
        Pattern pattern = Pattern.compile("[0-9]*[ymdh]");
        Matcher matcher = pattern.matcher(period);
        return matcher.matches();
    }

    public static void main(String[] args) {
        int j = 0;
        for (int i = 0; j < 10; i++) {
            j++;
            System.out.println(i);
        }
    }

}
