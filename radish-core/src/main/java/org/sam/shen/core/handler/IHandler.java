package org.sam.shen.core.handler;

import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.model.Resp;

/**
 * @author suoyao
 * Job execution interface
 */
public interface IHandler {
	
	Resp<String> start(HandlerEvent event) throws Exception;
	
	/**
	 * @return
	 * Job initialization method
	 */
	Resp<String> init();
	
	/**
	 * @return
	 * Job destruction method
	 */
	Resp<String> destroy();
    
}
