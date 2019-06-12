package org.sam.shen.scheduing.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.vo.UserAgentGroupVo;

/**
 * @author clock
 * @date 2019/2/25 上午10:48
 */
@Mapper
public interface UserMapper {

    User login(@Param("uname") String uname, @Param("password") String password);

    int insert(User user);

    int update(User user);

    int delete(Long userId);

    int modifyPassword(@Param("id") Long id, @Param("password") String password);

    int enableUser(@Param("id") long userId, @Param("enable") int enable);

    Page<UserAgentGroupVo> selectUserAndGroup(@Param("uname") String uname);

    UserAgentGroupVo selectUserAndGroupById(String userId);
}
