package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.scheduing.entity.JobEvent;

/**
 * @author clock
 * @date 2019-06-21 13:55
 */
@Setter
@Getter
public class JobEventPageVo extends JobEvent {

    private static final long serialVersionUID = -6047732751625614885L;

    private String jobName;

}
