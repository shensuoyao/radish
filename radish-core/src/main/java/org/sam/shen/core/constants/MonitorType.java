package org.sam.shen.core.constants;

/**
 * 监控类型
 * @author clock
 * @date 2019-05-16 16:46
 */
public enum MonitorType {
    HEARTBEAT("HeartBeatMonitoring"),
    EVENT("EventMonitoring"),
    THRESHOLD("ThresholdMonitoring"),
    FAILURE("FailureMonitoring");

    private String className;

    MonitorType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
