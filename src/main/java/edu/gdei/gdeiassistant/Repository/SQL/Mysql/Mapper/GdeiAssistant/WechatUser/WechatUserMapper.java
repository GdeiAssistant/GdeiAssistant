package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.WechatUser;

import org.apache.ibatis.annotations.*;

public interface WechatUserMapper {

    @Select("select username from wechat_user where wechat_id=#{wechatId} limit 1")
    @ResultType(String.class)
    public String selectUsername(@Param("wechatId") String wechatID) throws Exception;

    @Insert("insert into wechat_user (wechat_id,username) values (#{wechatId},#{username})")
    public void insertWechatUser(@Param("wechatId") String wechatID, @Param("username") String username) throws Exception;

    @Update("update wechat_user set username=#{username} where wechat_id=#{wechatId}")
    public void updateWechatUser(@Param("wechatId") String wechatID, @Param("username") String username) throws Exception;

    @Delete("delete from wechat_user where username=#{username}")
    public void resetWechatUser(String username);
}
