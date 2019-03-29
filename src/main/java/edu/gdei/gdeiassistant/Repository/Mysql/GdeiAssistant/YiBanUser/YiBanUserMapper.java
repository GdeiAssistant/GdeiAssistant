package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.YiBanUser;

import org.apache.ibatis.annotations.*;

public interface YiBanUserMapper {

    @Select("select username from yiban_user where user_id=#{userid} limit 1")
    @ResultType(String.class)
    public String selectUsername(int userid) throws Exception;

    @Insert("insert into yiban_user (user_id,username) values (#{userid},#{username})")
    public void insertYiBanUser(@Param("userid") int userid, @Param("username") String username) throws Exception;

    @Update("update yiban_user set username=#{username} where user_id=#{userid}")
    public void updateYiBanUser(@Param("userid") int userid, @Param("username") String username) throws Exception;

    @Delete("delete from yiban_user where username=#{username}")
    public void resetYiBanUser(String username);
}
