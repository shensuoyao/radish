package org.sam.shen.monitor.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 告警通知人信息
 * @author clock
 * @date 2019-05-17 13:49
 */
@Getter
@Setter
public class Notifier implements Serializable {

    private static final long serialVersionUID = 4339765244545889341L;

    // 通知人姓名
    private String name;

    // 通知人手机号码
    private String phone;

    // 通知人邮箱
    private String email;

}
