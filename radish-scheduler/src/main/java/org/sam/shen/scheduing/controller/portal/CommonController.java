package org.sam.shen.scheduing.controller.portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.scheduing.constants.SchedConstant;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value="common")
public class CommonController {

	@Autowired
	private AgentService agentService;
	
	/**
	 *  获取Agent和Handler处理器的分组选择
	 * @author suoyao
	 * @date 上午9:11:37
	 * @param agentName 客户端名称
	 * @return
	 */
	@SuppressWarnings("serial")
	@RequestMapping(value = "agent-handler-group", method = RequestMethod.GET)
	public List<Map<String, Object>> getAgentHandlerListForGroup(@RequestParam("agentName") String agentName, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (SchedConstant.ADMINISTRATOR.equals(user.getUname())) {
            user.setId(null);
        }
		List<Map<String, ?>> list;
		if (StringUtils.isEmpty(agentName)) {
            list = agentService.queryAgentHandlerByAgentNameForPage(1, 10, agentName, user.getId());
        } else {
            list = agentService.queryAgentHandlerByAgentName(agentName, user.getId());
        }
		List<Map<String, Object>> result = Lists.newArrayList();
		if(null != list && list.size() > 0) {
			long agentId = -1L;
			for(Map<String, ?> m : list) {
				long aId = Long.valueOf(m.get("agentId").toString());
				if(aId != agentId) {
					result.add(new HashMap<String, Object>() {
						{
							put("name", m.get("agentName"));
							put("type", "optgroup");
						}
					});
					agentId = aId;
				}
				result.add(new HashMap<String, Object>() {
					{
						put("name", m.get("handler"));
						put("value", String.valueOf(aId).concat("-").concat(String.valueOf(m.get("handler"))));
					}
				});
			}
		}
		return result;
	}

    /**
     * 提供给app模块的Agent和Handler选择
     * @author clock
     * @date 2019/3/12 下午1:34
     * @param agentName 客户端名称
     * @param session session
     * @return Agent和Handler列表
     */
    @RequestMapping(value = "agent-handler-group-app", method = RequestMethod.GET)
    public List<Map<String, Object>> getAgentHandlerListForGroupApp(@RequestParam("agentName") String agentName, HttpSession session) {
	    User user = (User) session.getAttribute("user");
        if (SchedConstant.ADMINISTRATOR.equals(user.getUname())) {
            user.setId(null);
        }
        List<Map<String, ?>> list;
        if (StringUtils.isEmpty(agentName)) {
            list = agentService.queryAgentHandlerByAgentNameForPage(1, 10, agentName, user.getId());
        } else {
            list = agentService.queryAgentHandlerByAgentName(agentName, user.getId());
        }
        List<Map<String, Object>> result = Lists.newArrayList();
        if(null != list && list.size() > 0) {
            long agentId = -1L;
            for(Map<String, ?> m : list) {
                long aId = Long.valueOf(m.get("agentId").toString());
                if(aId != agentId) {
                    result.add(new HashMap<String, Object>() {
                        private static final long serialVersionUID = -1327560835402916072L;
                        {
                            put("name", m.get("agentName"));
                            put("type", "optgroup");
                        }
                    });
                    agentId = aId;
                }
                result.add(new HashMap<String, Object>() {
                    private static final long serialVersionUID = -393533457698464686L;
                    {
                        put("name", m.get("handler"));
                        put("value", String.valueOf(m.get("id")));
                    }
                });
            }
        }
        return result;
    }
	
}
