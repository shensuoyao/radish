package org.sam.shen.scheduing.controller.core;

import org.sam.shen.core.constants.HandlerTypeEnum;
import org.sam.shen.core.handler.CallBackParam;
import org.sam.shen.core.model.AgentInfo;
import org.sam.shen.core.model.AgentPerformance;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

/**
 * @author suoyao
 * @date 2018年7月31日 下午3:23:54
 * Agent core APIs
 */
@RestController
@RequestMapping(value = "/core")
public class CoreController {
	Logger logger = LoggerFactory.getLogger(CoreController.class);
	
	@Autowired
	private AgentService agentService;

	/**
	 * @author suoyao
	 * @date 下午12:58:09
	 * @param agentInfo
	 * @return
	 * Agent Registry 
	 */
	@RequestMapping(value="/registry", method = RequestMethod.PUT)
	public Resp<String> registry(@RequestBody AgentInfo agentInfo) {
		if(logger.isInfoEnabled()) {
			logger.info("Agent Registry : {}", agentInfo.toString());
		}
		if(agentService.registry(agentInfo)) {
			return Resp.SUCCESS;
		}
		return Resp.FAIL;
	}
	
	/**
	 * @author suoyao
	 * @date 下午3:24:44
	 * @return
	 * Agent heartbeat call
	 */
	@RequestMapping(value = "/heartbeat", method = RequestMethod.POST)
	@ApiOperation(value="Registry Agent Machines", notes="Agent Registry heartbeat to Scheduing")
	public Resp<AgentPerformance> heartbeat(@RequestBody AgentPerformance agent) {
		logger.info("agent {} heartbeat: {}", agent.getAgentName(), agent.toString());
		return new Resp<>(agent);
	}
	
	/**
	 * @author suoyao
	 * @date 下午5:57:48
	 * @param agentName
	 * @return
	 *   触发任务接口
	 */
	@RequestMapping(value = "/triggercall", method = RequestMethod.GET)
	@ApiOperation(value="Registry Agent Machines", notes="Agent Trigger Task from Scheduing")
	public Resp<CallBackParam> triggerCall(@RequestParam("agentName") String agentName) {
		// 根据Agent机器性能决定是否能抢到任务
		CallBackParam callBackParam = new CallBackParam();
		callBackParam.setJobId(String.valueOf(System.currentTimeMillis()));
		callBackParam.setRegistryHandler("scriptHandler");
		callBackParam.setCmd("ls -al /run/media/suoyao/develop");
		callBackParam.setHandlerType(HandlerTypeEnum.H_SHELL);
		// String[] params = {"君问归期未有期，", "巴山夜雨涨秋池。", "何当共剪西窗烛，", "却话巴山夜雨时。 "};
		// callBackParam.setParams(params);
		return new Resp<>(callBackParam);
	}
	
}
