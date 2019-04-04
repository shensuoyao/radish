package org.sam.shen.scheduing.cluster;

import org.sam.shen.scheduing.cluster.ClusterPeer.NodeState;

/**
 * 投票结果对象
 * 
 * @author suoyao
 * @date 2019年2月26日 下午5:47:53
 * 
 */
public class Vote {

	// 服务节点ID
	private int nid;

	// 每次投票的随机ID
	// 根据jobNum调度的任务数计算
	private Integer rhid;

	// 投票的轮次
	private long electionEpoch = 1L;

	// 节点当前状态
	private NodeState state;

	public Vote(int nid, int rhid, long electionEpoch) {
		this.nid = nid;
		this.rhid = rhid;
		this.electionEpoch = electionEpoch;
		this.state = NodeState.LOOKING;
	}
	
	public Vote(int nid, int rhid, long electionEpoch, NodeState state) {
		this(nid, rhid, electionEpoch);
		this.state = state;
	}

	public Vote(int nid, int rhid) {
		this(nid, rhid, -1, NodeState.LOOKING);
	}

	public int getNid() {
		return nid;
	}

	public Integer getRhid() {
		return rhid;
	}

	public long getElectionEpoch() {
		return electionEpoch;
	}

	public NodeState getState() {
		return state;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Vote)) {
			return false;
		}
		Vote other = (Vote) o;
		if ((state == NodeState.LOOKING) || (other.state == NodeState.LOOKING)) {
			return (nid == other.nid && rhid == other.rhid && electionEpoch == other.electionEpoch);
		} else {
            return (nid == other.nid && electionEpoch == other.electionEpoch);
		}
	}
	
	@Override
	public int hashCode() {
		return (int) (nid & rhid);
	}
	
	@Override
	public String toString() {
		return String.format("(%d, %s, %s)", nid, Long.toHexString(rhid), Long.toHexString(electionEpoch));
	}

}
