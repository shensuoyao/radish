package org.sam.shen.agent.controller;

import org.sam.shen.core.log.LogReader;
import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.Resp;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author suoyao
 * @date 2018年8月6日 下午5:09:58
  *  客户端日志接口
 */
@RestController
@Component
@RequestMapping("handler-log")
public class HandlerLogController {

	/**
	  *   读取任务日志
	 * @author suoyao
	 * @date 下午5:36:27
	 * @param jobId
	 * @return
	 */
	@RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
	public Resp<LogReader> readHandlerLog(@PathVariable("eventId") String eventId,
	        @RequestParam(value = "beginLineNum", required = false) Integer beginLineNum) {
		String logFileName = RadishLogFileAppender.makeLogFile(eventId);
		return new Resp<>(RadishLogFileAppender.readLog(logFileName, beginLineNum));
	}
	
}
