package org.sam.shen.scheduing.service;

import java.util.List;

import javax.annotation.Resource;

import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.AgentHandler;
import org.sam.shen.scheduing.mapper.AgentGroupMapper;
import org.sam.shen.scheduing.mapper.AgentHandlerMapper;
import org.sam.shen.scheduing.mapper.AgentMapper;
import org.sam.shen.scheduing.vo.AgentEditVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;

@Service("agentService")
public class AgentService {

	@Resource
	private AgentMapper agentMapper;
	
	@Resource
	private AgentGroupMapper agentGroupMapper;
	
	@Resource
	private AgentHandlerMapper agentHandlerMapper;
	
	/**
	 *  Agent客户端注册
	 * @author suoyao
	 * @date 上午11:08:30
	 * @param agentInfo
	 * @return
	 */
	@Transactional
	public Boolean registry(AgentInfo agentInfo) {
		Agent agent = agentMapper.findAgentByName(agentInfo.getAgentName());
		if(null != agent && agent.getAgentName().equals(agentInfo.getAgentName())) {
			return Boolean.FALSE;
		}
		if(null == agent) {
			agent = new Agent(agentInfo.getAgentName(), agentInfo.getAgentIp(), agentInfo.getAgentPort());
			agentMapper.saveAgent(agent);
			List<AgentHandler> agentHandlerList = Lists.newArrayList();
			for (String handler : agentInfo.getRegistryHandlerMap().keySet()) {
				agentHandlerList
				        .add(new AgentHandler(agent.getId(), handler, agentInfo.getRegistryHandlerMap().get(handler)));
			}
			agentHandlerMapper.saveAgentHandlerBatch(agentHandlerList);
		}
		return Boolean.TRUE;
	}
	
	/**
	 *  根据AgentName查询Agent集合并分页
	 * @author suoyao
	 * @date 上午11:08:01
	 * @param index
	 * @param limit
	 * @param agentName
	 * @return
	 */
	public Page<Agent> queryAgentForPager(int index, int limit, String agentName) {
		PageHelper.startPage(index, limit);
		return agentMapper.queryAgentForPager(agentName);
	}
	
	/**
	 *  Agent 客户端编辑视图业务
	 * @author suoyao
	 * @date 上午11:20:30
	 * @param agentId
	 * @return
	 */
	public AgentEditVo agentEditView(Long agentId) {
		Agent agent = agentMapper.findAgentById(agentId);
		List<AgentHandler> handlers = agentHandlerMapper.queryAgentHandlerByAgentId(agentId);
		return new AgentEditVo(agent, handlers);
	}
	
}
