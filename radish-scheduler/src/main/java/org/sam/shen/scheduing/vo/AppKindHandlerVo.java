package org.sam.shen.scheduing.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author clock
 * @date 2019/2/18 下午4:01
 */
@Getter
@Setter
public class AppKindHandlerVo implements Serializable {

    private static final long serialVersionUID = -2623928046120213650L;

    private String kindId;

    private String kind;

    private String kindHandlerId;

    private String handlerId;

    private String agentId;

    private String handler;

}
