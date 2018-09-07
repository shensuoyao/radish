package org.sam.shen.scheduing.controller.portal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.model.AgentPerformance;
import org.sam.shen.scheduing.service.RedisService;
import org.sam.shen.scheduing.vo.DynamicChartNodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller
@RequestMapping(value = "monitor")
public class MonitorController {

	@Autowired
	private RedisService redisService;
	
	/**
	 *   获取在线的Agent
	 * @author suoyao
	 * @date 下午3:15:52
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "agent-online", method = RequestMethod.GET)
	public ModelAndView agentOnline(ModelAndView model) {
		List<Map<String, Object>> agentOnlines = Lists.newArrayList();
		Set<String> keys = redisService.getKeys(Constant.REDIS_AGENT_PREFIX.concat("*"));
		if(null != keys && keys.size() > 0) {
			keys.forEach(key -> {
				Map<String, Object> hash = redisService.hmget(key);
				agentOnlines.add(hash);
			});
		}
		model.addObject("agentOnlines", agentOnlines);
		model.setViewName("frame/monitor/agent_online");
		return model;
	}
	
	@RequestMapping(value = "agent-online-monitor/{agentId}", method = RequestMethod.GET)
	public ModelAndView agentOnlineMonitor(ModelAndView model, @PathVariable("agentId") Long agentId) {
		Map<String, Object> hash = redisService.hmget(Constant.REDIS_AGENT_PREFIX + agentId);
		AgentPerformance performance = JSON.parseObject(JSON.toJSONString(hash), AgentPerformance.class);
		model.addObject("performance", performance);
		model.setViewName("frame/monitor/agent_online_monitor");
		return model;
	}
	
	@RequestMapping(value = "agent-online-monitor-dynamic/{agentId}", method = RequestMethod.GET)
	@ResponseBody
	public DynamicChartNodeVo agentOnlineMonitorDynamic(@PathVariable("agentId") Long agentId) {
		Map<String, Object> hash = redisService.hmget(Constant.REDIS_AGENT_PREFIX + agentId);
		if(null == hash) {
			return new DynamicChartNodeVo();
		}
		AgentPerformance performance = JSON.parseObject(JSON.toJSONString(hash), AgentPerformance.class);
		
		String t = new DateTime().toString("HH:mm:ss");
		DynamicChartNodeVo dynamicChartVo = new DynamicChartNodeVo(t);
		
		Map<String, Object> yAxis = Maps.newHashMap();
		
		double d1 = (performance.getJvmTotalMemory() - performance.getJvmFreeMemory()) / (double)performance.getJvmTotalMemory();
		BigDecimal b1 = new BigDecimal(d1);
		yAxis.put("JVM", b1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		
		double d2 = (performance.getPhysicalTotalMemory() - performance.getPhysicalFreeMemory()) / (double)performance.getPhysicalTotalMemory();
		BigDecimal b2 = new BigDecimal(d2);
		yAxis.put("Physical", b2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		
		dynamicChartVo.setyAxis(yAxis);
		
		return dynamicChartVo;
	}
	
}
