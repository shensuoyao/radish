package org.sam.shen.scheduing.controller.portal;

import org.sam.shen.scheduing.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value="portal")
public class HomePageController {
	
	@Autowired
	private AgentService agentService;

	@RequestMapping(value = {"", "/", "/index", "/home"}, method = RequestMethod.GET)
	public ModelAndView home(ModelAndView model) {
		model.setViewName("home");
		return model;
	}
	
	@RequestMapping(value = "dashboard", method = RequestMethod.GET)
	public ModelAndView dashboard(ModelAndView model) {
		model.addObject("agentGroupCount", agentService.countAgentGroup());
		model.addObject("agentOnlineCount", agentService.countAgent(1));
		model.addObject("agentOfflineCount", agentService.countAgent(0));
		model.setViewName("frame/dashboard");
		return model;
	}
	
}
