package org.sam.shen.scheduing.controller.portal;

import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.AgentGroup;
import org.sam.shen.scheduing.entity.RespPager;
import org.sam.shen.scheduing.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.Page;

@Controller
@RequestMapping(value="portal")
public class AgentController {
	
	@Autowired
	private AgentService agentService;
	
	@RequestMapping(value = "agent", method = RequestMethod.GET)
	public ModelAndView toAgentPage(ModelAndView model) {
		
		model.setViewName("frame/agent/agent");
		return model;
	}
	
	@RequestMapping(value = "agent/json", method = RequestMethod.GET)
	@ResponseBody
	public RespPager<Page<Agent>> queryAgentForJson(@RequestParam("page") Integer page,
	        @RequestParam("limit") Integer limit) {
		Page<Agent> pager = agentService.queryAgentForPager(page, limit);
		return new RespPager<>(pager);
	}

	@RequestMapping(value = "agent-group", method = RequestMethod.GET)
	public ModelAndView queryAgentGroup(ModelAndView model) {
		
		model.setViewName("frame/agent/agent_group");
		return model;
	}
	
	@RequestMapping(value = "agent-group-add", method = RequestMethod.GET)
	public ModelAndView agentGroupAdd(ModelAndView model) {
		
		model.setViewName("frame/agent/agent_group_add");
		return model;
	} 
	
	@RequestMapping(value = "agent-group-save", method = RequestMethod.POST)
	public ModelAndView agentGroupSave(ModelAndView model, AgentGroup agentGroup) {
		
		return null;
	}
	
}
