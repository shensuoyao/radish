package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;
import org.sam.shen.scheduing.entity.JobScheduler;

import java.util.Date;

/**
 * @author clock
 * @date 2019/3/27 下午5:08
 */
@Getter
@Setter
public class JobSchedulerVo extends JobScheduler {

    private Date createTime;

    private String jobName;

    private String crontab;

}
