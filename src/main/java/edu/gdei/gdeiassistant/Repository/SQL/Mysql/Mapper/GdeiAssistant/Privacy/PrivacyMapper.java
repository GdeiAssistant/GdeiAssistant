package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy;

import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import org.apache.ibatis.annotations.*;

public interface PrivacyMapper {

    @Select("select * from privacy where username=#{username}")
    @Results(id = "Privacy", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "genderOpen", column = "is_gender_open"),
            @Result(property = "locationOpen", column = "is_location_open"),
            @Result(property = "hometownOpen", column = "is_hometown_open"),
            @Result(property = "introductionOpen", column = "is_introduction_open"),
            @Result(property = "facultyOpen", column = "is_faculty_open"),
            @Result(property = "majorOpen", column = "is_major_open"),
            @Result(property = "enrollmentOpen", column = "is_enrollment_open"),
            @Result(property = "ageOpen", column = "is_age_open"),
            @Result(property = "degreeOpen", column = "is_degree_open"),
            @Result(property = "professionOpen", column = "is_profession_open"),
            @Result(property = "primarySchoolOpen", column = "is_primary_school_open"),
            @Result(property = "juniorHighSchoolOpen", column = "is_junior_high_school_open"),
            @Result(property = "highSchoolOpen", column = "is_high_school_open"),
            @Result(property = "cacheAllow", column = "is_cache_allow"),
            @Result(property = "robotsIndexAllow", column = "is_robots_index_allow")
    })
    public Privacy selectPrivacy(String username) throws Exception;

    @Insert("insert into privacy (username,is_gender_open,is_faculty_open" +
            ",is_major_open,is_enrollment_open,is_age_open,is_degree_open,is_profession_open,is_primary_school_open,is_junior_high_school_open,is_high_school_open" +
            ",is_location_open,is_hometown_open,is_introduction_open,is_cache_allow,is_robots_index_allow) " +
            "values(#{username},true,true,true,true,true,true,true,true,true,true,true,true,true,false,true)")
    public void initPrivacy(String username) throws Exception;

    @Update("update privacy set is_gender_open=1,is_faculty_open=1,is_major_open=1,is_enrollment_open=1,is_age_open,is_degree_open,is_primary_school_open=1" +
            ",is_junior_high_school_open=1,is_high_school_open=1,is_location_open=1,is_hometown_open=1,is_introduction_open=1,is_cache_allow=0,is_robots_index_allow=1 where username=#{username}")
    public void resetPrivacy(String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='genderOpen'>" +
            "                set is_gender_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_gender_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateGender(@Param("genderOpen") boolean gender, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='facultyOpen'>" +
            "                set is_faculty_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_faculty_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateFaculty(@Param("facultyOpen") boolean faculty
            , @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='majorOpen'>" +
            "                set is_major_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_major_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateMajor(@Param("majorOpen") boolean major
            , @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='locationOpen'>" +
            "                set is_location_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_location_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}"
            + "</script>")
    public void updateLocation(@Param("locationOpen") boolean location, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='hometownOpen'>" +
            "                set is_hometown_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_hometown_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}"
            + "</script>")
    public void updateHometown(@Param("hometownOpen") boolean hometown, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='introductionOpen'>" +
            "                set is_introduction_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_introduction_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateIntroduction(@Param("introductionOpen") boolean introduction
            , @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='enrollment'>" +
            "                set is_enrollment_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_enrollment_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateEnrollment(@Param("enrollment") boolean enrollment, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='age'>" +
            "                set is_age_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_age_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateAge(@Param("age") boolean age, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='degree'>" +
            "                set is_degree_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_degree_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateDegree(@Param("degree") boolean degree, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='profession'>" +
            "                set is_profession_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_profession_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateProfession(@Param("profession") boolean profession, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='primarySchool'>" +
            "                set is_primary_school_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_primary_school_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updatePrimarySchool(@Param("primarySchool") boolean primarySchool, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='juniorHighSchool'>" +
            "                set is_junior_high_school_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_junior_high_school_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateJuniorHighSchool(@Param("juniorHighSchool") boolean juniorHighSchool, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='highSchool'>" +
            "                set is_high_school_open='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_high_school_open='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateHighSchool(@Param("highSchool") boolean highSchool, @Param("username") String username) throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='cacheAllow'>" +
            "                set is_cache_allow='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_cache_allow='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateCache(@Param("cacheAllow") boolean cache, @Param("username") String username)
            throws Exception;

    @Update("<script>" +
            "update privacy" +
            "        <choose>" +
            "            <when test='robotsIndexAllow'>" +
            "                set is_robots_index_allow='1'" +
            "            </when>" +
            "            <otherwise>" +
            "                set is_robots_index_allow='0'" +
            "            </otherwise>" +
            "        </choose>" +
            "        where username=#{username}" +
            "</script>")
    public void updateRobotsIndex(@Param("robotsIndexAllow") boolean robotsIndex, @Param("username") String username) throws Exception;
}
