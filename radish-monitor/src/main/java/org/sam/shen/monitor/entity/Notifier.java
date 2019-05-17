package org.sam.shen.monitor.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019-05-17 13:49
 */
@Getter
@Setter
public class Notifier implements Serializable {

    private static final long serialVersionUID = 4339765244545889341L;

    private String name;

    private String phone;

    private String email;

}
