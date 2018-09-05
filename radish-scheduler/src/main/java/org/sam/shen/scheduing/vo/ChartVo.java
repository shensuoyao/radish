package org.sam.shen.scheduing.vo;

import java.util.List;

import com.google.common.collect.Lists;

public class ChartVo {

	private String text;
	
	private List<String> legend;
	
	private List<Object> xAxis;
	
	private List<Object> yAxis;
	
	public ChartVo() {
		super();
		this.legend = Lists.newArrayList();
		this.xAxis = Lists.newArrayList();
		this.yAxis = Lists.newArrayList();
	}
	
	public ChartVo(String text) {
		this();
		this.text = text;
	}
	
	public void addLegend(String legend) {
		this.legend.add(legend);
	}
	
	public void addXAxis(Object obj) {
		this.xAxis.add(obj);
	}
	
	public void addYAxis(Object obj) {
		this.yAxis.add(obj);
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<String> getLegend() {
		return legend;
	}

	public void setLegend(List<String> legend) {
		this.legend = legend;
	}

	public List<Object> getxAxis() {
		return xAxis;
	}

	public void setxAxis(List<Object> xAxis) {
		this.xAxis = xAxis;
	}

	public List<Object> getyAxis() {
		return yAxis;
	}

	public void setyAxis(List<Object> yAxis) {
		this.yAxis = yAxis;
	}
	
}
