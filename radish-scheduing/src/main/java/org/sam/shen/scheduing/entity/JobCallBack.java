package org.sam.shen.scheduing.entity;

import java.util.Set;

import org.sam.shen.core.handler.CallBackParam;

/**
 *  任务执行
 * @author suoyao
 * @date 2018年8月16日 下午1:45:09
  * 
 */
public class JobCallBack {

	private CallBackParam callbackParam;
	
	private Set<Long> agents;

	public CallBackParam getCallbackParam() {
		return callbackParam;
	}

	public void setCallbackParam(CallBackParam callbackParam) {
		this.callbackParam = callbackParam;
	}

	public Set<Long> getAgents() {
		return agents;
	}

	public void setAgents(Set<Long> agents) {
		this.agents = agents;
	}
	
}
