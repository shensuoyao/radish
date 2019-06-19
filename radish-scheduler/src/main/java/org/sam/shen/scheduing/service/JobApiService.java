package org.sam.shen.scheduing.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.entity.AppKind;
import org.sam.shen.scheduing.entity.JobAppRef;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.mapper.*;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.sam.shen.scheduing.vo.JobApiVo;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AppInfoMapper appInfoMapper;

    @Autowired
    private JobService jobService;


    /**
     * 获取可执行处理器
     * @author clock
     * @date 2019-06-14 14:43
     * @param appId 应用ID
     * @param kind 分类名称
     * @return 可执行处理器
     */
    private String getExecutors(String appId, String kind) {
        // 查询handler
        AppKind appKind = appKindMapper.selectByAppAndKind(appId, kind);
        if (appKind == null) {
            throw new RuntimeException("无效的kind！");
        }

        String handlers = appKind.getHandlers();
        if (StringUtils.isNotEmpty(handlers)) {
            return handlers;
        } else {
            throw new RuntimeException("无可执行的handler！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveJobAppRef(JobApiVo vo, String kind) {
        vo.setExecutorHandlers(getExecutors(vo.getAppId(), kind));
        // 保存基本信息
        vo.setCreateTime(new Date());
        jobInfoMapper.saveJobInfo(vo);
        JobAppRef jobAppRef = new JobAppRef(Long.toString(vo.getId()), vo.getAppId());
        jobAppRefMapper.insertJobAppRef(jobAppRef);
        // 添加job任务
        // 备注：多个地方需要同样的逻辑判断，可以放在addJob方法中实现
        if (vo.getEnable() == Constant.YES && StringUtils.isNotEmpty(vo.getExecutorHandlers()) && StringUtils.isNotEmpty(vo.getCrontab())) {
            try {
                // new Date()获取的数据带有毫秒数，mysql数据库存的数据不带，所以去掉毫秒数统一标准
                RadishDynamicScheduler.addJob(vo.getId(), vo.getCreateTime().getTime() / 1000 * 1000, vo.getCrontab());
            } catch (SchedulerException e) {
                log.error("Add scheduler failed. [{}]", e.getMessage());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSaveJobAppRef(List<JobApiVo> jobApiVos, String appId, String kind) {
        AppInfo appInfo = appInfoMapper.selectAppInfoById(appId);
        // 查询handler
        String executors = getExecutors(appId, kind);
        // 保存基本信息
        List<JobInfo> jobs = new ArrayList<>();
        for (JobApiVo jav : jobApiVos) {
            jav.setCreateTime(new Date());
            jav.setExecutorHandlers(executors);
            jav.setUserId(appInfo.getUserId());
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
                    // new Date()获取的数据带有毫秒数，mysql数据库存的数据不带，所以去掉毫秒数统一标准
                    RadishDynamicScheduler.addJob(job.getId(), job.getCreateTime().getTime() / 1000 * 1000, job.getCrontab());
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
        this.findJobAppById(jobId, appId);
        // 删除数据库中的job
        jobInfoMapper.deleteJobById(jobId);
        jobAppRefMapper.deleteJobAppRefByJobId(Long.toString(jobId));
    }

    public void unableJobById(long jobId, String appId) {
        JobApiVo job = this.findJobAppById(jobId, appId);
        // 更新任务状态
        job.setEnable(Constant.NO);
        job.setUpdateTime(new Date());
        jobService.upgradeJobInfo(job);
    }

    public void enableJobById(long jobId, String appId) {
        JobApiVo job = this.findJobAppById(jobId, appId);
        // 更新任务状态
        job.setEnable(Constant.YES);
        job.setUpdateTime(new Date());
        jobService.upgradeJobInfo(job);
    }

    public void updateJob(JobApiVo vo, String kind) {
        JobApiVo job = this.findJobAppById(vo.getId(), vo.getAppId());
        // 更新任务
        vo.setUpdateTime(new Date());
        vo.setExecutorHandlers(getExecutors(vo.getAppId(), kind));
        job.update(vo);
        jobService.upgradeJobInfo(job);
    }
}
