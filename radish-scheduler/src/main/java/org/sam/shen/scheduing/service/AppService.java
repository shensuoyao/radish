package org.sam.shen.scheduing.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.entity.AppKind;
import org.sam.shen.scheduing.mapper.AppInfoMapper;
import org.sam.shen.scheduing.mapper.AppKindMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author clock
 * @date 2019/1/30 下午2:22
 */
@Service
public class AppService {

    @Resource
    private AppInfoMapper appInfoMapper;

    @Resource
    private AppKindMapper appKindMapper;

    public Page<AppInfo> getAppsWithPage(String appName, Integer page, Integer pageSize, Long userId) {
        PageHelper.startPage(page, pageSize);
        return appInfoMapper.selectApps(appName, userId);
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

    @Transactional(rollbackFor = Exception.class)
    public void deleteApp(String appId) {
        appInfoMapper.deleteAppInfoById(appId);
    }

    public boolean updateApp(AppInfo appInfo) {
        int flag = appInfoMapper.updateAppInfoById(appInfo);
        return flag == 1;
    }

    public Page<AppKind> getKindsWithPage(AppKind appKind, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        return appKindMapper.selectAppKind(appKind);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveKindHandlers(String appId, String kind, String handlers) {
        String kindId = UUID.randomUUID().toString().replaceAll("-", "");
        AppKind appKind = new AppKind(kindId, appId, kind, handlers);
        appKindMapper.insertAppKind(appKind);
    }

    public AppKind getAppKindById(String kindId) {
        return appKindMapper.selectById(kindId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateKindHandlers(AppKind appKind) {
        appKindMapper.updateAppKind(appKind);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteKindHandlers(String kindId) {
        appKindMapper.deleteAppKind(kindId);
    }

}
