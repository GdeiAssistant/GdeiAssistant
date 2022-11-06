package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Email;

import cn.gdeiassistant.Pojo.Alias.DataEncryption;
import cn.gdeiassistant.Pojo.Entity.Email;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface EmailMapper {

    @Select("select * from email where username=#{username}")
    @Results(id = "Email", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "email", column = "email", javaType = DataEncryption.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "gmt_create"),
            @Result(property = "updateTime", column = "gmt_modified")
    })
    Email selectEmail(String username);

    @Insert("insert into email (username,email,gmt_create,gmt_modified) values(#{username},#{email,javaType=encryption, jdbcType=VARCHAR},now(),now())")
    void insertEmail(@Param("username") String username, @Param("email") String email);

    @Update("update email email=#{email,javaType=encryption, jdbcType=VARCHAR},gmt_modified=now() where username=#{username}")
    void updateEmail(Email email);

    @Delete("delete from email where username=#{username}")
    void deleteEmail(String username);
}
