package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User;

import edu.gdei.gdeiassistant.Pojo.Entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {

    @Select("select * from user where username=#{username} limit 1")
    @Results(id = "User", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "state", column = "state"),
            @Result(property = "group", column = "user_group")
    })
    User selectUser(String username) throws Exception;

    @Select("select count(username) from user where username like concat(concat('%',#{username}),'%')")
    @ResultType(Integer.class)
    Integer selectDeletedUserCount(String username) throws Exception;

    @Select("select count(id) from user_group")
    @ResultType(Integer.class)
    Integer selectUserGroupCount() throws Exception;

    @Select("select * from user where state!=2 order by username limit #{start},#{size}")
    @ResultMap("User")
    List<User> selectUserList(@Param("start") int start, @Param("size") int size) throws Exception;

    @Select("select * from user where state!=2")
    @ResultMap("User")
    List<User> selectAllUser() throws Exception;

    @Select("select count(username) from user where state!=2;")
    @ResultType(Integer.class)
    Integer selectUserCount() throws Exception;

    @Insert("insert into user (username,password,state,user_group) values (#{username},#{password},1,2)")
    void insertUser(User user) throws Exception;

    @Update("update user set password=#{password},state=1 where username=#{username}")
    void updateUser(User user) throws Exception;

    @Update("update user set username=#{resetname},password=null,state=2 where username=#{username}")
    void closeUser(@Param("resetname") String resetname, @Param("username") String username) throws Exception;

    @Update("update user set user_group=#{group} where username=#{username}")
    void updateUserGroup(@Param("username") String username, @Param("group") Integer group);
}
