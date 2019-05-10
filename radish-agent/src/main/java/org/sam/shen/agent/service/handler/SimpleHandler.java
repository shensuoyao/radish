package org.sam.shen.agent.service.handler;

import java.util.concurrent.TimeUnit;

import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.AbsHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.model.Resp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@AHandler(name = "simpleHandler", description = "简单的任务处理器示例")
@Component
public class SimpleHandler extends AbsHandler {
	Logger logger = LoggerFactory.getLogger(SimpleHandler.class);

	@Override
	public Resp<String> execute(HandlerEvent event) throws Exception {
		for(String p : event.getParams()) {
			logger.info(getEventId() + " 处理任务 p : {}", p);
			log("jobId = " + getEventId() + " ====>>> " + p);
			TimeUnit.SECONDS.sleep(10);
		}
		return null;
	}
	
}
