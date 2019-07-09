package org.sam.shen.agent.handler;

import org.sam.shen.agent.service.JobEventService;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.AbsHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.model.Resp;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author clock
 * @date 2019-05-28 17:25
 */
@Component
@AHandler(name = "migrateToHisHandler", description = "将处理过的event移到历史库")
public class MigrateToHisHandler extends AbsHandler {

    private final JobEventService jobEventService;

    public MigrateToHisHandler(JobEventService jobEventService) {
        this.jobEventService = jobEventService;
    }

    @Override
    public Resp<String> execute(HandlerEvent event) throws Exception {
        List<Map<String, Object>> handledEvent = jobEventService.limitHandledEvent(500);
        while (handledEvent != null && handledEvent.size() > 0) {
            try {
                jobEventService.migrateHandledEvent(handledEvent);
                handledEvent = jobEventService.limitHandledEvent(500);
            } catch (Exception e) {
                log("部分数据迁移失败！["
                        + handledEvent.stream().map(map -> String.valueOf(map.get("event_id"))).collect(Collectors.joining(","))
                        + "] [" + e.getMessage() + "]");
                return Resp.FAIL;
            }
        }

        return Resp.SUCCESS;
    }

}
