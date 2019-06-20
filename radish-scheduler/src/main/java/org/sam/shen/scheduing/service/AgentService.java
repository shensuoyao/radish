package org.sam.shen.scheduing.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.scheduing.entity.Agent;
import org.sam.shen.scheduing.entity.AgentGroup;
import org.sam.shen.scheduing.entity.AgentGroupRef;
import org.sam.shen.scheduing.entity.AgentHandler;
import org.sam.shen.scheduing.mapper.*;
import org.sam.shen.scheduing.vo.AgentEditVo;
import org.sam.shen.scheduing.vo.AgentGroupEditView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author suoyao
 * @date 2018年8月13日 上午10:16:16
  * 
 */
@Service("agentService")
public class AgentService {

	@Resource
	private AgentMapper agentMapper;
	
	@Resource
	private AgentGroupMapper agentGroupMapper;
	
	@Resource
	private AgentHandlerMapper agentHandlerMapper;
	
	@Resource
	private AgentGroupRefMapper agentGroupRefMapper;

	@Resource
	private UserAgentGroupMapper userAgentGroupMapper;
	
	/**
	 *  Agent客户端注册
	 * @author suoyao
	 * @date 上午11:08:30
	 * @param agentInfo
	 * @return
	 */
	@Transactional
	public Long registry(AgentInfo agentInfo) {
		Agent agent = agentMapper.findAgentByName(agentInfo.getAgentName());
		if(null == agent) {
			// null的时候为新注册客户端
			agent = new Agent(agentInfo.getAgentName(), agentInfo.getAgentIp(), agentInfo.getAgentPort());
			agent.setNetwork(agentInfo.getNetwork());
			agent.setNettyPort(agentInfo.getNettyPort());
			agentMapper.saveAgent(agent);
			List<AgentHandler> agentHandlerList = Lists.newArrayList();
			for (String handler : agentInfo.getRegistryHandlerMap().keySet()) {
				agentHandlerList
				        .add(new AgentHandler(agent.getId(), handler, agentInfo.getRegistryHandlerMap().get(handler)));
			}
			agentHandlerMapper.saveAgentHandlerBatch(agentHandlerList);
		} else if(agent.getAgentName().equals(agentInfo.getAgentName()) && agent.getAgentIp().equals(agentInfo.getAgentIp()) ) {
			// Agent不为空, 并且客户端的IP相同, 则判断为同一Agent 更新
			agent.setAgentPort(agentInfo.getAgentPort());
            agent.setNetwork(agentInfo.getNetwork());
            agent.setNettyPort(agentInfo.getNettyPort());
			agentMapper.upgradeAgent(agent);
			// 删除所有的 Agent Handler
			agentHandlerMapper.deleteAgentHandler(agent.getId());
			// 新增Agent Handler
			List<AgentHandler> agentHandlerList = Lists.newArrayList();
			for (String handler : agentInfo.getRegistryHandlerMap().keySet()) {
				agentHandlerList
				        .add(new AgentHandler(agent.getId(), handler, agentInfo.getRegistryHandlerMap().get(handler)));
			}
			agentHandlerMapper.saveAgentHandlerBatch(agentHandlerList);
		} else {
			// 客户端名重复
			return -1L;
		}
		return agent.getId();
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
	public Page<Agent> queryAgentForPager(int index, int limit, String agentName, Long userId) {
		PageHelper.startPage(index, limit);
		if (userId == null) {
		    return agentMapper.queryAgentForPager(agentName);
        }
		return agentMapper.queryAgentForPagerUser(agentName, userId);
	}
	
	public List<Agent> queryAgentForList(String agentName, Long userId) {
	    if (userId == null) {
            return agentMapper.queryAgentForList(agentName);
        } else {
	        return agentMapper.queryAgentForPagerUser(agentName, userId);
        }
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
		List<AgentHandler> handlers = Lists.newArrayList();
		if(null != agent) {
			handlers = agentHandlerMapper.queryAgentHandlerByAgentId(agentId);
		} else {
			agent = new Agent();
		}
		return new AgentEditVo(agent, handlers);
	}
	
	/**
	 *  更新Agent客户端信息
	 * @author suoyao
	 * @date 上午11:17:15
	 * @param agent
	 * @param handlers
	 */
	@Transactional
	public void upgradeAgent(Agent agent, List<String> handlers) {
		// 更新Agent admin
		agentMapper.upgradeAgentAdmin(agent);
		// 更新 Agent Handler
		Map<String, Object> param = Maps.newHashMap();
		param.put("enable", 0);
		param.put("agentId", agent.getId());
		// 全部更新成禁用
		agentHandlerMapper.upgradeAgentHandler(param);
		if(null != handlers && handlers.size() > 0) {
			param.put("enable", 1);
			param.put("handlers", handlers);
			// 启用修改的部分
			agentHandlerMapper.upgradeAgentHandler(param);
		}
	}
	
	/**
	 *  查询AgentGroup
	 * @author suoyao
	 * @date 下午4:56:14
	 * @return
	 */
	public List<AgentGroup> queryAgentGroup(Long userId) {
		List<AgentGroup> result;
		if (userId == null) {
		    result = agentGroupMapper.queryAgentGroup();
        } else {
		    result = agentGroupMapper.queryUserAgentGroup(userId);
        }
		if(null == result) {
			return Collections.emptyList();
		}
		return result;
	}

	public List<AgentGroup> queryAgentGroup(String groupName) {
		List<AgentGroup> result = agentGroupMapper.queryAgentGroupByName(groupName);
		if(null == result) {
			return Collections.emptyList();
		}
		return result;
	}
	
	/** 
	 *  保存Agent Group 组
	 * @author suoyao
	 * @date 下午4:50:30
	 * @param agentGroup
	 * @param agents
	 */
	@Transactional
	public void saveAgentGroup(AgentGroup agentGroup, List<Long> agents) {
		if(null == agentGroup.getId()) {
			// 新增
			agentGroupMapper.saveAgentGroup(agentGroup);
		} else {
			// 修改
			agentGroupMapper.upgradeAgentGroup(agentGroup);
			if(null != agents && agents.size() > 0) {
				// 删除原来的关联
				agentGroupRefMapper.deleteAgentGroupRef(agentGroup.getId());
			}
		}
		if(null != agents && agents.size() > 0) {
			List<AgentGroupRef> agentGroupRefList = Lists.newArrayList();
			agents.forEach(agentId -> agentGroupRefList.add(new AgentGroupRef(agentId, agentGroup.getId())));
			agentGroupRefMapper.saveAgentGroupRefBatch(agentGroupRefList);
		}
	}
	
	/**
	 *  客户端组编辑视图
	 * @author suoyao
	 * @date 下午2:03:27
	 * @param agentGroupId
	 * @return
	 */
	@Transactional
	public AgentGroupEditView agentGroupEditView(Long agentGroupId) {
		AgentGroup agentGroup = agentGroupMapper.findAgentGroupById(agentGroupId);
		List<Agent> agents = Lists.newArrayList();
		if(null != agentGroup) {
			agents = agentMapper.queryAgentByAgentGroup(agentGroupId);
		} else {
			agentGroup = new AgentGroup();
		}
		return new AgentGroupEditView(agentGroup, agents);
	}
	
	public List<Agent> queryAgentInIds(List<Long> ids) {
		List<Agent> list = agentMapper.queryAgentInIds(ids);
		if(null == list) {
			list = Collections.emptyList();
		}
		return list;
	}

	@Transactional(rollbackFor = Exception.class)
	public void deleteAgentGroup(Long id) {
		agentGroupMapper.deleteAgentGroup(id);
		userAgentGroupMapper.deleteByGroupId(id);
	}

    /**
     * 根据客户端名称查询客户端和处理器的信息
     * @param name 客户端名称
     * @return 客户端和处理器的列表信息
     */
	public List<Map<String, ?>> queryAgentHandlerByAgentName(String name, Long userId) {
	    if (userId == null) {
            return agentHandlerMapper.queryAgentHandlerByAgentName(name);
        } else {
            return agentHandlerMapper.queryByAgentNameForUser(name, userId);
        }
    }

    /**
     * 根据客户端名称分页查询客户端和处理器的信息
     * @param index 页码
     * @param limit 每页数量
     * @param name 客户端名称
     * @return 客户端和处理器的列表信息
     */
    public List<Map<String, ?>> queryAgentHandlerByAgentNameForPage(int index, int limit, String name, Long userId) {
	    PageHelper.startPage(index, limit);
        if (userId == null) {
            return agentHandlerMapper.queryAgentHandlerByAgentName(name);
        } else {
            return agentHandlerMapper.queryByAgentNameForUser(name, userId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeAgent(Long agentId) {
        // 判断agent是否还连接着
        agentMapper.deleteAgent(agentId);
        agentGroupRefMapper.deleteByAgentId(agentId);
    }

}
