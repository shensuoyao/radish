package org.sam.shen.scheduing.service;

import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author clock
 * @date 2019/2/25 下午4:45
 */
@Service
public class LoginService {

    @Resource
    private UserMapper userMapper;

    public void login(User user) {
        userMapper.login(user.getUname(), user.getPassword());
    }
}
