package org.sam.shen.core.constants;

/**
 * @author suoyao
 * @date 2018年8月6日 下午1:51:43
  *   处理器类型枚举
 */
public enum HandlerTypeEnum {

	H_JAVA("Java", null, null),
	H_SHELL("Shell", "bash", ".sh"),
	H_PYTHON("Python", "python", ".py"),
	H_JAVASCRIPT("Javascript", "javascript", ".js");
	
	private String desc;
	private String cmd;
	private String suffix;
	
	private HandlerTypeEnum(String desc, String cmd, String suffix) {
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
	
	public static HandlerTypeEnum match(String name) {
		for (HandlerTypeEnum item : HandlerTypeEnum.values()) {
			if (item.name().equals(name)) {
				return item;
			}
		}
		return null;
	}
	
}
