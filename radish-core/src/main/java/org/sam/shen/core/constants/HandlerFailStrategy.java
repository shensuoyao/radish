package org.sam.shen.core.constants;

/**
 * @author suoyao
 * @date 2018年8月6日 下午1:51:43
  *   处理器类型枚举
 */
public enum HandlerFailStrategy {

	DISCARD("丢弃"),
	RETRY("重试");
//	ALARM("告警");
	
	private String desc;
	
	HandlerFailStrategy(String desc) {
        this.desc = desc;
    }

	public String getDesc() {
		return desc;
	}
	
	public static HandlerFailStrategy match(String name) {
		for (HandlerFailStrategy item : HandlerFailStrategy.values()) {
			if (item.name().equals(name)) {
				return item;
			}
		}
		return null;
	}
	
}
