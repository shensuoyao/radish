package org.sam.shen.scheduing.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.JobScheduler;
import org.sam.shen.scheduing.vo.JobSchedulerVo;

import java.util.List;

/**
 * @author clock
 * @date 2019/3/27 下午4:32
 */
@Mapper
public interface JobSchedulerMapper {

    int insert(JobScheduler jobScheduler);

    int changeRunningStatus(JobScheduler jobScheduler);

    Page<JobSchedulerVo> queryJobScheduler(@Param("jobName") String jobName,
                                                    @Param("runningStatus") JobScheduler.RunningStatus status,
                                                    @Param("userId") Long userId,
                                                    @Param("field") String field,
                                                    @Param("order") String order);

    List<JobSchedulerVo> queryAllScheduler();

    List<JobSchedulerVo> querySchedulerByNid(Integer nid);

    int delete(Long jobId);

    int exist(Long jobId);

}
