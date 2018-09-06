package org.sam.shen.scheduing.vo;

import java.util.Map;

public class DynamicChartNodeVo {

	private Object xAxis;
	
	private Map<String, Object> yAxis;
	
	public DynamicChartNodeVo() {
		super();
	}
	
	public DynamicChartNodeVo(Object xAxis) {
		this();
		this.xAxis = xAxis;
	}

	public Object getxAxis() {
		return xAxis;
	}

	public void setxAxis(Object xAxis) {
		this.xAxis = xAxis;
	}

	public Map<String, Object> getyAxis() {
		return yAxis;
	}

	public void setyAxis(Map<String, Object> yAxis) {
		this.yAxis = yAxis;
	}
	
}
