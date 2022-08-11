package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy;

import cn.gdeiassistant.Pojo.Entity.Privacy;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

public interface PrivacyMapper {
    @Select("select * from privacy where username=#{username}")
    @Results(id = "Privacy", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "genderOpen", column = "is_gender_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "locationOpen", column = "is_location_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "hometownOpen", column = "is_hometown_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "introductionOpen", column = "is_introduction_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "facultyOpen", column = "is_faculty_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "majorOpen", column = "is_major_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "enrollmentOpen", column = "is_enrollment_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "ageOpen", column = "is_age_open", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "cacheAllow", column = "is_cache_allow", javaType = Boolean.class, jdbcType = JdbcType.TINYINT),
            @Result(property = "robotsIndexAllow", column = "is_robots_index_allow", javaType = Boolean.class, jdbcType = JdbcType.TINYINT)
    })
    Privacy selectPrivacy(String username);

    @Insert("insert into privacy (username,is_cache_allow) values(#{username},false)")
    void initPrivacy(String username);

    @Update("update privacy set is_cache_allow=0 where username=#{username}")
    void resetPrivacy(String username);

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
    void updateGender(@Param("genderOpen") Boolean gender, @Param("username") String username);

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
    void updateFaculty(@Param("facultyOpen") Boolean faculty
            , @Param("username") String username);

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
    void updateMajor(@Param("majorOpen") Boolean major
            , @Param("username") String username);

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
    void updateLocation(@Param("locationOpen") Boolean location, @Param("username") String username);

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
    void updateHometown(@Param("hometownOpen") Boolean hometown, @Param("username") String username);

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
    void updateIntroduction(@Param("introductionOpen") Boolean introduction
            , @Param("username") String username);

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
    void updateEnrollment(@Param("enrollment") Boolean enrollment, @Param("username") String username);

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
    void updateAge(@Param("age") Boolean age, @Param("username") String username);

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
    void updateCache(@Param("cacheAllow") Boolean cache, @Param("username") String username)
           ;

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
    void updateRobotsIndex(@Param("robotsIndexAllow") Boolean robotsIndex, @Param("username") String username);
}
