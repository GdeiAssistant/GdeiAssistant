package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User;

import cn.gdeiassistant.Pojo.Entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {

    @Select("select * from user where username=#{username} limit 1")
    @Results(id = "User", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password")
    })
    User selectUser(String username) throws Exception;

    @Select("select count(username) from user where username like concat(concat('%',#{username}),'%')")
    @ResultType(Integer.class)
    Integer selectDeletedUserCount(String username) throws Exception;

    @Select("select * from user order by username limit #{start},#{size}")
    @ResultMap("User")
    List<User> selectUserList(@Param("start") int start, @Param("size") int size) throws Exception;

    @Select("select * from user")
    @ResultMap("User")
    List<User> selectAllUser() throws Exception;

    @Insert("insert into user (username,password) values (#{username},#{password})")
    void insertUser(User user) throws Exception;

    @Update("update user set password=#{password} where username=#{username}")
    void updateUser(User user) throws Exception;

    @Update("update user set username=#{resetname},password=null where username=#{username}")
    void closeUser(@Param("resetname") String resetname, @Param("username") String username) throws Exception;
}
