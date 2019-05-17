package org.sam.shen.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.sam.shen.core.constants.MonitorType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author clock
 * @date 2019-05-13 17:28
 */
@Getter
@Setter
public class MonitorInfo implements Serializable {

    private static final long serialVersionUID = 803178685224196292L;

    private String id;

    private String bizId;

    private MonitorType monitorType;

    private String alarmType;

    private Map<String, String> extra;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public MonitorInfo() {}

    public MonitorInfo(String id, String bizId, MonitorType monitorType, Map<String, String> extra, Date createTime, String alarmType) {
        this.id = id;
        this.bizId = bizId;
        this.monitorType = monitorType;
        this.extra = extra;
        this.createTime = createTime;
        this.alarmType = alarmType;
    }
}
