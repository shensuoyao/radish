package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.UserJob;

/**
 * @author clock
 * @date 2019/2/25 上午11:02
 */
@Mapper
public interface UserJobMapper {

    int insert(UserJob userJob);

    int deleteByJobId(String jobId);

}
