package org.sam.shen.scheduing.controller.portal;

import java.util.Set;

import org.quartz.SchedulerException;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.scheduing.service.DashboardService;
import org.sam.shen.scheduing.service.RedisService;
import org.sam.shen.scheduing.vo.ChartVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value="portal")
public class HomePageController {
	
	private Logger logger = LoggerFactory.getLogger(HomePageController.class);
	
	@Autowired
	private DashboardService dashboardService;
	
	@Autowired
	private RedisService redisService;

	@RequestMapping(value = {"", "/", "/index", "/home"}, method = RequestMethod.GET)
	public ModelAndView home(ModelAndView model) {
		model.setViewName("home");
		return model;
	}
	
	@RequestMapping(value = "dashboard", method = RequestMethod.GET)
	public ModelAndView dashboard(ModelAndView model) {
		model.addObject("agentGroupCount", dashboardService.countAgentGroup());
		
		Integer agentTotalCount = dashboardService.countAgent();
		if(null == agentTotalCount) {
			agentTotalCount = 0;
		}
		// Online Agent size
		Set<String> keys = redisService.getKeys(Constant.REDIS_AGENT_PREFIX + "*");
		int agentOnlineCount = 0;
		if(null != keys) {
			agentOnlineCount = keys.size();
		}
		model.addObject("agentOnlineCount", agentOnlineCount);
		model.addObject("agentOfflineCount", agentTotalCount.intValue() - agentOnlineCount);
		
		model.setViewName("frame/dashboard");
		return model;
	}
	
	/**
	 *  事件图表
	 * @author suoyao
	 * @date 下午2:57:04
	 * @return
	 */
	@RequestMapping(value = "/dashboard/event-chart", method = RequestMethod.GET)
	@ResponseBody
	public ChartVo eventChart() {
		return dashboardService.eventChart();
	}
	
	/**
	 *  任务图表
	 * @author suoyao
	 * @date 下午2:57:19
	 * @return
	 */
	@RequestMapping(value = "/dashboard/job-chart", method = RequestMethod.GET)
	@ResponseBody
	public ChartVo jobChart() {
		try {
			return dashboardService.jobChart();
		} catch (SchedulerException e) {
			logger.error("job chart fail.", e);
		}
		return new ChartVo();
	}
	
}
