package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019/2/26 下午1:32
 */
@Getter
@Setter
public class UserAgentGroupVo implements Serializable {

    private static final long serialVersionUID = 7217750662578790296L;

    private Long userId;

    private String uname;

    private String password;

    private Integer enable;

    private String groups;

    private String groupIds;

}
