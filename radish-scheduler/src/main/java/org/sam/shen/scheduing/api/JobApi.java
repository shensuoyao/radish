package org.sam.shen.scheduing.api;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.HandlerFailStrategy;
import org.sam.shen.core.constants.HandlerType;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.sam.shen.scheduing.service.JobApiService;
import org.sam.shen.scheduing.service.JobService;
import org.sam.shen.scheduing.vo.JobApiVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author clock
 * @date 2019/1/7 下午5:00
 */
@RestController
@RequestMapping("api")
public class JobApi {

    private final JobApiService jobApiService;

    private final JobService jobService;

    public JobApi(JobApiService jobApiService, JobService jobService) {
        this.jobApiService = jobApiService;
        this.jobService = jobService;
    }

    /**
     * 新增job，并创建定时任务
     * @author clock
     * @date 2019/1/9 下午5:52
     * @param jobApiVo 任务
     * @return 任务ID
     */
    @RequestMapping(value = "/jobs", method = RequestMethod.POST)
    public Resp<Long> addJob(@RequestBody JobApiVo jobApiVo, @RequestHeader String appId, @RequestHeader String kind) {
        // 设置默认值
        if (jobApiVo.getHandlerType() == null) {
            jobApiVo.setHandlerType(HandlerType.H_JAVA);
        }
        if (jobApiVo.getHandlerFailStrategy() == null) {
            jobApiVo.setHandlerFailStrategy(HandlerFailStrategy.DISCARD);
        }
        if (jobApiVo.getPriority() == null) {
            jobApiVo.setPriority(0);
        }
//        if (jobApiVo.getEnable() == null) {
//            jobApiVo.setEnable(1);
//        }
        jobApiVo.setAppId(appId);
        jobApiService.saveJobAppRef(jobApiVo, kind);
        return new Resp<>(jobApiVo.getId());
    }

    /**
     * 批量新增job，创建定时任务
     * @author clock
     * @date 2019/1/11 下午5:25
     * @param jobApiVos 多任务
     * @return 批量创建任务结果
     */
    @RequestMapping(value = "/jobs/bulk", method = RequestMethod.POST)
    public Resp<String> addJobs(@RequestBody List<JobApiVo> jobApiVos, @RequestHeader String appId, @RequestHeader String kind) {
        jobApiService.batchSaveJobAppRef(jobApiVos, appId, kind);
        return Resp.SUCCESS;
    }

    /**
     * 根据任务ID获取任务
     * @author clock
     * @date 2019/1/11 下午5:26
     * @param jobId 任务ID
     * @return 任务信息
     */
    @RequestMapping(value = "/jobs/{jobId}", method = RequestMethod.GET)
    public Resp<JobApiVo> getJob(@PathVariable Long jobId, @RequestHeader String appId) {
        JobApiVo job = jobApiService.findJobAppById(jobId, appId);
        return new Resp<>(job);
    }

    /**
     * 获取某个应用创建的所有任务
     * @author clock
     * @date 2019/1/11 下午5:26
     * @param appId 应用ID
     * @return 任务信息
     */
    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    public Resp<List<JobInfo>> getAppJobs(@RequestHeader String appId) {
        List<JobInfo> data = jobApiService.findJobsByAppId(appId);
        return new Resp<>(data);
    }

    /**
     * 更新任务信息
     * @author clock
     * @date 2019/1/11 下午5:26
     * @param jobId 任务ID
     * @param vo 需要更新的任务信息
     * @return 处理结果
     */
    @RequestMapping(value = "/jobs/{jobId}", method = RequestMethod.PUT)
    public Resp<String> updateJob(@RequestBody JobApiVo vo, @PathVariable Long jobId, @RequestHeader String appId,
                                  @RequestHeader String kind) {
        vo.setId(jobId);
        vo.setAppId(appId);
        jobApiService.updateJob(vo, kind);
        return Resp.SUCCESS;
    }

    /**
     * 移除任务
     * @author clock
     * @date 2019/1/11 下午5:26
     * @param jobId 任务ID
     * @param appId 应用ID
     * @return 处理结果
     */
    @RequestMapping(value = "/jobs/{jobId}", method = RequestMethod.DELETE)
    public Resp<String> removeJob(@PathVariable Long jobId, @RequestHeader String appId) {
        jobApiService.removeJobById(jobId, appId);
        return Resp.SUCCESS;
    }

    /**
     * 暂停任务
     * @author clock
     * @date 2019/1/11 下午5:27
     * @param jobId 任务ID
     * @param appId 应用ID
     * @return 处理结果
     */
    @RequestMapping(value = "/jobs/{jobId}/unable", method = RequestMethod.POST)
    public Resp<String> unableJob(@PathVariable Long jobId, @RequestHeader String appId) {
        jobApiService.unableJobById(jobId, appId);
        return Resp.SUCCESS;
    }

    /**
     * 启动任务
     * @author clock
     * @date 2019/1/11 下午5:27
     * @param jobId 任务ID
     * @param appId 应用ID
     * @return 处理结果
     */
    @RequestMapping(value = "/jobs/{jobId}/enable", method = RequestMethod.POST)
    public Resp<String> enableJob(@PathVariable Long jobId, @RequestHeader String appId) {
        jobApiService.enableJobById(jobId, appId);
        return Resp.SUCCESS;
    }

    /**
     * 根据job id动态生成event
     * @author clock
     * @date 2019-05-28 16:06
     * @param jobId 任务ID
     * @param appId 应用ID
     * @return 处理结果
     */
    @RequestMapping(value = "/jobs/{jobId}/events", method = RequestMethod.POST)
    public Resp<String> generateEvent(@PathVariable Long jobId, @RequestHeader String appId) {
        JobInfo jobInfo = jobService.findJobInfo(jobId);
        if (jobInfo == null) {
            return new Resp<>(0, "invalid job id.");
        }
        if (jobInfo.getEnable() == Constant.NO || StringUtils.isNotEmpty(jobInfo.getParentJobId())) {
            return new Resp<>(0, "invalid job.");
        }
        if (RadishDynamicScheduler.addJobEvent(jobId)) {
            return new Resp<>(1, "add job event success.");
        } else {
            return new Resp<>(0, "add job event failed.");
        }
    }

}
