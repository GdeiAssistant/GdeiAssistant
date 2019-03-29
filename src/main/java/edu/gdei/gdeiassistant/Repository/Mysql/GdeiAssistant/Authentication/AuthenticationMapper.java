package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Authentication;

import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface AuthenticationMapper {

    @Select("select * from authentication where username=#{username} and is_deleted=0")
    @Results(id = "Authentication", value = {
            @Result(property = "identityCode", column = "identity_code"),
            @Result(property = "username", column = "username"),
            @Result(property = "realname", column = "realname"),
            @Result(property = "identityNumber", column = "identity_number"),
            @Result(property = "schoolNumber", column = "school_number"),
            @Result(property = "method", column = "method", jdbcType = JdbcType.TINYINT, javaType = Integer.class)
    })
    public Authentication selectAuthentication(String username);

    @Insert("insert into authentication (identity_code,username,realname,identity_number,school_number" +
            ",gmt_create,gmt_modified,method,is_deleted)" +
            " values (#{identityCode},#{username},#{realname},#{identityNumber},#{schoolNumber}" +
            ",now(),now(),#{method},0)")
    public void insertAuthentication(Authentication authentication);

    @Update("update authentication set realname=#{realname},identity_number=#{identityNumber},school_number=#{schoolNumber}" +
            ",gmt_modified=now(),method=#{method},is_deleted=0 where username=#{username}")
    public void updateAuthentication(Authentication authentication);

    @Update("update authentication set identity_code=null,realname=null,identity_number=null,school_number=null,gmt_modified=now()" +
            ",method=-1,is_deleted=1 where username=#{username}")
    public void deleteAuthentication(String username);
}
