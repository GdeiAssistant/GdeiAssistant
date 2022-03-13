package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Phone;

import cn.gdeiassistant.Pojo.Alias.DataEncryption;
import cn.gdeiassistant.Pojo.Entity.Phone;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface PhoneMapper {

    @Select("select * from phone where username=#{username}")
    @Results(id = "Phone", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "code", column = "code"),
            @Result(property = "phone", column = "phone", javaType = DataEncryption.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "createTime", column = "gmt_create"),
            @Result(property = "updateTime", column = "gmt_modified")
    })
    public Phone selectPhone(String username);

    @Insert("insert into phone (username,code,phone,gmt_create,gmt_modified) values(#{username},#{code},#{phone,javaType=encryption, jdbcType=VARCHAR},now(),now())")
    public void insertPhone(@Param("username") String username, @Param("code") Integer code, @Param("phone") String phone);

    @Update("update phone set code=#{code},phone=#{phone,javaType=encryption, jdbcType=VARCHAR},gmt_modified=now() where username=#{username}")
    public void updatePhone(Phone phone);

    @Delete("delete from phone where username=#{username}")
    public void deletePhone(String username);
}
