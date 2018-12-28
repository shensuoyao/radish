package org.sam.shen.scheduing.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.strategy.AbsDistributionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author clock
 * @date 2018/12/28 上午9:38
 */
@Slf4j
public class ModDistributionStrategy extends AbsDistributionStrategy {

    @Override
    public List<JobEvent> doDistribute(JobInfo jobInfo, EventStatus status, String parentEventId, String parentGroupId) {
        try {
            int mod = Integer.parseInt(jobInfo.getDistRule());
            List<JobEvent> jobEvents = new ArrayList<>(mod);
            long time = System.currentTimeMillis();
            String groupId = UUID.randomUUID().toString().trim().replaceAll("-", "");
            for (int i = 0; i < mod; i++) {
                JobEvent jobEvent = new JobEvent(jobInfo.getId(), time, i, groupId, jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
                        status, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams(), jobInfo.getDistType(), Integer.toString(i));
                jobEvent.setParentJobId(jobInfo.getParentJobId());
                jobEvent.setParentEventId(parentEventId);
                jobEvent.setParentGroupId(parentGroupId);
                jobEvents.add(jobEvent);
            }
            return jobEvents;
        } catch (NumberFormatException e) {
            log.error("job[{}]: mod must be integer.", jobInfo.getId());
        }
        return Collections.emptyList();
    }

}
