package org.sam.shen.agent.handler;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.agent.entity.ExpiredEvent;
import org.sam.shen.agent.service.JobEventService;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.AbsHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.model.Resp;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author clock
 * @date 2019-05-22 16:46
 */
@Component
@AHandler(name = "expiredEventHandler", description = "检查event是否超时未执行，并对移除超时event")
public class ExpiredEventHandler extends AbsHandler {

    private final JobEventService expiredEventService;

    public ExpiredEventHandler(JobEventService expiredEventService) {
        this.expiredEventService = expiredEventService;
    }

    @Override
    public Resp<String> execute(HandlerEvent event) throws Exception {
        // 查询过期的event
        List<ExpiredEvent> events = expiredEventService.getExpiredEvent();
        if (events == null || events.size() <= 0) {
            return Resp.SUCCESS;
        }
        List<String> expiredIds = new ArrayList<>();
        for (ExpiredEvent expiredEvent : events) {
            String expiredStr = expiredEvent.getExpired();
            if (StringUtils.isEmpty(expiredEvent.getExpired())) {
                continue;
            }
            double expired = Double.parseDouble(expiredStr.substring(0, expiredStr.length() - 1));
            if (expiredStr.endsWith("d")) {
                expired = expired * 24;
            }
            double diff = (System.currentTimeMillis() - expiredEvent.getCreateTime().getTime()) / 1000.0 / 3600;
            if (diff > expired) {
                expiredIds.add(expiredEvent.getEventId());
            }
        }
        // 清除过期的event
        expiredEventService.clearExpiredEvent(expiredIds);
        return Resp.SUCCESS;
    }

}
