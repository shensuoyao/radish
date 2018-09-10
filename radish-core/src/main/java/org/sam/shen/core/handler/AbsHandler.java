package org.sam.shen.core.handler;

import java.util.List;

import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.Resp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbsHandler implements IHandler {
	Logger logger = LoggerFactory.getLogger(AbsHandler.class);
	
	/**
	 *  任务调用ID
	 */
	protected String eventId;
	
	/**
	 *  日志文件名
	 */
	protected String logFileName;

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public abstract Resp<String> execute(HandlerEvent event) throws Exception;

	@Override
	public Resp<String> init() {
		logger.info("RADISH ------------> 初始化任务...");
		return Resp.SUCCESS;
	}

	@Override
	public Resp<String> destory() {
		logger.info("RADISH ------------> 清理任务:  {}",  eventId);
		setEventId(null);
		setLogFileName(null);
		return Resp.SUCCESS;
	}

	@Override
	public Resp<String> start(HandlerEvent event) throws Exception {
		this.eventId = event.getEventId();
		this.logFileName = RadishLogFileAppender.makeLogFile(this.eventId);
		return execute(event);
	}

	protected void log(List<String> logLines) {
		RadishLogFileAppender.appendLog(logFileName, logLines);
	}
	
}
