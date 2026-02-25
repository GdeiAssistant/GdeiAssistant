package cn.gdeiassistant.core.cetquery.mapper;

import cn.gdeiassistant.core.cetquery.pojo.entity.CetNumberEntity;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

/**
 * 提供 CetNumberEntity 的 ResultMap 供 AppDataMapper 使用；实际读写仍可用 core.cet.mapper.CetMapper。
 */
public interface CetQueryMapper {

    @Select("select username, number from cet where username=#{username}")
    @Results(id = "CetNumberEntity", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "number", column = "number")
    })
    CetNumberEntity selectCetNumber(String username);
}
