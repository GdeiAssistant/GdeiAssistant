package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy;

import com.linguancheng.gdeiassistant.Pojo.Entity.Privacy;
import org.apache.ibatis.annotations.Param;

public interface PrivacyMapper {

    public Privacy selectPrivacy(String username) throws Exception;

    public void initPrivacy(String username) throws Exception;

    public void resetPrivacy(String username) throws Exception;

    public void updateGender(@Param("genderOpen") boolean gender, @Param("username") String username) throws Exception;

    public void updateGenderOrientation(@Param("genderOrientationOpen") boolean genderOrientation
            , @Param("username") String username) throws Exception;

    public void updateFaculty(@Param("facultyOpen") boolean faculty
            , @Param("username") String username) throws Exception;

    public void updateMajor(@Param("majorOpen") boolean major
            , @Param("username") String username) throws Exception;

    public void updateRegion(@Param("regionOpen") boolean region
            , @Param("username") String username) throws Exception;

    public void updateIntroduction(@Param("introductionOpen") boolean introduction
            , @Param("username") String username) throws Exception;

    public void updateCache(@Param("cacheAllow") boolean cache, @Param("username") String username)
            throws Exception;
}
