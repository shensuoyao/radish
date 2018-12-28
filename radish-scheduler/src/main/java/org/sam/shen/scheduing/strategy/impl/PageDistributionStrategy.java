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
 * @date 2018/12/28 上午9:36
 */
@Slf4j
public class PageDistributionStrategy extends AbsDistributionStrategy {

    @Override
    public List<JobEvent> doDistribute(JobInfo jobInfo, EventStatus status, String parentEventId, String parentGroupId) {
        /*
        分页规则应为：totalPage|totalCount
        生成的eventRule可直接用于limit查询
         */
        String distRule = jobInfo.getDistRule();
        try {
            int totalPage = Integer.parseInt(distRule.split("\\|")[0]);
            int totalCount = Integer.parseInt(distRule.split("\\|")[1]);
            // 如果分页数大于数据总量，则不需要分页
            if (totalPage > totalCount) {
                JobEvent jobEvent = new JobEvent(jobInfo.getId(), jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
                        status, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams());
                jobEvent.setDistType(jobInfo.getDistType());
                jobEvent.setEventRule("0".concat(",").concat(Integer.toString(totalCount)));
                jobEvent.setParentJobId(jobInfo.getParentJobId());
                jobEvent.setParentEventId(parentEventId);
                jobEvent.setParentGroupId(parentGroupId);
                return Collections.singletonList(jobEvent);
            }

            // 对数据进行分页，生成eventRule，可直接用于limit查询
            List<JobEvent> jobEvents = new ArrayList<>();
            long time = System.currentTimeMillis();
            String groupId = UUID.randomUUID().toString().trim().replaceAll("-", "");

            int pageCount = totalCount / totalPage;
            for (int i = 0; i < totalPage; i++) {
                if (i == totalPage - 1) { // 当循环到最后一页时，将剩下的数据都放到最后一页
                    pageCount = totalCount - (totalPage - 1) * pageCount;
                }
                String eventRule = Integer.toString(i * pageCount).concat(",").concat(Integer.toString(pageCount));
                JobEvent jobEvent = new JobEvent(jobInfo.getId(), time, i, groupId, jobInfo.getExecutorHandlers(), jobInfo.getHandlerType(),
                        status, jobInfo.getPriority(), jobInfo.getCmd(), jobInfo.getParams(), jobInfo.getDistType(), eventRule);
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

}
