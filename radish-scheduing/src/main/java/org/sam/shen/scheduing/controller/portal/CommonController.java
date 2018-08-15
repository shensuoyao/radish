package org.sam.shen.scheduing.controller.portal;

import javax.annotation.Resource;

import org.sam.shen.scheduing.mapper.AgentHandlerMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="common")
public class CommonController {

	@Resource
	private AgentHandlerMapper agentHandlerMapper;
	
}
