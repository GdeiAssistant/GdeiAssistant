package cn.gdeiassistant.core.user.mapper;

import cn.gdeiassistant.common.pojo.Alias.DataEncryption;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface UserMapper {

    @Select("select * from user where username=#{username} limit 1")
    @Results(id = "UserEntity", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password", javaType = DataEncryption.class, jdbcType = JdbcType.VARCHAR)
    })
    UserEntity selectUser(String username);

    @Select("select count(username) from user where username like concat(concat('%',#{username}),'%')")
    @ResultType(Integer.class)
    Integer selectDeletedUserCount(String username);

    @Insert("insert into user (username,password) values (#{username},#{password,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR})")
    void insertUser(UserEntity user);

    @Update("update user set password=#{password,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR} where username=#{username}")
    void updateUser(UserEntity user);

    @Update("update user set username=#{resetname},password=null where username=#{username}")
    void closeUser(@Param("resetname") String resetname, @Param("username") String username);
}
