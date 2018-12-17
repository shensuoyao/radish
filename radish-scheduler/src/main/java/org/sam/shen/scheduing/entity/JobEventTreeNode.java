package org.sam.shen.scheduing.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author clock
 * @date 2018/12/13 上午10:52
 */
@Setter
@Getter
public class JobEventTreeNode {

    private JobEvent jobEvent;

    private JobInfo jobInfo;

    private List<JobEventTreeNode> children;

}
