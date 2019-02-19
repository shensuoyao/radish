package org.sam.shen.scheduing.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.sam.shen.scheduing.entity.AppInfo;
import org.sam.shen.scheduing.entity.AppKind;
import org.sam.shen.scheduing.entity.AppKindHandler;
import org.sam.shen.scheduing.mapper.AppInfoMapper;
import org.sam.shen.scheduing.mapper.AppKindHandlerMapper;
import org.sam.shen.scheduing.mapper.AppKindMapper;
import org.sam.shen.scheduing.vo.AppKindHandlerVo;
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

    @Resource
    private AppKindHandlerMapper appKindHandlerMapper;

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

    public Page<AppKind> getKindsWithPage(AppKind appKind, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        return appKindMapper.selectAppKind(appKind);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveKindHandlers(String appId, String kind, List<String> handlers) {
        String kindId = UUID.randomUUID().toString().replaceAll("-", "");
        AppKind appKind = new AppKind(kindId, appId, kind);
        appKindMapper.insertAppKind(appKind);
        if (handlers.size() < 1) {
            return;
        }
        List<AppKindHandler> kindHandlers = new ArrayList<>();
        for (String handler : handlers) {
            String kindHandlerId = UUID.randomUUID().toString().replaceAll("-", "");
            AppKindHandler appKindHandler = new AppKindHandler(kindHandlerId, kindId, handler);
            kindHandlers.add(appKindHandler);
        }
        appKindHandlerMapper.batchInsert(kindHandlers);
    }

    public Map<String, Object> getKindHandlerById(String kindId) {
        List<AppKindHandlerVo> handlers = appKindHandlerMapper.selectKindHandler(kindId);
        if (handlers == null || handlers.size() < 1) {
            return null;
        }
        String kind = handlers.get(0).getKind();
        List<String> handlerIdArr = new ArrayList<>();
        for (AppKindHandlerVo vo : handlers) {
            if (StringUtils.isNotEmpty(vo.getHandlerId())) {
                handlerIdArr.add(vo.getHandlerId());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("kindId", kindId);
        result.put("kind", kind);
        result.put("handlers", handlerIdArr);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateKindHandlers(AppKind appKind, List<String> handlers) {
        appKindMapper.updateAppKind(appKind);
        appKindHandlerMapper.deleteKindHandler(appKind.getId());
        if (handlers.size() < 1) {
            return;
        }
        List<AppKindHandler> kindHandlers = new ArrayList<>();
        for (String handler : handlers) {
            String kindHandlerId = UUID.randomUUID().toString().replaceAll("-", "");
            AppKindHandler appKindHandler = new AppKindHandler(kindHandlerId, appKind.getId(), handler);
            kindHandlers.add(appKindHandler);
        }
        appKindHandlerMapper.batchInsert(kindHandlers);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteKindHandlers(String kindId) {
        appKindMapper.deleteAppKind(kindId);
        appKindHandlerMapper.deleteKindHandler(kindId);
    }

}
