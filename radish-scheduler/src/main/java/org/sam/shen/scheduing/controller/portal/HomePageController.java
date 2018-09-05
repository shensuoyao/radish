package org.sam.shen.scheduing.controller.portal;

import org.quartz.SchedulerException;
import org.sam.shen.scheduing.service.DashboardService;
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

	@RequestMapping(value = {"", "/", "/index", "/home"}, method = RequestMethod.GET)
	public ModelAndView home(ModelAndView model) {
		model.setViewName("home");
		return model;
	}
	
	@RequestMapping(value = "dashboard", method = RequestMethod.GET)
	public ModelAndView dashboard(ModelAndView model) {
		model.addObject("agentGroupCount", dashboardService.countAgentGroup());
		model.addObject("agentOnlineCount", dashboardService.countAgent(1));
		model.addObject("agentOfflineCount", dashboardService.countAgent(0));
		
		model.setViewName("frame/dashboard");
		return model;
	}
	
	@RequestMapping(value = "/dashboard/event-chart", method = RequestMethod.GET)
	@ResponseBody
	public ChartVo eventChart() {
		return dashboardService.eventChart();
	}
	
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
