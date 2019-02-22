package org.sam.shen.scheduing.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.scheduing.entity.AppKind;
import org.sam.shen.scheduing.entity.JobAppRef;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.AppKindHandlerMapper;
import org.sam.shen.scheduing.mapper.AppKindMapper;
import org.sam.shen.scheduing.mapper.JobAppRefMapper;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.sam.shen.scheduing.vo.AppKindHandlerVo;
import org.sam.shen.scheduing.vo.JobApiVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author clock
 * @date 2019/1/8 下午2:46
 */
@Slf4j
@Service
public class JobApiService {

    @Resource
    private JobInfoMapper jobInfoMapper;

    @Resource
    private JobAppRefMapper jobAppRefMapper;

    @Resource
    private AppKindMapper appKindMapper;

    @Resource
    private AppKindHandlerMapper appKindHandlerMapper;

    @Transactional(rollbackFor = Exception.class)
    public void saveJobAppRef(JobApiVo vo, String kind) {
        // 查询handler
        AppKind appKind = appKindMapper.selectByAppAndKind(vo.getAppId(), kind);
        if (appKind == null) {
            throw new RuntimeException("无效的kind！");
        }
        List<AppKindHandlerVo> handlers = appKindHandlerMapper.selectKindHandler(appKind.getId());
        if (handlers != null && handlers.size() > 0) {
            String executors = handlers.stream().map(akh -> akh.getAgentId().concat("-").concat(akh.getHandler())).collect(Collectors.joining(","));
            vo.setExecutorHandlers(executors);
        }
        // 保存基本信息
        vo.setCreateTime(new Date());
        jobInfoMapper.saveJobInfo(vo);
        JobAppRef jobAppRef = new JobAppRef(Long.toString(vo.getId()), vo.getAppId());
        jobAppRefMapper.insertJobAppRef(jobAppRef);
        // 添加job任务
        // 备注：多个地方需要同样的逻辑判断，可以放在addJob方法中实现
        if (vo.getEnable() == Constant.YES && StringUtils.isNotEmpty(vo.getExecutorHandlers()) && StringUtils.isNotEmpty(vo.getCrontab())) {
            try {
                RadishDynamicScheduler.addJob(vo.getId(), vo.getJobName(), vo.getCrontab());
            } catch (SchedulerException e) {
                log.error("Add scheduler failed. [{}]", e.getMessage());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSaveJobAppRef(List<JobApiVo> jobApiVos, String appId, String kind) {
        // 查询handler
        String executors = null;
        AppKind appKind = appKindMapper.selectByAppAndKind(appId, kind);
        if (appKind == null) {
            throw new RuntimeException("无效的kind！");
        }
        List<AppKindHandlerVo> handlers = appKindHandlerMapper.selectKindHandler(appKind.getId());
        if (handlers != null && handlers.size() > 0) {
            executors = handlers.stream().map(akh -> akh.getAgentId().concat("-").concat(akh.getHandler())).collect(Collectors.joining(","));
        }
        // 保存基本信息
        List<JobInfo> jobs = new ArrayList<>();
        for (JobApiVo jav : jobApiVos) {
            jav.setCreateTime(new Date());
            jav.setExecutorHandlers(executors);
            jobs.add(jav);
        }
        jobInfoMapper.batchInsert(jobs);
        List<JobAppRef> refs = jobs.stream()
                .map(job -> new JobAppRef(Long.toString(job.getId()), appId))
                .collect(Collectors.toList());
        jobAppRefMapper.batchInsert(refs);
        // 添加job任务
        for (JobInfo job : jobs) {
            if (job.getEnable() == Constant.YES && StringUtils.isNotEmpty(job.getExecutorHandlers()) && StringUtils.isNotEmpty(job.getCrontab())) {
                try {
                    RadishDynamicScheduler.addJob(job.getId(), job.getJobName(), job.getCrontab());
                } catch (SchedulerException e) {
                    log.error("job[{}]: add scheduler failed. [{}]", job.getId(), e.getMessage());
                }
            }
        }
    }

    public JobApiVo findJobAppById(Long jobId, String appId) {
        JobApiVo job = jobInfoMapper.findJobAppById(jobId);
        if (job == null) {
            throw new RuntimeException("无效的任务ID！");
        }
        if (!appId.equals(job.getAppId())) {
            throw new RuntimeException("任务ID和应用ID不匹配！");
        }
        return job;
    }

    public List<JobInfo> findJobsByAppId(String appId) {
        return jobInfoMapper.findJobsByAppId(appId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeJobById(long jobId, String appId) {
        JobApiVo job = this.findJobAppById(jobId, appId);
        // 删除数据库中的job
        jobInfoMapper.deleteJobById(jobId);
        jobAppRefMapper.deleteJobAppRefByJobId(Long.toString(jobId));
        // 移除系统中的job任务
        try {
            RadishDynamicScheduler.removeJob(jobId, job.getJobName());
        } catch (Exception e) {
            log.error("job[{}]: remove job scheduler failed. [{}]", jobId, e.getMessage());
        }
    }

    public void unableJobById(long jobId, String appId) {
        JobApiVo job = this.findJobAppById(jobId, appId);
        // 更新任务状态
        job.setEnable(Constant.NO);
        job.setUpdateTime(new Date());
        jobInfoMapper.upgradeJonInfo(job);
        // 移除运行的job任务
        try {
            RadishDynamicScheduler.pauseJob(jobId, job.getJobName());
        } catch (SchedulerException e) {
            log.error("job[{}]: remove job scheduler failed. [{}]", jobId, e.getMessage());
        }
    }

    public void enableJobById(long jobId, String appId) {
        JobApiVo job = this.findJobAppById(jobId, appId);
        // 更新任务状态
        job.setEnable(Constant.YES);
        job.setUpdateTime(new Date());
        jobInfoMapper.upgradeJonInfo(job);
        // 移除运行的job任务
        try {
            if (!RadishDynamicScheduler.checkExists(job.getId(), job.getJobName())) {
                if (StringUtils.isNotEmpty(job.getExecutorHandlers()) && StringUtils.isNotEmpty(job.getCrontab())) {
                    RadishDynamicScheduler.addJob(job.getId(), job.getJobName(), job.getCrontab());
                }
            } else {
                RadishDynamicScheduler.resumeJob(jobId, job.getJobName());
            }
        } catch (SchedulerException e) {
            log.error("job[{}]: remove job scheduler failed. [{}]", jobId, e.getMessage());
        }
    }

    public void updateJob(JobApiVo vo) {
        JobApiVo job = this.findJobAppById(vo.getId(), vo.getAppId());
        if (StringUtils.isEmpty(vo.getJobName()) || !vo.getJobName().equals(job.getJobName())) {
            throw new RuntimeException("任务名称不允许修改！");
        }
        // 更新任务
        vo.setUpdateTime(new Date());
        jobInfoMapper.upgradeJonInfo(vo);
        // 更新定时任务
        try {
            if (vo.getEnable() == Constant.YES) {
                if (StringUtils.isNotEmpty(job.getExecutorHandlers()) && StringUtils.isNotEmpty(vo.getCrontab())) {
                    RadishDynamicScheduler.UpgradeScheduleJob(vo.getId(), vo.getJobName(), vo.getCrontab());
                }
            } else {
                RadishDynamicScheduler.pauseJob(vo.getId(), vo.getJobName());
            }
        } catch (SchedulerException e) {
            log.error("job[{}]: update job scheduler failed. [{}]", vo.getId(), e.getMessage());
        }
    }
}
