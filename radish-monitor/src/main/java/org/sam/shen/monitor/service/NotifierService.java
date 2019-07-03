package org.sam.shen.monitor.service;

import org.sam.shen.monitor.entity.Notifier;
import org.sam.shen.monitor.mapper.NotifierMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;


/**
 * @author clock
 * @date 2019-05-17 14:43
 */
@Component
public class NotifierService implements ApplicationContextAware {

    private static NotifierMapper notifierMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        notifierMapper = applicationContext.getBean(NotifierMapper.class);
    }

    /**
     * 查询客户端通知人
     * @author clock
     * @date 2019-05-21 14:56
     * @param agentId 客户端ID
     * @return 告警人信息
     */
    public static Notifier getNotifierOfAgent(String agentId) {
        return notifierMapper.selectFromAgent(agentId);
    }

    /**
     * 查询任务通知人
     * @author clock
     * @date 2019-05-21 14:56
     * @param eventId 事件ID
     * @return 告警人信息
     */
    public static Notifier getNotifierOfJob(String eventId) {
        return notifierMapper.selectFromJob(eventId);
    }

    /**
     * 查询执行事件的客户端信息
     * @author clock
     * @date 2019-07-02 17:47
     * @param eventId 事件ID
     * @return 客户端信息
     */
    public static Map<String, Object> selectAgentByEventId(String eventId) {
        return notifierMapper.selectAgentByEventId(eventId);
    }

}
