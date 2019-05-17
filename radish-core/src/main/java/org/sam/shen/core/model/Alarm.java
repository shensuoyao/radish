package org.sam.shen.core.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019-05-15 16:32
 */
@Getter
@Setter
public class Alarm implements Serializable {

    private static final long serialVersionUID = 6816475311590856895L;

    private String id;

    private String content;

    private String alarmType;

    private String notifier;

    private String email;

    private String phone;

}
