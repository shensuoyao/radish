package org.sam.shen.scheduing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sam.shen.scheduing.entity.AppKindHandler;
import org.sam.shen.scheduing.vo.AppKindHandlerVo;

import java.util.List;

/**
 * @author clock
 * @date 2019/2/18 下午1:50
 */
@Mapper
public interface AppKindHandlerMapper {

    List<AppKindHandlerVo> selectKindHandler(String kindId);

    int insertKindHandler(AppKindHandler appKindHandler);

    int batchInsert(List<AppKindHandler> list);

    int deleteKindHandler(String kindId);
}
