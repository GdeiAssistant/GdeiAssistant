package com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.Introduction;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.Profile;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProfileMapper {

    public List<User> selectUnauthenticatedUser() throws Exception;

    public Profile selectBaseProfile(String username) throws Exception;

    public Profile selectUserProfile(String username) throws Exception;

    public Introduction selectUserIntroduction(String username) throws Exception;

    public void updateRealName(@Param("username") String username, @Param("realname") String realname) throws Exception;

    public void initUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    public void initUserIntroduction(String username) throws Exception;

    public void updateUserProfile(Profile profile) throws Exception;

    public void resetUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    public void resetUserIntroduction(@Param("username") String username);

    public void updateUserIntroduction(@Param("username") String username, @Param("introduction") String introduction) throws Exception;

}
