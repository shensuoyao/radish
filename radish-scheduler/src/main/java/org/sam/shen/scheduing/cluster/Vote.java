package org.sam.shen.scheduing.cluster;

/**
 * 投票结果对象
 * 
 * @author suoyao
 * @date 2019年2月26日 下午5:47:53
 * 
 */
public class Vote {

	// 服务节点ID
	private String nodeId;

	// 每次投票的随机ID
	// 根据jobNum调度的任务数计算
	private long rhid;

	// 猜拳ID，随机计算
	private long moraid;

	// 投票的轮次
	private long electionEpoch;

	// 节点当前状态
	private NodeState state;

	// 节点版本号
	final private long version;

	public Vote(String nodeId, long rhid, long moraid, long electionEpoch) {
		this.version = System.currentTimeMillis();
		this.nodeId = nodeId;
		this.rhid = rhid;
		this.moraid = moraid;
		this.electionEpoch = electionEpoch;
	}

	public Vote(String nodeId, long rhid, long moraid) {
		this(nodeId, rhid, moraid, -1);
	}

	public String getNodeId() {
		return nodeId;
	}

	public long getRhid() {
		return rhid;
	}

	public long getMoraid() {
		return moraid;
	}

	public long getElectionEpoch() {
		return electionEpoch;
	}

	public NodeState getState() {
		return state;
	}

	public long getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s, %s, %d)", nodeId, Long.toHexString(rhid), Long.toHexString(moraid), version);
	}

}
