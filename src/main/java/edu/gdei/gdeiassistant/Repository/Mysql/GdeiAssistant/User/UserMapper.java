package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.User;

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
    public User selectUser(String username) throws Exception;

    @Select("select count(username) from user where username like concat(concat('%',#{username}),'%')")
    @ResultType(Integer.class)
    public Integer selectDeletedUserCount(String username) throws Exception;

    @Select("select * from user where state!=-1 order by username limit #{start},#{size}")
    @ResultMap("User")
    public List<User> selectUserList(@Param("start") int start, @Param("size") int size) throws Exception;

    @Select("select * from user where state!=-1")
    @ResultMap("User")
    public List<User> selectAllUser() throws Exception;

    @Select("select count(username) from user where state!=-1;")
    @ResultType(Integer.class)
    public Integer selectUserCount() throws Exception;

    @Insert("insert into user (username,password,state,user_group) values (#{username},#{password},0,1)")
    public void insertUser(User user) throws Exception;

    @Update("update user set password=#{password},state=0 where username=#{username}")
    public void updateUser(User user) throws Exception;

    @Update("update user set username=#{resetname},password=null,keycode=null,number=null,state=-1 where username=#{username}")
    public void closeUser(@Param("resetname") String resetname, @Param("username") String username) throws Exception;

    @Update("update user set group=#{group} where username=#{username}")
    public void updateUserGroup(@Param("username") String username, @Param("group") Integer group);
}
