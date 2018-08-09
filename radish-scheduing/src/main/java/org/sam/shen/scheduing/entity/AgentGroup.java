package org.sam.shen.scheduing.entity;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
  *  客户端机组
 * @author suoyao
 * @date 2018年8月8日 下午4:01:13
  * 
 */
public class AgentGroup extends Identity {
	private static final long serialVersionUID = 1551264174238873923L;

	// Agent 组名
	private String groupName;
	
	// 创建时间
	private Date createTime;
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteNullStringAsEmpty);
	}
}
