package org.sam.shen.monitor.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.monitor.entity.Notifier;

import java.util.Map;

/**
 * @author clock
 * @date 2019-05-17 14:04
 */
@Mapper
public interface NotifierMapper {

    Notifier selectFromAgent(String agentId);

    Notifier selectFromJob(String eventId);

    Map<String, Object> selectAgentByEventId(String eventId);

}
