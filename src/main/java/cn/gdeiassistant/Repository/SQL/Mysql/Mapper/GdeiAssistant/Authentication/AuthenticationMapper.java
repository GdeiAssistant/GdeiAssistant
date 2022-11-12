package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Authentication;

import cn.gdeiassistant.Pojo.Alias.DataEncryption;
import cn.gdeiassistant.Pojo.Entity.Authentication;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface AuthenticationMapper {

    @Select("select * from authentication where username=#{username}")
    @Results(id = "Authentication", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "type", column = "type"),
            @Result(property = "number", column = "number", javaType = DataEncryption.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "name",column = "name",javaType = DataEncryption.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "gmt_create"),
            @Result(property = "updateTime", column = "gmt_modified")
    })
    Authentication selectAuthentication(String username);

    @Insert("insert into authentication (username,type,number,name,gmt_create,gmt_modified) values(#{username},#{type},#{number,javaType=encryption, jdbcType=VARCHAR},#{name,javaType=encryption, jdbcType=VARCHAR},now(),now())")
    void insertAuthentication(@Param("username") String username, @Param("type") Integer type
            , @Param("number") String number, @Param("name") String name);

    @Delete("delete from authentication where username=#{username}")
    void deleteAuthentication(String username);
}
