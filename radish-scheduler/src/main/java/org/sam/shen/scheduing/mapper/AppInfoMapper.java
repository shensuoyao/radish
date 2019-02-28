package org.sam.shen.scheduing.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sam.shen.scheduing.entity.AppInfo;

import java.util.List;

/**
 * @author clock
 * @date 2019/1/8 上午10:49
 */
@Mapper
public interface AppInfoMapper {

    Page<AppInfo> selectApps(@Param("appName") String appName, @Param("userId") Long userId);

    AppInfo selectAppInfoById(String appId);

    int insertAppInfo(AppInfo appInfo);

    int updateAppInfoById(AppInfo appInfo);

    int batchInsert(List<AppInfo> list);

    int deleteAppInfoById(String appId);
}
