package org.sam.shen.scheduing.controller.core;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.sam.shen.core.annotations.RadishLog;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.constants.MonitorType;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.core.model.AgentMonitorInfo;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.JobInfo;
import org.sam.shen.scheduing.service.AgentService;
import org.sam.shen.scheduing.service.JobEventService;
import org.sam.shen.scheduing.service.JobService;
import org.sam.shen.scheduing.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;

/**
 * @author suoyao
 * @date 2018年7月31日 下午3:23:54 Agent core APIs
 */
@Slf4j
@RestController
@RequestMapping(value = "/core")
public class CoreController {

	@Autowired
	private AgentService agentService;
	
	@Autowired
	private JobEventService jobEventService;

	@Autowired
    private JobService jobService;
	
	@Autowired
	private RedisService redisService;

	/**
     * Agent registry
	 * @author suoyao
	 * @date 下午12:58:09
	 * @param agentInfo agent information
	 * @return agent id or failure information
	 */
	@RequestMapping(value = "/registry", method = RequestMethod.PUT)
	public Resp<Long> registry(@RequestBody AgentInfo agentInfo) {
		if (log.isInfoEnabled()) {
			log.info("Agent Registry : {}", agentInfo.toString());
		}
		Long agentId = agentService.registry(agentInfo);
		if (agentId > 0) {
			return new Resp<>(agentId);
		}
		return new Resp<>(0, "Registry Fail", -1L);
	}

	/**
     * Agent heartbeat call
	 * @author suoyao
	 * @date 下午3:24:44
	 * @return agent monitoring information
	 */
	@RadishLog(monitorType = MonitorType.HEARTBEAT)
	@RequestMapping(value = "/heartbeat", method = RequestMethod.POST)
	public Resp<AgentMonitorInfo> heartbeat(@RequestBody AgentMonitorInfo agent) {
		@SuppressWarnings("unchecked")
		Map<String,  Object> m = JSON.parseObject(agent.toString(), Map.class);
		redisService.hmsetEx(Constant.REDIS_AGENT_PREFIX + agent.getAgentId(), m, 60);
		return new Resp<>(agent);
	}

	/**
     * Preempt event
	 * @author suoyao
	 * @date 下午5:57:48
	 * @param agentId agent id
	 * @return preemptive event
	 */
	@RequestMapping(value = "/trigger-event/{agentId}", method = RequestMethod.GET)
	public Resp<HandlerEvent> triggerEvent(@PathVariable(value = "agentId", required = false) Long agentId) {
		// 根据Agent机器性能决定是否能抢到任务
		HandlerEvent event = jobEventService.triggerJobEvent(agentId);
		return new Resp<>(event);
	}
	
	/**
	 * Handle report that agent execute event
	 * @author suoyao
	 * @date 下午4:57:50
	 * @param event job event
	 * @return handle result
	 */
	@RadishLog(monitorType = MonitorType.EVENT)
	@RequestMapping(value = "/handler-event-report", method = RequestMethod.POST)
	public Resp<String> handlerEventReport(@RequestBody HandlerEvent event) {
		jobEventService.handlerJobEventReport(event);
		return Resp.SUCCESS;
	}

    /**
     * Handle child event
     * @author clock
     * @date 2018/12/4 下午3:13
     * @param event handler event
     * @return handle result
     */
	@RequestMapping(value = "/handle-child-event", method = RequestMethod.POST)
	public Resp<String> handleChildEvent(@RequestBody HandlerEvent event) {
        List<JobInfo> jobInfo = jobService.getChildJobByParentJobId(event.getJobId());
        if (jobInfo == null || jobInfo.size() == 0) {
            return new Resp<>(Resp.FAIL.getCode(), "have no child job");
        }
        jobEventService.addChildJobEvent(event);
        return Resp.SUCCESS;
    }

}
