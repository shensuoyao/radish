package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.JobEvent;

import com.github.pagehelper.Page;
import org.sam.shen.scheduing.vo.JobEventVo;

import java.util.List;

@Mapper
public interface JobEventMapper {

	void saveJobEvent(JobEvent event);
	
	void upgradeJobEvent(JobEvent event);
	
	JobEvent findJobEventByEventId(String eventId);
	
	Page<JobEvent> queryJobEventForPager(@Param("stat") String stat, @Param("userId") Long userId);
	
	// ------------  统计 -----------------------------------
	Integer countJobEventByStat(@Param("stat") String stat, @Param("userId") Long userId);

	List<JobEvent> queryJobEventByJobId(Long jobId);

	List<JobEventVo> queryChildJobEventVo(@Param("events") String events, @Param("groups") String groups);

    List<JobEventVo> findJobEventVoById(@Param("eventId") String eventId, @Param("groupId") String groupId);

	int rehandleFailedEvent(String eventId);

	int updateEventPriority(JobEvent jobEvent);

	int batchInsert(List<JobEvent> list);

	int updateChildEventStatus(@Param("stat") String stat, @Param("pid") String pid);

    int activateChildEventByGroupId(String groupId);

	List<JobEvent> findSubeventsByPid(String pid);

    List<JobEvent> findSubeventsByParentGroupId(String pid);
	
	/**
	 * 判断该事件组的所有事件是否完成
	 * @author clock
	 * @date 2018/12/29 上午9:51
	 * @param groupId 事件组ID
	 * @return 事件组的
	 */
	int checkGroupComplete(String groupId);

	List<JobEvent> findJobEventByGroupId(String groupId);
}
