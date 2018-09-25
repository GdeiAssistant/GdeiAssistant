package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.WechatUser;

import org.apache.ibatis.annotations.Param;

public interface WechatUserMapper {

    public String selectUsername(@Param("wechatId") String wechatID) throws Exception;

    public void insertWechatUser(@Param("wechatId") String wechatID, @Param("username") String username) throws Exception;

    public void updateWechatUser(@Param("wechatId") String wechatID, @Param("username") String username) throws Exception;

    public void resetWechatUser(String username);
}
