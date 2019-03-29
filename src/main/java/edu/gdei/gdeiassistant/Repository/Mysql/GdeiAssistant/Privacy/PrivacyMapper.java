package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy;

import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import org.apache.ibatis.annotations.*;

public interface PrivacyMapper {

    @Select("select * from privacy where username=#{username}")
    @Results(id = "Privacy", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "genderOpen", column = "is_gender_open"),
            @Result(property = "genderOrientationOpen", column = "is_gender_orientation_open"),
            @Result(property = "regionOpen", column = "is_region_open"),
            @Result(property = "introductionOpen", column = "is_introduction_open"),
            @Result(property = "facultyOpen", column = "is_faculty_open"),
            @Result(property = "majorOpen", column = "is_major_open"),
            @Result(property = "cacheAllow", column = "is_cache_allow")
    })
    public Privacy selectPrivacy(String username) throws Exception;

    @Insert("insert into privacy (username,is_gender_open,is_gender_orientation_open,is_faculty_open" +
            ",is_major_open,is_region_open,is_introduction_open,is_cache_allow) " +
            "values(#{username},true,true,true,true,true,true,true)")
    public void initPrivacy(String username) throws Exception;

    @Update("update privacy set is_gender_open=1,is_gender_orientation_open=1,is_faculty_open=1,is_major_open=1" +
            ",is_region_open=1,is_introduction_open=1,is_cache_allow=1 where username=#{username}")
    public void resetPrivacy(String username) throws Exception;

    @Update("update privacy" +
            "        <choose>" +
            "            <when test='genderOpen'>" +
            "                set is_gender_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_gender_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}")
    public void updateGender(@Param("genderOpen") boolean gender, @Param("username") String username) throws Exception;

    @Update("update privacy" +
            "        <choose>" +
            "            <when test='genderOrientationOpen'>" +
            "                set is_gender_orientation_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_gender_orientation_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}")
    public void updateGenderOrientation(@Param("genderOrientationOpen") boolean genderOrientation
            , @Param("username") String username) throws Exception;

    @Update("update privacy" +
            "        <choose>" +
            "            <when test='facultyOpen'>" +
            "                set is_faculty_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_faculty_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}")
    public void updateFaculty(@Param("facultyOpen") boolean faculty
            , @Param("username") String username) throws Exception;

    @Update("update privacy" +
            "        <choose>" +
            "            <when test='majorOpen'>" +
            "                set is_major_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_major_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}")
    public void updateMajor(@Param("majorOpen") boolean major
            , @Param("username") String username) throws Exception;

    @Update("update privacy" +
            "        <choose>" +
            "            <when test='regionOpen'>" +
            "                set is_region_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_region_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}")
    public void updateRegion(@Param("regionOpen") boolean region
            , @Param("username") String username) throws Exception;

    @Update("update privacy" +
            "        <choose>" +
            "            <when test='introductionOpen'>" +
            "                set is_introduction_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_introduction_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}")
    public void updateIntroduction(@Param("introductionOpen") boolean introduction
            , @Param("username") String username) throws Exception;

    @Update("update privacy" +
            "        <choose>" +
            "            <when test='cacheAllow'>" +
            "                set is_cache_allow='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_cache_allow='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}")
    public void updateCache(@Param("cacheAllow") boolean cache, @Param("username") String username)
            throws Exception;
}
