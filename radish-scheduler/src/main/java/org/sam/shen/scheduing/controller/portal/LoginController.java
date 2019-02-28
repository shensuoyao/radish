package org.sam.shen.scheduing.controller.portal;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.User;
import org.sam.shen.scheduing.mapper.UserMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author clock
 * @date 2019/2/25 下午3:31
 */
@Controller
public class LoginController {

    @Resource
    private UserMapper userMapper;

    @RequestMapping(value = {"", "/", "/login"}, method = RequestMethod.GET)
    public ModelAndView loginView(ModelAndView model) {
        model.setViewName("login");
        return model;
    }

    @ResponseBody
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public Resp<String> login(@RequestBody User user, HttpSession session) {
        if (StringUtils.isEmpty(user.getUname())|| StringUtils.isEmpty(user.getPassword())) {
            return new Resp<>(Resp.FAIL.getCode(), "用户名和密码不能为空！");
        }
        User u = userMapper.login(user.getUname(), user.getPassword());
        if (u == null) {
            return new Resp<>(Resp.FAIL.getCode(), "用户名或密码错误！");
        }
        session.setAttribute("user", u);
        return Resp.SUCCESS;
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public ModelAndView logout(ModelAndView modelAndView, HttpSession session) {
        session.invalidate();
        modelAndView.setViewName("login");
        return modelAndView;
    }

}
