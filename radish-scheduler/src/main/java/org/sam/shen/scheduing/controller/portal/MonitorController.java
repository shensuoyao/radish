package org.sam.shen.scheduing.controller.portal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.sam.shen.core.constants.Constant;
import org.sam.shen.core.model.AgentMonitorInfo;
import org.sam.shen.scheduing.service.RedisService;
import org.sam.shen.scheduing.vo.DynamicChartNodeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller
@RequestMapping(value = "monitor")
public class MonitorController {

	private final RedisService redisService;

    @Autowired
    public MonitorController(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
	 * 获取在线的Agent
	 * @author suoyao
	 * @date 下午3:15:52
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "agent-online", method = RequestMethod.GET)
	public ModelAndView agentOnline(ModelAndView model) {
		List<AgentMonitorInfo> agentOnlines = Lists.newArrayList();
		Set<String> keys = redisService.getKeys(Constant.REDIS_AGENT_PREFIX.concat("*"));
		if(null != keys && keys.size() > 0) {
			keys.forEach(key -> {
				Map<String, Object> hash = redisService.hmget(key);
                agentOnlines.add(JSON.parseObject(JSON.toJSONString(hash), AgentMonitorInfo.class));
			});
		}
		model.addObject("agentOnlines", agentOnlines);
		model.setViewName("frame/monitor/agent_online");
		return model;
	}
	
	@RequestMapping(value = "agent-online-monitor/{agentId}", method = RequestMethod.GET)
	public ModelAndView agentOnlineMonitor(ModelAndView model, @PathVariable("agentId") Long agentId) {
		Map<String, Object> hash = redisService.hmget(Constant.REDIS_AGENT_PREFIX + agentId);
		AgentMonitorInfo agentMonitorInfo = JSON.parseObject(JSON.toJSONString(hash), AgentMonitorInfo.class);

		model.addObject("agentMonitorInfo", agentMonitorInfo);
		model.setViewName("frame/monitor/agent_online_monitor");
		return model;
	}
	
	@RequestMapping(value = "agent-online-monitor-dynamic/{agentId}", method = RequestMethod.GET)
	@ResponseBody
	public DynamicChartNodeVo agentOnlineMonitorDynamic(@PathVariable("agentId") Long agentId, String type) {
		Map<String, Object> hash = redisService.hmget(Constant.REDIS_AGENT_PREFIX + agentId);
		if(null == hash || StringUtils.isEmpty(type)) {
			return new DynamicChartNodeVo();
		}
		AgentMonitorInfo monitorInfo = JSON.parseObject(JSON.toJSONString(hash), AgentMonitorInfo.class);
		String t = new DateTime().toString("HH:mm:ss");
        DynamicChartNodeVo dynamicChartVo = new DynamicChartNodeVo(t);
        Map<String, Object> yAxis = Maps.newHashMap();

        switch (type) {
            case "cpu":
            	dynamicChartVo.setMeasurement("%");
                yAxis.put("空闲", monitorInfo.getCpuIdle());
                yAxis.put("用户", monitorInfo.getCpuUser());
                yAxis.put("系统", monitorInfo.getCpuSystem());
                break;
            case "mem":
				dynamicChartVo.setMeasurement("KB");
                yAxis.put("空闲内存", monitorInfo.getMemoryFree());
                break;
            case "disk":
                dynamicChartVo.setMeasurement("KB/s");
                yAxis.put("写出/秒", monitorInfo.getIoWtps());
                yAxis.put("读入/秒", monitorInfo.getIoRtps());
                break;
            case "net":
                dynamicChartVo.setMeasurement("KB/s");
                if (monitorInfo.getNetworkIOList().size() > 0) {
                    List<AgentMonitorInfo.NetworkIO> eth = monitorInfo.getNetworkIOList().stream().filter(net -> "eth0".equals(net.getIface())).collect(Collectors.toList());
                    if (eth.size() > 0) {
                        yAxis.put("接受数据/秒", eth.get(0).getRx());
                        yAxis.put("发送数据/秒", eth.get(0).getTx());
                    } else {
                        yAxis.put("接受数据/秒", monitorInfo.getNetworkIOList().get(0).getRx());
                        yAxis.put("发送数据/秒", monitorInfo.getNetworkIOList().get(0).getTx());
                    }
                } else {
                    yAxis.put("接受数据/秒", null);
                    yAxis.put("发送数据/秒", null);
                }
                break;
        }
        dynamicChartVo.setyAxis(yAxis);
		return dynamicChartVo;
	}
	
}
