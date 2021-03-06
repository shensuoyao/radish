package org.sam.shen.scheduing.controller.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.quartz.SchedulerException;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.scheduing.constants.SchedConstant;
import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.service.AgentService;
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

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value="portal")
public class HomePageController {
	
	private Logger logger = LoggerFactory.getLogger(HomePageController.class);
	
	@Autowired
	private DashboardService dashboardService;

	@Autowired
    private AgentService agentService;
	
	@Autowired
	private RedisService redisService;

	@RequestMapping(value = {"", "/", "/index", "/home"}, method = RequestMethod.GET)
	public ModelAndView home(ModelAndView model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        model.addObject("user", user);
		model.setViewName("home");
		return model;
	}
	
	@RequestMapping(value = "dashboard", method = RequestMethod.GET)
	public ModelAndView dashboard(ModelAndView model, HttpSession session) {
	    User user = (User) session.getAttribute("user");
	    if (SchedConstant.ADMINISTRATOR.equals(user.getUname())) {
	        user.setId(null);
        }
		model.addObject("agentGroupCount", dashboardService.countAgentGroup(user.getId()));
		
		Integer agentTotalCount = dashboardService.countAgent(user.getId());
		if(null == agentTotalCount) {
			agentTotalCount = 0;
		}
		// Online Agent size
		Set<String> keys = redisService.getKeys(Constant.REDIS_AGENT_PREFIX + "*");
		int agentOnlineCount = 0;
		if(null != keys) {
            if (user.getId() == null) {
                agentOnlineCount = keys.size();
            } else {
                List<String> aids = new ArrayList<>();
                for (String key : keys) {
                    aids.add(key.split("_")[1]);
                }
                List<Agent> agents = agentService.queryAgentForList(null, user.getId());
                agentOnlineCount = agents.stream().filter(a -> aids.contains(Long.toString(a.getId()))).collect(Collectors.toList()).size();
            }
		}
		model.addObject("agentOnlineCount", agentOnlineCount);
		model.addObject("agentOfflineCount", agentTotalCount - agentOnlineCount);
		
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
	public ChartVo eventChart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (SchedConstant.ADMINISTRATOR.equals(user.getUname())){ // 如果管理员登陆查询所有数据
            user.setId(null);
        }
		return dashboardService.eventChart(user.getId());
	}
	
	/**
	 *  任务图表
	 * @author suoyao
	 * @date 下午2:57:19
	 * @return
	 */
	@RequestMapping(value = "/dashboard/job-chart", method = RequestMethod.GET)
	@ResponseBody
	public ChartVo jobChart(HttpSession session) {
		try {
		    User user = (User) session.getAttribute("user");
            if (SchedConstant.ADMINISTRATOR.equals(user.getUname())){ // 如果管理员登陆查询所有数据
                user.setId(null);
            }
			return dashboardService.jobChart(user.getId());
		} catch (SchedulerException e) {
			logger.error("job chart fail.", e);
		}
		return new ChartVo();
	}
	
}
