package org.sam.shen.monitor.service;

import org.sam.shen.monitor.entity.Notifier;
import org.sam.shen.monitor.mapper.NotifierMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


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


    public static Notifier getNotifierOfAgent(String agentId) {
        return notifierMapper.selectFromAgent(agentId);
    }

    public static Notifier getNotifierOfJob(String eventId) {
        return notifierMapper.selectFromJob(eventId);
    }

}
