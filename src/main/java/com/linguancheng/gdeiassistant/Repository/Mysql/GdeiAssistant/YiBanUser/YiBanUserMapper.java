package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.YiBanUser;

import org.apache.ibatis.annotations.Param;

public interface YiBanUserMapper {

    public String selectUsername(int userid) throws Exception;

    public void insertYiBanUser(@Param("userid") int userid, @Param("username") String username) throws Exception;

    public void updateYiBanUser(@Param("userid") int userid, @Param("username") String username) throws Exception;

    public void resetYiBanUser(String username);
}
