package org.sam.shen.agent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.agent.entity.ExpiredEvent;

import java.util.List;
import java.util.Map;

/**
 * @author clock
 * @date 2019-05-22 17:08
 */
@Mapper
public interface JobEventMapper {

    List<ExpiredEvent> selectExpiredEvent();

    void deleteExpiredEvent(@Param("events") List<String> events);

    List<Map<String, Object>> selectHandledEvent();

    void deleteHandledEvent();

    void batchInsertEvent(@Param("events") List<Map<String, Object>> events);
}
