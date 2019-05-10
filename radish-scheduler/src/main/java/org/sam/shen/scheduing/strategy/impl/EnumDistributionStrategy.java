package org.sam.shen.scheduing.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.strategy.AbsDistributionStrategy;

/**
 * @author clock
 * @date 2018/12/28 上午9:33
 */
public class EnumDistributionStrategy extends AbsDistributionStrategy {

    @Override
    public List<JobEvent> doDistribute(JobInfo jobInfo, EventStatus status, String parentEventId, String parentGroupId) {
        List<JobEvent> jobEvents = new ArrayList<>();
        long time = System.currentTimeMillis();
        String groupId = UUID.randomUUID().toString().trim().replaceAll("-", "");
        String[] rules = jobInfo.getDistRule().split(",");
        for (int i = 0; i < rules.length; i++) {
            JobEvent jobEvent = new JobEvent(jobInfo.getId(), time, i, groupId, jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
                    status, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams(), jobInfo.getDistType(), rules[i]);
            jobEvent.setParentJobId(jobInfo.getParentJobId());
            jobEvent.setParentEventId(parentEventId);
            jobEvent.setParentGroupId(parentGroupId);
            jobEvent.setParamFilePath(jobInfo.getParamFilePath());
            jobEvents.add(jobEvent);
        }
        return jobEvents;
    }

}
