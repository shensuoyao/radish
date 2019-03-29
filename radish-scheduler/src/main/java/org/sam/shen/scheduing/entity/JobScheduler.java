package org.sam.shen.scheduing.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author clock
 * @date 2019/3/27 下午4:32
 */
@Getter
@Setter
public class JobScheduler {

    public enum RunningStatus {
        RUNNING, PAUSED, STOP
    }

    private Long id;

    private Long jobId;

    private Integer nid;

    private RunningStatus runningStatus;

    private Date prevFireTime;

    private Date nextFireTime;

    public JobScheduler() {}

    public JobScheduler(Long jobId, RunningStatus runningStatus, Date prevFireTime, Date nextFireTime) {
        this.jobId = jobId;
        this.runningStatus = runningStatus;
        this.prevFireTime = prevFireTime;
        this.nextFireTime = nextFireTime;
    }

    public JobScheduler(Long jobId, Integer nid, RunningStatus runningStatus, Date prevFireTime, Date nextFireTime) {
        this(jobId, runningStatus, prevFireTime, nextFireTime);
        this.nid = nid;
    }
}
