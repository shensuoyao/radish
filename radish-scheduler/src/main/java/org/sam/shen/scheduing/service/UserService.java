package org.sam.shen.scheduing.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.entity.UserAgentGroup;
import org.sam.shen.scheduing.mapper.UserAgentGroupMapper;
import org.sam.shen.scheduing.mapper.UserMapper;
import org.sam.shen.scheduing.vo.UserAgentGroupVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clock
 * @date 2019/2/26 下午2:19
 */
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserAgentGroupMapper userAgentGroupMapper;

    public Page<UserAgentGroupVo> selectUserWithPage(String uname, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        return userMapper.selectUserAndGroup(uname);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveUserGroup(User user, List<String> groups) {
        userMapper.insert(user);
        if (groups != null && groups.size() > 0) {
            List<UserAgentGroup> agentGroups = new ArrayList<>();
            for (String groupId : groups) {
                agentGroups.add(new UserAgentGroup(user.getId(), Long.parseLong(groupId)));
            }
            userAgentGroupMapper.batchInsert(agentGroups);
        }
    }

    public UserAgentGroupVo selectUserGroupById(String userId) {
        return userMapper.selectUserAndGroupById(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserGroup(User user, List<String> groups) {
        userMapper.update(user);
        userAgentGroupMapper.deleteByUserId(user.getId());
        if (groups != null && groups.size() > 0) {
            List<UserAgentGroup> agentGroups = new ArrayList<>();
            for (String groupId : groups) {
                agentGroups.add(new UserAgentGroup(user.getId(), Long.parseLong(groupId)));
            }
            userAgentGroupMapper.batchInsert(agentGroups);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUserGroup(Long userId){
        userMapper.delete(userId);
        userAgentGroupMapper.deleteByUserId(userId);
    }

}
