package org.sam.shen.scheduing.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.AppKind;

/**
 * @author clock
 * @date 2019/2/18 下午1:50
 */
@Mapper
public interface AppKindMapper {

    Page<AppKind> selectAppKind(AppKind appKind);

    int insertAppKind(AppKind appKind);

    int updateAppKind(AppKind appKind);

    int deleteAppKind(String id);

}
