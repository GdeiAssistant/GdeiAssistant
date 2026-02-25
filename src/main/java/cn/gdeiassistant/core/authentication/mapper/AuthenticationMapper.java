package cn.gdeiassistant.core.authentication.mapper;

import cn.gdeiassistant.common.pojo.Alias.DataEncryption;
import cn.gdeiassistant.common.pojo.Entity.Authentication;
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

    @Insert("insert into authentication (username,type,number,name,gmt_create,gmt_modified) values(#{username},#{type},#{number,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR},#{name,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR},now(),now())")
    void insertAuthentication(@Param("username") String username, @Param("type") Integer type
            , @Param("number") String number, @Param("name") String name);

    @Delete("delete from authentication where username=#{username}")
    void deleteAuthentication(String username);
}
