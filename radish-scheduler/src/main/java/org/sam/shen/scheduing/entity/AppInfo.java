package org.sam.shen.scheduing.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019/1/8 上午9:44
 */
@Getter
@Setter
public class AppInfo implements Serializable {

    private static final long serialVersionUID = 6695344033767602429L;

    private String appId;

    private String appName;

    private String domain;

    private Long userId;

}
