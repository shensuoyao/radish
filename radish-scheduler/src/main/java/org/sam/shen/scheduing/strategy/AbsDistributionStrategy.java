package org.sam.shen.scheduing.strategy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.entity.JobEvent;
import org.sam.shen.scheduing.entity.JobInfo;

import java.util.Collections;
import java.util.List;

/**
 * @author clock
 * @date 2018/12/28 下午1:17
 */
@Slf4j
public abstract class AbsDistributionStrategy implements DistributionStrategy {

    @Override
    public List<JobEvent> distribute(JobInfo jobInfo, EventStatus status, String parentEventId, String parentGroupId) {
        if (StringUtils.isEmpty(jobInfo.getDistRule())) {
            log.error("job[{}] have no distribution rule.", jobInfo.getId());
            return Collections.emptyList();
        }
        return doDistribute(jobInfo, status, parentEventId, parentGroupId);
    }

    public abstract List<JobEvent> doDistribute(JobInfo jobInfo, EventStatus status, String parentEventId, String parentGroupId);
}
