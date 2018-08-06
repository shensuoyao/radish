package org.sam.shen.core.handler;

import org.sam.shen.core.model.Resp;

/**
 * @author suoyao
 * Job execution interface
 */
public interface IHandler {
	
	Resp<String> start(CallBackParam param) throws Exception;
	
	/**
	 * @return
	 * Job initialization method
	 */
	Resp<String> init();
	
	/**
	 * @return
	 * Job destruction method
	 */
	Resp<String> destory();
    
}
