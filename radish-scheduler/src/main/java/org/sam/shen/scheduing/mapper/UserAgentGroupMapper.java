package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.UserAgentGroup;

import java.util.List;

/**
 * @author clock
 * @date 2019/2/25 上午10:49
 */
@Mapper
public interface UserAgentGroupMapper {

    int insert(UserAgentGroup userAgentGroup);

    int batchInsert(List<UserAgentGroup> list);

    void deleteByUserId(Long userId);

    void deleteByGroupId(Long groupId);
}
