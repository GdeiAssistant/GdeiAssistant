package cn.gdeiassistant.core.phone.mapper;

import cn.gdeiassistant.common.pojo.Alias.DataEncryption;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
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
    PhoneEntity selectPhone(String username);

    @Insert("insert into phone (username,code,phone,gmt_create,gmt_modified) values(#{username},#{code},#{phone,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR},now(),now())")
    void insertPhone(@Param("username") String username, @Param("code") Integer code, @Param("phone") String phone);

    @Update("update phone set code=#{code},phone=#{phone,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR},gmt_modified=now() where username=#{username}")
    void updatePhone(PhoneEntity entity);

    @Delete("delete from phone where username=#{username}")
    void deletePhone(String username);
}
