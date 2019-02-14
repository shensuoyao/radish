package org.sam.shen.scheduing.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.mapper.AppInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author clock
 * @date 2019/1/30 下午2:22
 */
@Service
public class AppService {

    @Resource
    private AppInfoMapper appInfoMapper;

    public Page<AppInfo> getAppsWithPage(String appName, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        return appInfoMapper.selectApps(appName);
    }

    public AppInfo getAppById(String appId) {
        return appInfoMapper.selectAppInfoById(appId);
    }

    public String saveApp(AppInfo appInfo) {
        String appId = appInfo.getAppId();
        if (StringUtils.isEmpty(appId)) {
            appId = UUID.randomUUID().toString().replaceAll("-", "");
            appInfo.setAppId(appId);
        }
        int flag = appInfoMapper.insertAppInfo(appInfo);
        if (flag == 1) {
            return appId;
        }
        return null;
    }

    public boolean deleteApp(String appId) {
        int flag = appInfoMapper.deleteAppInfoById(appId);
        return flag == 1;
    }

    public boolean updateApp(AppInfo appInfo) {
        int flag = appInfoMapper.updateAppInfoById(appInfo);
        return flag == 1;
    }

}
