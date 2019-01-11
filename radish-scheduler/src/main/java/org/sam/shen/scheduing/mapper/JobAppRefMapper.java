package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.JobAppRef;

import java.util.List;

/**
 * @author clock
 * @date 2019/1/8 下午1:48
 */
@Mapper
public interface JobAppRefMapper {

    JobAppRef selectJobAppRefById(String id);

    int insertJobAppRef(JobAppRef jobAppRef);

    int updateJobAppRef(JobAppRef jobAppRef);

    int batchInsert(List<JobAppRef> list);

    int deleteJobAppRefById(String id);

    int deleteJobAppRefByJobId(String jobId);
}
