package org.sam.shen.core.handler;

import java.util.List;

import org.sam.shen.core.log.RadishLogFileAppender;
import org.sam.shen.core.model.Resp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbsHandler implements IHandler {
	Logger logger = LoggerFactory.getLogger(AbsHandler.class);
	
	/**
	 *  任务调用ID
	 */
	protected String callId;
	
	/**
	 *  日志文件名
	 */
	protected String logFileName;

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public abstract Resp<String> execute(CallBackParam param) throws Exception;

	@Override
	public Resp<String> init() {
		logger.info("RADISH ------------> 初始化任务...");
		return null;
	}

	@Override
	public Resp<String> destory() {
		logger.info("RADISH ------------> 清理任务:  {}",  callId);
		setCallId(null);
		setLogFileName(null);
		return null;
	}

	@Override
	public Resp<String> start(CallBackParam param) throws Exception {
		this.callId = param.getJobId();
		this.logFileName = RadishLogFileAppender.makeLogFile(this.callId);
		return execute(param);
	}

	protected void log(List<String> logLines) {
		RadishLogFileAppender.appendLog(logFileName, logLines);
	}
	
}
