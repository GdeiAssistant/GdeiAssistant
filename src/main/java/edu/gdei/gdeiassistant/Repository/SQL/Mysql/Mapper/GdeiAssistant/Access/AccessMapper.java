package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Access;

import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AccessMapper {

    @Select("select * from access")
    @Results(id = "Access", value = {
            @Result(property = "name", column = "name"),
            @Result(property = "group", column = "user_group"),
    })
    List<Access> selectAllAccess() throws Exception;

    @Select("select * from access where user_group=#{groupId}")
    @ResultMap("Access")
    List<Access> selectAccess(Integer groupId) throws Exception;
}
