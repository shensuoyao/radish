package org.sam.shen.core.model;

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;

/**
 *  Agent客户端信息
 * @author suoyao
 * @date 2018年8月7日 上午9:16:07
  *  
 */
@Getter
@Setter
public class AgentInfo {

	// Agent注册之后获得, 全局唯一
	private Long agentId;
	
	// Agent 管理名(需要唯一)
	private String agentName;
	
	// ip
	private String agentIp;
	
	// Rest RPC 端口
	private int agentPort;

	// 与Agent的通信模式
	private String network;

	// netty监听端口
	private Integer nettyPort;
	
	// Agent中注册的Handler处理器集合
	private Map<String, String> registryHandlerMap;

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteMapNullValue);
	}
	
}
