package org.sam.shen.scheduing.cluster;

import org.sam.shen.core.util.Identities;

import lombok.Getter;
import lombok.Setter;

/**
 * 集群中节点服务之间通讯的数据包
 * @author suoyao
 * @date 2019年3月15日 下午3:07:17
  * 
 * @param <T>
 */
@Getter
@Setter
public class ClusterPacket<T> {
	// packet信息的唯一ID
	private String uxid;
	
	private Integer type;
	
	private Integer nid;
	
	private int rhid;
	
	// 此T必须为包装类型
	private T t;
	
	public ClusterPacket() {
		this.uxid = Identities.uuid();
	}

	public ClusterPacket(int type) {
		this();
		this.type = type;
	}
	
	public ClusterPacket(int type, int nid) {
		this();
		this.type = type;
		this.nid = nid;
	}

	public ClusterPacket(int type, int nid, int rhid) {
		this();
		this.type = type;
		this.nid = nid;
		this.rhid = rhid;
	}
	
	public ClusterPacket(int type, int nid, int rhid, T t) {
		this();
		this.type = type;
		this.nid = nid;
		this.rhid = rhid;
		this.t = t;
	}
	
	public boolean isEmpty() {
		return (type == null || nid == null);
	}
	
	@Override
	public boolean equals(Object packet_) {
		if (!(packet_ instanceof ClusterPacket)) {
			return false;
		}
		if (packet_ == this) {
			return true;
		}
		ClusterPacket<?> packet = (ClusterPacket<?>) packet_;
		boolean ret = false;
		ret = (uxid.equals(packet.uxid));
		if (!ret) return ret;
		ret = (type == packet.type);
		if (!ret) return ret;
		ret = (nid == packet.nid);
		if (!ret) return ret;
		ret = (rhid == packet.rhid);
		if (!ret) return ret;
		ret = t.equals(packet.getT());
		if (!ret) return ret;
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 17;
		if(null != uxid) {
			result = prime * result + uxid.hashCode();
		}
		if(null != type) {
			result = prime * result + type.hashCode();
		}
		if(null != nid) {
			result = prime * result + nid.hashCode();
		}
		result = prime * result + rhid;
		if(null != t) {
			result = prime * result + t.hashCode();
		}
		return super.hashCode();
	}
	
}
