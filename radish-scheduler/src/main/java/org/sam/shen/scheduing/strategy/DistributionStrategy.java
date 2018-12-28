package org.sam.shen.scheduing.strategy;

import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;

import java.util.List;

/**
 * @author clock
 * @date 2018/12/28 上午9:17
 */
public interface DistributionStrategy {

    List<JobEvent> distribute(JobInfo jobInfo, EventStatus status, String parentEventId, String parentGroupId);

}
