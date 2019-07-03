package org.sam.shen.core.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 告警信息
 * @author clock
 * @date 2019-05-15 16:32
 */
@Getter
@Setter
public class Alarm implements Serializable {

    private static final long serialVersionUID = 6816475311590856895L;

    private String id;

    // 告警邮箱内容
    private String content;

    // 告警类型，短信或者邮件
    private String alarmType;

    // 告警通知人
    private String notifier;

    // 告警通知邮箱
    private String email;

    // 告警通知手机号
    private String phone;

    // 告警短信模板
    private String template;

    // 告警短信模板参数
    private String[] templateParams;

}
