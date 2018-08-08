package org.sam.shen.scheduing.controller.portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value="portal")
public class AgentController {

	@RequestMapping(value = "agent-group", method = RequestMethod.GET)
	public ModelAndView queryAgentGroup(ModelAndView model) {
		
		model.setViewName("frame/agent/agent_group");
		return model;
	}
	
}
