package org.sam.shen.monitor.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.monitor.entity.Notifier;

/**
 * @author clock
 * @date 2019-05-17 14:04
 */
@Mapper
public interface NotifierMapper {

    Notifier selectFromAgent(String agentId);

    Notifier selectFromJob(String eventId);

}
