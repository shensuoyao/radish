package org.sam.shen.core.constants;

/**
 * @author suoyao
 * @date 2018年8月6日 下午1:51:43
  *   处理器类型枚举
 */
public enum HandlerType {

	H_JAVA("Java", null, null),
	H_SHELL("Shell", "bash", ".sh"),
	H_PYTHON("Python", "python", ".py"),
	H_JAVASCRIPT("Javascript", "node", ".js");
	
	private String desc;
	private String cmd;
	private String suffix;
	
	private HandlerType(String desc, String cmd, String suffix) {
        this.desc = desc;
        this.cmd = cmd;
        this.suffix = suffix;
    }

	public String getDesc() {
		return desc;
	}

	public String getCmd() {
		return cmd;
	}

	public String getSuffix() {
		return suffix;
	}
	
	public static HandlerType match(String name) {
		for (HandlerType item : HandlerType.values()) {
			if (item.name().equals(name)) {
				return item;
			}
		}
		return null;
	}
	
}
