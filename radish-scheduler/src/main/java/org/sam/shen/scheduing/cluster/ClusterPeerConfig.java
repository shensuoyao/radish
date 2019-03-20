package org.sam.shen.scheduing.cluster;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.sam.shen.core.util.IpUtil;
import org.sam.shen.scheduing.cluster.ClusterPeer.ClusterServer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;

/**
 * 集群配置
 * @author suoyao
 * @date 2019年3月1日 上午11:13:01
  * 
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix="cluster")
public class ClusterPeerConfig {

	// 服务器节点ID
	private Integer nid;
	
	/*
	 * 同步阶段可使用的tick次数 
	 * 建议当节点服务器数量比较多，网络延迟比较大的情况下适当的调高该参数
	 */
	private int initLimit;
	
	// 滚动调用频率
	private int tickTime;
	
	// 同步超时限制
	private int syncLimit;
	
	// cnx网络超时限制
	private int cnxTimeout;
	
	// 是否监听所有可用IP
	private boolean clusterListenOnAllIPs = false;
	
	// 集群服务器配置
	private Map<String, String> servers = Maps.newHashMap();
	
	protected final HashMap<Integer,ClusterServer> clusterPeerServers =
	        new HashMap<Integer, ClusterServer>();
	
	// 默认的选举端口
	private final int electionPort = 3888;
	
	private final int leaderPort = 2888;
	
	/**
	 * 初始化集群配置
	 * @author suoyao
	 * @date 下午2:36:20
	 * @return
	 */
	public ClusterPeerConfig init() {
		if(null != servers && servers.size() > 0) {
			String localIpAddress = IpUtil.getIp();
			for(String key : servers.keySet()) {
				if(key.startsWith("node.")) {
					int dot = key.indexOf('.');
					int nodeId = Integer.valueOf(key.substring(dot + 1));
					String serverStr = servers.get(key).trim();
					String parts[] = serverStr.split(":");
					
					// 设置Cluster Peer的nid
					if(localIpAddress.equals(parts[0])) {
						this.nid = nodeId;
					}
					
					/*
					 * 设置集群服务器
					 */
					InetSocketAddress leaderAddr = null;
					if(parts.length == 1) {
						leaderAddr = new InetSocketAddress(parts[0], leaderPort);
					} else if(parts.length > 1) {
						leaderAddr = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
					}
					
					if (parts.length == 2) {
						InetSocketAddress electionAddr = new InetSocketAddress(parts[0], electionPort);
						clusterPeerServers.put(Integer.valueOf(nodeId), new ClusterServer(nodeId, leaderAddr, electionAddr));
					} else if (parts.length == 3) {
						InetSocketAddress electionAddr = new InetSocketAddress(parts[0], Integer.parseInt(parts[2]));
						clusterPeerServers.put(Integer.valueOf(nodeId),
						        new ClusterServer(nodeId, leaderAddr, electionAddr));
					}
					
				}
			}
		}
		return this;
	}
	
	public boolean isClusterDeploy() {
		return servers.size() > 1;
	}
	
}
