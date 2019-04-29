package org.sam.shen.core.handler.impl;

import java.io.File;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sam.shen.core.constants.HandlerType;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.handler.AbsHandler;
import org.sam.shen.core.handler.anno.AHandler;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.util.ScriptUtil;

/**
 * @author suoyao
 * @date 2018年8月6日 下午4:03:06
  *  脚本任务处理执行器
 */
@Slf4j
@AHandler(name = "scriptHandler", description = "脚本任务处理器")
public class ScriptHandler extends AbsHandler {

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

        if (event.getHandlerType() == HandlerType.H_JAVA) { // execute java with bean shell
	        Resp<String> result = ScriptUtil.execBshScriptWithResult(scriptFileName);
	        // print logs
	        log(Collections.singletonList(result.getData()));
	        return result;
        } else {
            // invoke
            int exitValue = ScriptUtil.execToFile(event.getHandlerType().getCmd(), scriptFileName, getLogFileName(), event.getParams());

            if (exitValue == 0) {
                return Resp.SUCCESS;
            } else if (exitValue == 101) {
                return Resp.FAIL;
            } else {
                return new Resp<>(Resp.FAIL.getCode(), "script exit value(" + exitValue + ") is failed");
            }
        }
	}

	@Override
	public Resp<String> destroy() {
		super.destroy();
		try {
			FileUtils.forceDelete(new File(scriptFileName));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			scriptFileName = null;
		}
		return null;
	}

}
