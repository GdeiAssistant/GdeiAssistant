package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.WechatUser;

import org.apache.ibatis.annotations.*;

public interface WechatUserMapper {

    @Select("select username from wechat_user where wechat_id=#{wechatId} limit 1")
    @ResultType(String.class)
    String selectUsername(@Param("wechatId") String wechatID) ;

    @Insert("insert into wechat_user (wechat_id,username) values (#{wechatId},#{username})")
    void insertWechatUser(@Param("wechatId") String wechatID, @Param("username") String username);

    @Update("update wechat_user set username=#{username} where wechat_id=#{wechatId}")
    void updateWechatUser(@Param("wechatId") String wechatID, @Param("username") String username);

    @Delete("delete from wechat_user where username=#{username}")
    void resetWechatUser(String username);
}
