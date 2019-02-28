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
public class UserAgentGroup implements Serializable {

    private static final long serialVersionUID = -1283547188836125237L;

    private Long id;

    private Long userId;

    private Long groupId;

    public UserAgentGroup(Long userId, Long groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
}
