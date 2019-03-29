package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Access;

import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

public interface AccessMapper {

    @Select("select * from access where id=#{groupId}")
    @ResultType(Access.class)
    public Access selectAccess(Integer groupId) throws Exception;
}
