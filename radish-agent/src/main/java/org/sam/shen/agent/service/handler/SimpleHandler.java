package org.sam.shen.agent.service.handler;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.sam.shen.core.handler.AbsHandler;
import org.sam.shen.core.handler.CallBackParam;
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
	public Resp<String> execute(CallBackParam param) throws Exception {
		for(String p : param.getParams()) {
			logger.info(getJobId() + " 处理任务 p : {}", p);
			log(Arrays.asList("jobId = " + getJobId() + " ====>>> " + p));
			TimeUnit.SECONDS.sleep(10);
		}
		return null;
	}
	
}
