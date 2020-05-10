package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile;

import edu.gdei.gdeiassistant.Pojo.Entity.Introduction;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

public interface ProfileMapper {

    @Select("select * from profile where username=#{username} limit 1")
    @Results(id = "Profile", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "birthday", column = "birthday", javaType = Date.class, jdbcType = JdbcType.DATE),
            @Result(property = "degree", column = "degree"),
            @Result(property = "faculty", column = "faculty"),
            @Result(property = "major", column = "major"),
            @Result(property = "enrollment", column = "enrollment"),
            @Result(property = "profession", column = "profession"),
            @Result(property = "highSchool", column = "high_school"),
            @Result(property = "juniorHighSchool", column = "junior_high_school"),
            @Result(property = "primarySchool", column = "primary_school"),
            @Result(property = "locationRegion", column = "location_region"),
            @Result(property = "locationState", column = "location_state"),
            @Result(property = "locationCity", column = "location_city"),
            @Result(property = "hometownRegion", column = "hometown_region"),
            @Result(property = "hometownState", column = "hometown_state"),
            @Result(property = "hometownCity", column = "hometown_city")
    })
    Profile selectUserProfile(String username) throws Exception;

    @Select("select * from introduction where username=#{username} limit 1")
    @Results(id = "Introduction", value = {
            @Result(property = "introductionContent", column = "introduction"),
            @Result(property = "username", column = "username")
    })
    Introduction selectUserIntroduction(String username) throws Exception;

    @Insert("insert into profile (username,nickname) values (#{username},#{nickname})")
    void initUserProfile(@Param("username") String username, @Param("nickname") String nickname) throws Exception;

    @Insert("insert into introduction (username) values (#{username})")
    void initUserIntroduction(String username) throws Exception;

    @Update("update profile set nickname=#{nickname} where username=#{username}")
    void updateNickname(Profile profile);

    @Update("update profile set gender=#{gender} where username=#{username}")
    void updateGender(Profile profile);

    @Update("update profile set birthday=#{birthday} where username=#{username}")
    void updateBirthday(Profile profile);

    @Update("update profile set faculty=#{faculty} where username=#{username}")
    void updateFaculty(Profile profile);

    @Update("update profile set major=#{major} where username=#{username}")
    void updateMajor(Profile profile);

    @Update("update profile set degree=#{degree} where username=#{username}")
    void updateDegree(Profile profile);

    @Update("update profile set enrollment=#{enrollment} where username=#{username}")
    void updateEnrollment(Profile profile);

    @Update("update profile set profession=#{profession} where username=#{username}")
    void updateProfession(Profile profile);

    @Update("update profile set location_region=#{locationRegion},location_state=#{locationState},location_city=#{locationCity} where username=#{username}")
    void updateLocation(Profile profile);

    @Update("update profile set hometown_region=#{hometownRegion},hometown_state=#{hometownState},hometown_city=#{hometownCity} where username=#{username}")
    void updateHometown(Profile profile);

    @Update("update profile set high_school=#{highSchool} where username=#{username}")
    void updateHighSchool(Profile profile);

    @Update("update profile set junior_high_school=#{juniorHighSchool} where username=#{username}")
    void updateJuniorHighSchool(Profile profile);

    @Update("update profile set primary_school=#{primarySchool} where username=#{username}")
    void updatePrimarySchool(Profile profile);

    @Update("update profile set nickname=#{nickname},gender=null,birthday=null,enrollment=null,profession=null,location_region=null" +
            ",location_state=null,location_city=null,hometown_region=null,hometown_state=null,hometown_city=null,high_school=null,junior_high_school=null,primary_school=null where username=#{username}")
    void resetUserProfile(@Param("username") String username, @Param("nickname") String nickname) throws Exception;

    @Update("update introduction set introduction=null where username=#{username}")
    void resetUserIntroduction(@Param("username") String username);

    @Update("update introduction set introduction=#{introduction} where username=#{username}")
    void updateUserIntroduction(@Param("username") String username, @Param("introduction") String introduction) throws Exception;
}
