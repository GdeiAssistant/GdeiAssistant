package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User;

import cn.gdeiassistant.Pojo.Alias.DataEncryption;
import cn.gdeiassistant.Pojo.Entity.User;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface UserMapper {

    @Select("select * from user where username=#{username} limit 1")
    @Results(id = "User", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password", javaType = DataEncryption.class, jdbcType = JdbcType.VARCHAR)
    })
    User selectUser(String username) ;

    @Select("select count(username) from user where username like concat(concat('%',#{username}),'%')")
    @ResultType(Integer.class)
    Integer selectDeletedUserCount(String username) ;

    @Insert("insert into user (username,password) values (#{username},#{password,javaType=encryption, jdbcType=VARCHAR})")
    void insertUser(User user) ;

    @Update("update user set password=#{password,javaType=encryption, jdbcType=VARCHAR} where username=#{username}")
    void updateUser(User user) ;

    @Update("update user set username=#{resetname},password=null where username=#{username}")
    void closeUser(@Param("resetname") String resetname, @Param("username") String username);
}
