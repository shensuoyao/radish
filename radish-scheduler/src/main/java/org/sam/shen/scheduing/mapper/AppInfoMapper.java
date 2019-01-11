package org.sam.shen.scheduing.mapper;

import org.sam.shen.scheduing.entity.AppInfo;

import java.util.List;

/**
 * @author clock
 * @date 2019/1/8 上午10:49
 */
public interface AppInfoMapper {

    AppInfo selectAppInfoById(String appId);

    int insertAppInfo(AppInfo appInfo);

    int updateAppInfoById(AppInfo appInfo);

    int batchInsert(List<AppInfo> list);

    int deleteAppInfoById(String appId);
}
