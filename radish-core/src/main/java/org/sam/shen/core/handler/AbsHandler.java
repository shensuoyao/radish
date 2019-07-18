package org.sam.shen.core.handler;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.Resp;

@Slf4j
public abstract class AbsHandler implements IHandler {

	/**
	 *  任务调用ID
	 */
	protected ThreadLocal<String> eventId = new ThreadLocal<>();
	
	/**
	 *  日志文件名
	 */
	protected ThreadLocal<String> logFileName = new ThreadLocal<>();

	public String getEventId() {
		return eventId.get();
	}

	public void setEventId(String eventId) {
		this.eventId.set(eventId);
	}

	public String getLogFileName() {
		return logFileName.get();
	}

	public void setLogFileName(String logFileName) {
		this.logFileName.set(logFileName);
	}

	public abstract Resp<String> execute(HandlerEvent event) throws Exception;

	@Override
	public Resp<String> init() {
		log.info("RADISH ------------> 初始化任务");
		return Resp.SUCCESS;
	}

	@Override
	public Resp<String> destroy() {
		log.info("RADISH ------------> 清理任务:  {}",  eventId.get());
		eventId.remove();
		logFileName.remove();
		return Resp.SUCCESS;
	}

	@Override
	public Resp<String> start(HandlerEvent event) throws Exception {
		log.info("RADISH ------------> 任务执行中:  {}", event.getEventId());
		this.setEventId(event.getEventId());
		this.setLogFileName(RadishLogFileAppender.makeLogFile(this.getEventId()));
		return execute(event);
	}

	protected void log(List<String> logLines) {
		RadishLogFileAppender.appendLog(this.getLogFileName(), logLines);
	}
	
	protected void log(String logStr) {
		if(StringUtils.isNotEmpty(logStr)) {
			RadishLogFileAppender.appendLog(this.getLogFileName(), Arrays.asList(logStr));
		}
	}
	
}
