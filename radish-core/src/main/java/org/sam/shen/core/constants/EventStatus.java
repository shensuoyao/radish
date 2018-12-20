package org.sam.shen.core.constants;

/**
 * @author suoyao
 * @date 2018年8月6日 下午1:51:43
  *   处理器类型枚举
 */
public enum EventStatus {

	WAIT(0),
	READY(1),	
	HANDLE(2),
	FAIL(3),
	SUCCESS(4),
	RETRY(5);
	
	private int stat;
	
	EventStatus(int stat) {
        this.stat = stat;
    }
	
	public int getStat() {
		return stat;
	}

	public static EventStatus match(String name) {
		for (EventStatus item : EventStatus.values()) {
			if (item.name().equals(name)) {
				return item;
			}
		}
		return null;
	}
	
}
