package org.sam.shen.scheduing.controller.portal;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.AgentGroup;
import org.sam.shen.scheduing.entity.RespPager;
import org.sam.shen.scheduing.service.AgentService;
import org.sam.shen.scheduing.vo.AgentEditVo;
import org.sam.shen.scheduing.vo.AgentGroupEditView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.github.pagehelper.Page;

/**
 * @author suoyao
 * @date 2018年8月10日 上午11:22:38
  * 
 */
@Controller
@RequestMapping(value="portal")
public class AgentController {
	
	@Autowired
	private AgentService agentService;
	
	/**
	 * @author suoyao
	 * @date 上午11:22:35
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "agent", method = RequestMethod.GET)
	public ModelAndView toAgentPage(ModelAndView model) {
		model.setViewName("frame/agent/agent");
		return model;
	}
	
	/**
	 *  Agent 查询分页 JSON数据集合
	 * @author suoyao
	 * @date 上午11:22:12
	 * @param page
	 * @param limit
	 * @param agentName
	 * @return
	 */
	@RequestMapping(value = "agent/json-pager", method = RequestMethod.GET)
	@ResponseBody
	public RespPager<Page<Agent>> queryAgentForJsonPager(@RequestParam("page") Integer page,
	        @RequestParam("limit") Integer limit,
	        @RequestParam(value = "agentName", required = false, defaultValue = "") String agentName) {
		if(null == page) {
			page = 1;
		}
		if(null == limit) {
			limit = 10;
		}
		Page<Agent> pager = agentService.queryAgentForPager(page, limit, agentName);
		return new RespPager<>(pager.getPageSize(), pager.getTotal(), pager);
	}
	
	/**
	 *  不分页的数据查询
	 * @author suoyao
	 * @date 下午3:45:35
	 * @param agentName
	 * @return
	 */
	@RequestMapping(value = "agent/json", method = RequestMethod.GET)
	@ResponseBody
	public Resp<List<Agent>> queryAgentForJson(
	        @RequestParam(value = "agentName", required = false, defaultValue = "") String agentName) {
		if (StringUtils.isEmpty(agentName)) {
			return new Resp<>(Collections.emptyList());
		}
		List<Agent> list = agentService.queryAgentForList(agentName);
		if(null == list) {
			list = Collections.emptyList();
		}
		return new Resp<>(list);
	} 
	
	/**
	 * Agent 编辑
	 * @author suoyao
	 * @date 上午11:21:39
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value = {"agent-edit/", "agent-edit/{id}"}, method = RequestMethod.GET)
	public ModelAndView agentEdit(ModelAndView model, @PathVariable(value = "id", required = false) Long id) {
		if(null != id) {
			model.addObject("view", agentService.agentEditView(id));
		} else {
			model.addObject("view", new AgentEditVo(new Agent(), Collections.emptyList()));
		}
		model.setViewName("frame/agent/agent_edit");
		return model;
	}
	
	/**
	 *  修改Agent客户端
	 * @author suoyao
	 * @date 下午4:32:22
	 * @param model
	 * @param agent
	 * @param handlers
	 * @return
	 */
	@RequestMapping(value = "agent-edit-save", method = RequestMethod.POST)
	public String agentEditSave(ModelAndView model, @ModelAttribute Agent agent, 
	        @RequestParam(value = "handlers", required = false) List<String> handlers) {
		agentService.upgradeAgent(agent, handlers);
		return "redirect:/portal/agent-edit/" + agent.getId();
	}

	@RequestMapping(value = "agent-group", method = RequestMethod.GET)
	public ModelAndView toAgentGroupPage(ModelAndView model) {
		model.setViewName("frame/agent/agent_group");
		return model;
	}
	
	/**
	 *  查询AgentGroup Json数据集
	 * @author suoyao
	 * @date 下午4:58:24
	 * @return
	 */
	@RequestMapping(value = "agent-group/json", method = RequestMethod.GET)
	@ResponseBody
	public Resp<List<AgentGroup>> queryAgentGroupForJson() {
		return new Resp<>(agentService.queryAgentGroup());
	}

    /**
     * 提供页面选择group使用
     * @author clock
     * @date 2019/2/26 下午3:36
     * @return 客户端组
     */
    @ResponseBody
    @RequestMapping(value = "agent-group-select", method = RequestMethod.GET)
    public List<Map<String, Object>> queryAgentGroup(@RequestParam(required = false) String groupName) {
	    List<AgentGroup> groups = agentService.queryAgentGroup(groupName);
	    return groups.stream().map(group -> {
	        Map<String, Object> map = new HashMap<>();
	        map.put("name", Long.toString(group.getId()).concat("-").concat(group.getGroupName()));
	        map.put("value", group.getId());
	        return map;
        }).collect(Collectors.toList());
    }
	
	/**
	 *  跳转到AgentGroup添加页面
	 * @author suoyao
	 * @date 下午4:33:16
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "agent-group-add", method = RequestMethod.GET)
	public ModelAndView agentGroupAdd(ModelAndView model) {
		model.setViewName("frame/agent/agent_group_add");
		return model;
	} 
	
	/**
	 *   编辑Agent Group
	 * @author suoyao
	 * @date 下午2:09:40
	 * @param model
	 * @param id
	 * @return
	 */
	@RequestMapping(value = { "agent-group-edit/", "agent-group-edit/{id}" }, method = RequestMethod.GET)
	public ModelAndView agentGroupEdit(ModelAndView model,
	        @PathVariable(value = "id", required = false) Long id) {
		if(null != id) {
			model.addObject("view", agentService.agentGroupEditView(id));
		} else {
			model.addObject("view", new AgentGroupEditView(new AgentGroup(), Collections.emptyList()));
		}
		model.setViewName("frame/agent/agent_group_edit");
		return model;
	}
	
	/**
	 *  保存Agent 机器组
	 * @author suoyao
	 * @date 上午10:05:35
	 * @param agentGroup
	 * @param agents
	 * @return
	 */
	@RequestMapping(value = "agent-group-save", method = RequestMethod.POST)
	public String agentGroupSave(@ModelAttribute AgentGroup agentGroup,
	        @RequestParam("agents") List<Long> agents) {
		agentGroup.setCreateTime(new Date());
		agentService.saveAgentGroup(agentGroup, agents);
		return "redirect:/portal/agent-group-edit/" + agentGroup.getId();
	}
	
	@RequestMapping(value = "agent-group-del", method = RequestMethod.POST)
	@ResponseBody
	public Resp<String> agentGroupDel(@RequestParam(value = "id", required = false) Long id) {
		if(null == id) {
			return Resp.FAIL;
		}
		agentService.deleteAgentGroup(id);
		return Resp.SUCCESS;
	}
	
}
