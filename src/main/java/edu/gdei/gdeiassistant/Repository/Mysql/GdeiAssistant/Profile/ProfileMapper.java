package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile;

import edu.gdei.gdeiassistant.Pojo.Entity.Introduction;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import org.apache.ibatis.annotations.Param;

public interface ProfileMapper {

    public Profile selectUserProfile(String username) throws Exception;

    public Introduction selectUserIntroduction(String username) throws Exception;

    public void initUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    public void initUserIntroduction(String username) throws Exception;

    public void updateUserProfile(Profile profile) throws Exception;

    public void resetUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    public void resetUserIntroduction(@Param("username") String username);

    public void updateUserIntroduction(@Param("username") String username, @Param("introduction") String introduction) throws Exception;

}
