package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.JobEvent;

import com.github.pagehelper.Page;

import java.util.List;

@Mapper
public interface JobEventMapper {

	void saveJobEvent(JobEvent event);
	
	void upgradeJobEvent(JobEvent event);
	
	JobEvent findJobEventByEventId(String eventId);
	
	Page<JobEvent> queryJobEventForPager(@Param("stat") String stat);
	
	// ------------  统计 -----------------------------------
	Integer countJobEventByStat(String stat);

	List<JobEvent> queryJobEventByJobId(Long jobId);

	List<JobEvent> queryChildJobEvent(String events);

	int rehandleFailedEvent(String eventId);
}
