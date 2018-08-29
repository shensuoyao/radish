package org.sam.shen.scheduing.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.AgentHandler;

@Mapper
public interface AgentHandlerMapper {

	List<AgentHandler> queryAgentHandlerByAgentId(Long agentId);
	
	void saveAgentHandlerBatch(List<AgentHandler> agentHandlerList);
	
	void upgradeAgentHandler(Map<String, Object> param);
	
	List<Map<String, ?>> queryAgentHandlerByAgentName(String agentName);
	
	void deleteAgentHandler(Long agentId);
	
}
