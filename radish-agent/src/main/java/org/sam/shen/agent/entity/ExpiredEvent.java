package org.sam.shen.agent.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author clock
 * @date 2019-05-22 17:49
 */
@Getter
@Setter
public class ExpiredEvent implements Serializable {

    private static final long serialVersionUID = 3935609957003183912L;

    private String eventId;

    private String expired;

    private Date createTime;

}
