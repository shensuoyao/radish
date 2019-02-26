package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.User;

/**
 * @author clock
 * @date 2019/2/25 上午10:48
 */
@Mapper
public interface UserMapper {

    User login(@Param("uname") String uname, @Param("password") String password);

    Long insert(User user);

    int modifyPassword(@Param("uname") String uname, @Param("password") String password);

    int enableUser(@Param("id") long userId, @Param("enable") int enable);
}
