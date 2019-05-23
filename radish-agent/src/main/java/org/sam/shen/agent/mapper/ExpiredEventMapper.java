package org.sam.shen.agent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.agent.entity.ExpiredEvent;

import java.util.List;

/**
 * @author clock
 * @date 2019-05-22 17:08
 */
@Mapper
public interface ExpiredEventMapper {

    List<ExpiredEvent> selectExpiredEvent();

    void deleteExpiredEvent(@Param("events") List<String> events);

}
