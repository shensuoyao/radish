package org.sam.shen.core.handler.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.AbsHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.util.ScriptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author suoyao
 * @date 2018年8月6日 下午4:03:06
  *  脚本任务处理执行器
 */
@AHandler(name = "scriptHandler", description = "脚本任务处理器")
@Component
public class ScriptHandler extends AbsHandler {
	Logger logger = LoggerFactory.getLogger(ScriptHandler.class);
	
	private String scriptFileName;

	public ScriptHandler() {
		super();
	}

	@Override
	public Resp<String> execute(HandlerEvent event) throws Exception {

		// make script file
		scriptFileName = FilenameUtils.getFullPath(getLogFileName()).concat(getEventId())
		        .concat(event.getHandlerType().getSuffix());
		ScriptUtil.markScriptFile(scriptFileName, event.getCmd());

		// invoke
		int exitValue = ScriptUtil.execToFile(event.getHandlerType().getCmd(), scriptFileName, getLogFileName(), event.getParams());

		if (exitValue == 0) {
			return Resp.SUCCESS;
		} else if (exitValue == 101) {
			return Resp.FAIL;
		} else {
			return new Resp<String>(Resp.FAIL.getCode(), "script exit value(" + exitValue + ") is failed");
		}
	}

	@Override
	public Resp<String> destory() {
		super.destory();
		try {
			FileUtils.forceDelete(new File(scriptFileName));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			scriptFileName = null;
		}
		return null;
	}

}
