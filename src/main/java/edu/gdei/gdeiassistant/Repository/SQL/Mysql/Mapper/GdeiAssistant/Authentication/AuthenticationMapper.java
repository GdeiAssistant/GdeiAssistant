package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Authentication;

import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface AuthenticationMapper {

    @Select("select identity_code,username,type from authentication where username=#{username}")
    @Results(id = "Authentication", value = {
            @Result(property = "identityCode", column = "identity_code"),
            @Result(property = "username", column = "username"),
            @Result(property = "type", column = "type", jdbcType = JdbcType.TINYINT, javaType = Integer.class)
    })
    public Authentication selectAuthentication(String username);

    @Insert("insert into authentication (identity_code,salt,username,gmt_create,gmt_modified,type)" +
            " values (#{identityCode},#{salt},#{username},now(),now(),#{type})")
    public void insertAuthentication(Authentication authentication);

    @Update("update authentication set identity_code=#{identityCode},salt=#{salt},gmt_modified=now(),type=#{type} where username=#{username}")
    public void updateAuthentication(Authentication authentication);

    @Delete("delete from authentication where username=#{username}")
    public void deleteAuthentication(String username);
}
