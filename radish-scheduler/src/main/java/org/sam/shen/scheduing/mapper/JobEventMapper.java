package org.sam.shen.scheduing.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.JobEvent;

import com.github.pagehelper.Page;

@Mapper
public interface JobEventMapper {

	void saveJobEventBatch(List<JobEvent> events);
	
	List<JobEvent> queryJobEventByAgentId(Long agentId);
	
	Integer countJobEventInJobIds(List<Long> ids);
	
	void deleteJobEventNotEqual(Map<String, Object> param);
	
	void upgradeJobEventStatus(Map<String, Object> param);
	
	List<JobEvent> queryJobEventByEventId(String eventId);
	
	Page<JobEvent> queryJobEventForPager(@Param("stat") String stat);
	
	// ------------  统计 -----------------------------------
	Integer countJobEventByStat(String stat);
	
}
