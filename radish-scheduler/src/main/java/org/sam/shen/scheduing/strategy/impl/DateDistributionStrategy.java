package org.sam.shen.scheduing.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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
        日期规则的应为：startTime-endTime|period
        period只支持y、m、d、h
         */
        String distRule = jobInfo.getDistRule();
        try {
            List<JobEvent> jobEvents = new ArrayList<>();
            long time = System.currentTimeMillis();
            String groupId = UUID.randomUUID().toString().trim().replaceAll("-", "");

            String dateRange = distRule.split("\\|")[0];
            String periodStr = distRule.split("\\|")[1];
            if (!isCorrectPeriod(periodStr)) {
                throw new RuntimeException();
            }
            int period = Integer.parseInt(periodStr.replaceAll("[ymdh]", ""));

            String startTimeStr = dateRange.split("-")[0];
            String endTimeStr = dateRange.split("-")[1];
            DateTime startTime = new DateTime(startTimeStr);
            DateTime endTime = new DateTime(endTimeStr);
            // 根据开始时间、结束时间、时间间隔切分时间段
            for (int i = 0; startTime.isBefore(endTime); i++) {
                StringBuilder eventRule = new StringBuilder();
                eventRule.append("[").append(startTime.toString());
                if (periodStr.endsWith("h")) {
                    startTime.plusHours(period);
                } else if (periodStr.endsWith("d")) {
                    startTime.plusDays(period);
                } else if (periodStr.endsWith("m")) {
                    startTime.plusMonths(period);
                } else if (periodStr.endsWith("y")) {
                    startTime.plusYears(period);
                }
                // 如果超过结束时间，则以结束时间为时间下限
                if (startTime.isAfter(endTime)) {
                    eventRule.append(endTime.toString()).append("]");
                }
                eventRule.append(startTime.toString()).append(")");
                // 创建event
                JobEvent jobEvent = new JobEvent(jobInfo.getId(), time, i, groupId, jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
                        status, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams(), jobInfo.getDistType(), eventRule.toString());
                jobEvent.setParentJobId(jobInfo.getParentJobId());
                jobEvent.setParentEventId(parentEventId);
                jobEvent.setParentGroupId(parentGroupId);
                jobEvents.add(jobEvent);
            }
            return jobEvents;
        } catch (Exception e) {
            log.error("job[{}]: rule[{}] format is incorrect.");
        }
        return Collections.emptyList();
    }

    private boolean isCorrectPeriod(String period) {
        Pattern pattern = Pattern.compile("[0-9]*[ymdh]");
        Matcher matcher = pattern.matcher(period);
        return matcher.matches();
    }

}
