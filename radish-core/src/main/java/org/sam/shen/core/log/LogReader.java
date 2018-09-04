package org.sam.shen.core.log;

import java.io.Serializable;
import java.util.List;

/**
 * @author suoyao
 * @date 2018年8月3日 下午4:33:23
  *   日志读取对象
 */
public class LogReader implements Serializable {
	private static final long serialVersionUID = 9204393928504133496L;

	private int beginLineNum;
	private int endLineNum;
	private List<String> logLines;
	
	public LogReader() {
		super();
	}
	
	public LogReader(List<String> logLines) {
		this();
		this.logLines = logLines;
	}
	
	public LogReader(int beginLineNum, int endLineNum, List<String> logLines) {
		this(logLines);
		this.beginLineNum = beginLineNum;
		this.endLineNum = endLineNum;
	}

	public int getBeginLineNum() {
		return beginLineNum;
	}

	public void setBeginLineNum(int beginLineNum) {
		this.beginLineNum = beginLineNum;
	}

	public int getEndLineNum() {
		return endLineNum;
	}

	public void setEndLineNum(int endLineNum) {
		this.endLineNum = endLineNum;
	}

	public List<String> getLogLines() {
		return logLines;
	}

	public void setLogLines(List<String> logLines) {
		this.logLines = logLines;
	}
	
}
