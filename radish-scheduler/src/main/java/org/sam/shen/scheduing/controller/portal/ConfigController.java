package org.sam.shen.scheduing.controller.portal;

import com.github.pagehelper.Page;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.constants.SchedConstant;
import org.sam.shen.scheduing.entity.*;
import org.sam.shen.scheduing.service.AppService;
import org.sam.shen.scheduing.service.UserService;
import org.sam.shen.scheduing.vo.UserAgentGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author clock
 * @date 2019/1/29 下午5:50
 */
@Controller
@RequestMapping("config")
public class ConfigController {

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = {"app", "app/"}, method = RequestMethod.GET)
    public ModelAndView configApp(ModelAndView modelAndView) {
        modelAndView.setViewName("frame/config/config_app");
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "apps", method = RequestMethod.GET)
    public RespPager<Page<AppInfo>> getApps(@RequestParam(required = false) String appName,
                                            @RequestParam(defaultValue = "0") Integer page,
                                            @RequestParam(defaultValue = "10") Integer limit,
                                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (SchedConstant.ADMINISTRATOR.equals(user.getUname())) {
            user.setId(null);
        }
        Page<AppInfo> result = appService.getAppsWithPage(appName, page, limit, user.getId());
        return new RespPager<>(result.getPageSize(), result.getTotal(), result);
    }

    @ResponseBody
    @RequestMapping(value = "apps/{appId}", method = RequestMethod.GET)
    public Resp<AppInfo> getAppById(@PathVariable String appId) {
        AppInfo appInfo = appService.getAppById(appId);
        return new Resp<>(appInfo);
    }

    @ResponseBody
    @RequestMapping(value = "apps", method = RequestMethod.POST)
    public Resp<String> saveApp(@RequestBody AppInfo appInfo, HttpSession session) {
        User user = (User) session.getAttribute("user");
        appInfo.setUserId(user.getId());
        String appId = appService.saveApp(appInfo);
        return new Resp<>(appId);
    }

    @ResponseBody
    @RequestMapping(value = "apps/{appId}", method = RequestMethod.DELETE)
    public Resp<String> deleteApp(@PathVariable String appId) {
        appService.deleteApp(appId);
        return Resp.SUCCESS;
    }

    @ResponseBody
    @RequestMapping(value = "apps/{appId}", method = RequestMethod.PUT)
    public Resp<String> updateApp(@PathVariable String appId, @RequestBody AppInfo appInfo) {
        appInfo.setAppId(appId);
        if (appService.updateApp(appInfo)) {
            return Resp.SUCCESS;
        } else {
            return Resp.FAIL;
        }
    }

    @RequestMapping(value = "kind", method = RequestMethod.GET)
    public ModelAndView kind(@RequestParam String appId, ModelAndView modelAndView) {
        modelAndView.addObject("appId", appId);
        modelAndView.setViewName("frame/config/config_app_kind");
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "kinds", method = RequestMethod.GET)
    public RespPager<Page<AppKind>> getKinds(@RequestParam String appId, @RequestParam(required = false) String kind,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer limit) {
        AppKind appKind = new AppKind(appId, kind);
        Page<AppKind> kinds = appService.getKindsWithPage(appKind, page, limit);
        return new RespPager<>(kinds);
    }

    @SuppressWarnings("unchecked")
    @ResponseBody
    @RequestMapping(value = "kinds", method = RequestMethod.POST)
    public Resp<String> saveKindHandler(@RequestBody Map<String, Object> kindHandler) {
        String appId = kindHandler.get("appId").toString();
        String kind = kindHandler.get("kind").toString();
        List<String> handlers = (ArrayList<String>) kindHandler.get("handlers");
        appService.saveKindHandlers(appId, kind, handlers);
        return Resp.SUCCESS;
    }

    @ResponseBody
    @RequestMapping(value = "kinds/{kindId}", method = RequestMethod.GET)
    public Resp<Map<String, Object>> getKindHandler(@PathVariable String kindId) {
        Map<String, Object> result = appService.getKindHandlerById(kindId);
        return new Resp<>(result);
    }

    @SuppressWarnings("unchecked")
    @ResponseBody
    @RequestMapping(value = "kinds/{kindId}", method = RequestMethod.PUT)
    public Resp<String> updateKindHandler(@PathVariable String kindId, @RequestBody Map<String, Object> kindHandler) {
        String kind = kindHandler.get("kind").toString();
        AppKind appKind = new AppKind(kindId, null, kind);
        List<String> handlers = (ArrayList<String>) kindHandler.get("handlers");
        appService.updateKindHandlers(appKind, handlers);
        return Resp.SUCCESS;
    }

    @ResponseBody
    @RequestMapping(value = "kinds/{kindId}", method = RequestMethod.DELETE)
    public Resp<String> deleteKindHandler(@PathVariable String kindId) {
        appService.deleteKindHandlers(kindId);
        return Resp.SUCCESS;
    }

    @RequestMapping(value = "user",method = RequestMethod.GET)
    public ModelAndView user(ModelAndView modelAndView) {
        modelAndView.setViewName("frame/config/config_user");
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "users", method = RequestMethod.GET)
    public RespPager<Page<UserAgentGroupVo>> getUsers(@RequestParam(required = false) String uname,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer limit) {
        Page<UserAgentGroupVo> users = userService.selectUserWithPage(uname, page, limit);
        return new RespPager<>(limit, users.getTotal(), users);
    }

    @ResponseBody
    @RequestMapping(value = "users/select", method = RequestMethod.GET)
    public List<Map<String, Object>> selectUsers(@RequestParam(required = false) String uname) {
        List<UserAgentGroupVo> users = userService.selectUser(uname);
        return users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", user.getUname());
            map.put("value", user.getUserId());
            return map;
        }).collect(Collectors.toList());
    }

    @ResponseBody
    @RequestMapping(value = "users", method = RequestMethod.POST)
    public Resp<String> saveUser(@RequestBody UserAgentGroupVo userGroup) {
        User user = new User(userGroup.getUname(), DigestUtils.md5DigestAsHex(userGroup.getPassword().getBytes()));
        List<String> groups = null;
        if (StringUtils.isNotEmpty(userGroup.getGroups())) {
            groups = Arrays.asList(userGroup.getGroups().split(","));
        }
        userService.saveUserGroup(user, groups);
        return Resp.SUCCESS;
    }

    @ResponseBody
    @RequestMapping(value = "users/{userId}", method = RequestMethod.GET)
    public Resp<UserAgentGroupVo> getUserById(@PathVariable String userId) {
        UserAgentGroupVo vo = userService.selectUserGroupById(userId);
        return new Resp<>(vo);
    }

    @ResponseBody
    @RequestMapping(value = "users/{userId}", method = RequestMethod.PUT)
    public Resp<String> updateUser(@PathVariable Long userId, @RequestBody UserAgentGroupVo vo) {
        User user = new User();
        user.setId(userId);
        user.setUname(vo.getUname());
        List<String> groups = null;
        if (StringUtils.isNotEmpty(vo.getGroupIds())) {
            groups = Arrays.asList(vo.getGroupIds().split(","));
        }
        userService.updateUserGroup(user, groups);
        return Resp.SUCCESS;
    }

    @ResponseBody
    @RequestMapping(value = "users/{userId}", method = RequestMethod.DELETE)
    public Resp<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUserGroup(userId);
        return Resp.SUCCESS;
    }

    @ResponseBody
    @PostMapping("users/{userId}/change-pwd")
    public Resp<String> changePassword(@PathVariable Long userId, @RequestBody User user) {
        userService.modifyPassword(userId, DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        return Resp.SUCCESS;
    }

}
