package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy;

import com.linguancheng.gdeiassistant.Pojo.Entity.Privacy;
import org.apache.ibatis.annotations.Param;

public interface PrivacyMapper {

    public Privacy selectPrivacy(String username) throws Exception;

    public void initPrivacy(String username) throws Exception;

    public void resetPrivacy(String username) throws Exception;

    public void updateGender(@Param("gender") boolean gender, @Param("username") String username) throws Exception;

    public void updateGenderOrientation(@Param("genderOrientation") boolean genderOrientation
            , @Param("username") String username) throws Exception;

    public void updateFaculty(@Param("faculty") boolean faculty
            , @Param("username") String username) throws Exception;

    public void updateMajor(@Param("major") boolean major
            , @Param("username") String username) throws Exception;

    public void updateRegion(@Param("region") boolean region
            , @Param("username") String username) throws Exception;

    public void updateIntroduction(@Param("introduction") boolean introduction
            , @Param("username") String username) throws Exception;

    public void updateCache(@Param("cache") boolean cache, @Param("username") String username)
            throws Exception;
}
