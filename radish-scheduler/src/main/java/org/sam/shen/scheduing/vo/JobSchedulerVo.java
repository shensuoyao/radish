package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.scheduing.entity.JobScheduler;

/**
 * @author clock
 * @date 2019/3/27 下午5:08
 */
@Getter
@Setter
public class JobSchedulerVo extends JobScheduler {

    private String jobName;

    private String crontab;

}
