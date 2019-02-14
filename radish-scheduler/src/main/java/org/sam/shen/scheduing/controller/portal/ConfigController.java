package org.sam.shen.scheduing.controller.portal;

import com.github.pagehelper.Page;
import org.sam.shen.core.model.Resp;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.entity.RespPager;
import org.sam.shen.scheduing.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


/**
 * @author clock
 * @date 2019/1/29 下午5:50
 */
@Controller
@RequestMapping("config")
public class ConfigController {

    @Autowired
    private AppService appService;

    @RequestMapping(value = {"app", "app/"}, method = RequestMethod.GET)
    public ModelAndView configApp(ModelAndView modelAndView) {
        modelAndView.setViewName("frame/config/config_app");
        return modelAndView;
    }

    @ResponseBody
    @RequestMapping(value = "apps", method = RequestMethod.GET)
    public RespPager<Page<AppInfo>> getApps(@RequestParam(required = false) String appName, @RequestParam(defaultValue = "0") Integer page,
                                           @RequestParam(defaultValue = "10") Integer limit) {
        Page<AppInfo> result = appService.getAppsWithPage(appName, page, limit);
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
    public Resp<String> saveApp(@RequestBody AppInfo appInfo) {
        String appId = appService.saveApp(appInfo);
        return new Resp<>(appId);
    }

    @ResponseBody
    @RequestMapping(value = "apps/{appId}", method = RequestMethod.DELETE)
    public Resp<String> deleteApp(@PathVariable String appId) {
        if (appService.deleteApp(appId)) {
            return Resp.SUCCESS;
        } else {
            return Resp.FAIL;
        }
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

}
