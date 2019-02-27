package org.sam.shen.scheduing.service;

import java.util.HashMap;

import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.sam.shen.core.constants.EventStatus;
import org.sam.shen.scheduing.mapper.AgentGroupMapper;
import org.sam.shen.scheduing.mapper.AgentMapper;
import org.sam.shen.scheduing.mapper.JobEventMapper;
import org.sam.shen.scheduing.mapper.JobInfoMapper;
import org.sam.shen.scheduing.scheduler.RadishDynamicScheduler;
import org.sam.shen.scheduing.vo.ChartVo;
import org.springframework.stereotype.Service;

@Service("dashboardService")
public class DashboardService {

	@Resource
	private AgentGroupMapper agentGroupMapper;
	
	@Resource
	private AgentMapper agentMapper;
	
	@Resource
	private JobInfoMapper jobInfoMapper;
	
	@Resource
	private JobEventMapper jobEventMapper;
	
	public Long countAgentGroup() {
		Long count = agentGroupMapper.countAgentGroup();
		return null == count ? 0 : count;
	}
	
	public Integer countAgent() {
		Integer count = agentMapper.countAgent();
		return null == count ? 0 : count;
	}
	
	public Integer countJobInfo(int enable, Long userId) {
		return jobInfoMapper.countJobInfoByEnable(enable, userId);
	}
	
	public ChartVo eventChart(Long userId) {
		ChartVo chartVo = new ChartVo();
		chartVo.addLegend("Event");
		for (EventStatus item : EventStatus.values()) {
			chartVo.addXAxis(item.name());
			chartVo.addYAxis(jobEventMapper.countJobEventByStat(item.name(), userId));
		}
		return chartVo;
	}
	
	@SuppressWarnings("serial")
	public ChartVo jobChart(Long userId) throws SchedulerException {
		ChartVo chartVo = new ChartVo();
		chartVo.addLegend("启用");
		chartVo.addLegend("禁用");
		chartVo.addLegend("调度中");

		chartVo.addYAxis(new HashMap<String, Object>() {
			{
				put("name", "启用");
				put("value", jobInfoMapper.countJobInfoByEnable(1, userId));
			}
		});
		chartVo.addYAxis(new HashMap<String, Object>() {
			{
				put("name", "禁用");
				put("value", jobInfoMapper.countJobInfoByEnable(0, userId));
			}
		});
		chartVo.addYAxis(new HashMap<String, Object>() {
			{
				put("name", "调度中");
				put("value", RadishDynamicScheduler.listJobsInScheduler(userId).size());
			}
		});
		return chartVo;
	}
	
}
