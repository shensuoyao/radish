package org.sam.shen.scheduing.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019/2/25 上午10:41
 */
@Getter
@Setter
public class User implements Serializable {

    private static final long serialVersionUID = -7852128788559669339L;

    private Long id;

    private String uname;

    private String password;

    private int enable;

}
