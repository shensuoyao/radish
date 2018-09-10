package org.sam.shen.scheduing.sendcloud.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 包含按每天查询和按小时查询的所有参数对象
 * @author shensuoyao
 *
 */
public class SendCloudMailCount {

	/**
	 * 过去 days 天内的统计数据 (days=1表示今天)
	 */
	private int days;
	
	
	/**
	 * 开始日期, 格式为yyyy-MM-dd
	 */
	private String startDate;
	
	/**
	 * 结束日期, 格式为yyyy-MM-dd
	 */
	private String endDate;
	
	/**
	 * 获取指定 API_USER 的统计数据, 多个 API_USER 用;分开, 如:apiUserList=a;b;c
	 */
	private List<String> apiUserList;
	
	/**
	 * 获取指定标签下的统计数据, 多个标签用;分开, 如:labelIdList=a;b;c
	 * 按天查询时用到
	 */
	private List<String> labelIdList;
	
	/**
	 * 获取指定域名下的统计数据, 多个域名用;分开, 如:domainList=a;b;c
	 * 按天查询时用到
	 */
	private List<String> domainList;
	
	/**
	 * boolean(1, 0) 默认为0. 如果为1, 则返回聚合数据
	 * 按天查询时用到
	 */
	private int aggregate = -1;

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public List<String> getApiUserList() {
		return apiUserList;
	}

	public void setApiUserList(List<String> apiUserList) {
		this.apiUserList = apiUserList;
	}

	public List<String> getLabelIdList() {
		return labelIdList;
	}

	public void setLabelIdList(List<String> labelIdList) {
		this.labelIdList = labelIdList;
	}

	public List<String> getDomainList() {
		return domainList;
	}

	public void setDomainList(List<String> domainList) {
		this.domainList = domainList;
	}

	public int getAggregate() {
		return aggregate;
	}

	public void setAggregate(int aggregate) {
		this.aggregate = aggregate;
	}
	
	public void addApiUser(String apiUser) {
		if (apiUserList == null)
			apiUserList = new ArrayList<String>();
		apiUserList.addAll(Arrays.asList(apiUser.split(";")));
	}
	
	public void addLabelId(String labelId) {
		if (labelIdList == null)
			labelIdList = new ArrayList<String>();
		labelIdList.addAll(Arrays.asList(labelId.split(";")));
	}
	
	public void addDomainList(String domain) {
		if (domainList == null)
			domainList = new ArrayList<String>();
		domainList.addAll(Arrays.asList(domain.split(";")));
	}
	
	public String toApiUserString() {
		StringBuilder sb = new StringBuilder();
		for (String apiUser : apiUserList) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(apiUser);
		}
		return sb.toString();
	}
	
	public String toLabelIdString() {
		StringBuilder sb = new StringBuilder();
		for (String labelId : labelIdList) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(labelId);
		}
		return sb.toString();
	}
	
	public String toDomainString() {
		StringBuilder sb = new StringBuilder();
		for (String domain : domainList) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(domain);
		}
		return sb.toString();
	}
	
}
