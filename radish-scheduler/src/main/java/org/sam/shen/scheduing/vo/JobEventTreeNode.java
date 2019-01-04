package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author clock
 * @date 2018/12/13 上午10:52
 */
@Setter
@Getter
public class JobEventTreeNode {

    private String id;

    private String pid;

    private boolean isGroup;

    List<JobEventVo> events;

    private List<JobEventTreeNode> children;

    public JobEventTreeNode(List<JobEventVo> events) {
        this.events = events;
        String groupId = events.get(0).getJobEvent().getGroupId();
        String parentGroupId = events.get(0).getJobEvent().getParentGroupId();
        if (StringUtils.isNotEmpty(groupId)) {
            isGroup = true;
            this.id = events.get(0).getJobEvent().getGroupId();
        } else {
            isGroup = false;
            this.id = events.get(0).getJobEvent().getEventId();
        }
        this.pid = StringUtils.isNotEmpty(parentGroupId) ? parentGroupId : events.get(0).getJobEvent().getParentEventId();
    }
}
