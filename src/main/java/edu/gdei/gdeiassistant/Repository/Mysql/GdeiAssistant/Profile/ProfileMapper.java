package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile;

import edu.gdei.gdeiassistant.Pojo.Entity.Introduction;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import org.apache.ibatis.annotations.*;

public interface ProfileMapper {

    @Select("select * from profile where username=#{username} limit 1")
    @Results(id = "Profile", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "kickname", column = "kickname"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "genderOrientation", column = "gender_orientation"),
            @Result(property = "degree", column = "degree"),
            @Result(property = "faculty", column = "faculty"),
            @Result(property = "major", column = "major"),
            @Result(property = "region", column = "region"),
            @Result(property = "state", column = "state"),
            @Result(property = "city", column = "city")
    })
    Profile selectUserProfile(String username) throws Exception;

    @Select("select * from introduction where username=#{username} limit 1")
    @Results(id = "Introduction", value = {
            @Result(property = "introductionContent", column = "introduction"),
            @Result(property = "username", column = "username")
    })
    Introduction selectUserIntroduction(String username) throws Exception;

    @Insert("insert into profile (username,kickname) values (#{username},#{kickname})")
    void initUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    @Insert("insert into introduction (username) values (#{username})")
    void initUserIntroduction(String username) throws Exception;

    @Update("update profile set kickname=#{kickname} where username=#{username}")
    void updateKickname(Profile profile);

    @Update("update profile set gender=#{gender} where username=#{username}")
    void updateGender(Profile profile);

    @Update("update profile set gender_orientation=#{gender_orientation} where username=#{username}")
    void updateGenderOrientation(Profile profile);

    @Update("update profile set faculty=#{faculty} where username=#{username}")
    void updateFaculty(Profile profile);

    @Update("update profile set major=#{major} where username=#{username}")
    void updateMajor(Profile profile);

    @Update("update profile set degree=#{degree} where username=#{username}")
    void updateDegree(Profile profile);

    @Update("update profile set region=#{region},state=#{state},city=#{city} where username=#{username}")
    void updateLocation(Profile profile);

    @Update("update profile set kickname=#{kickname},gender=null,gender_orientation=null,region=null,state=null,city=null where username=#{username}")
    void resetUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    @Update("update introduction set introduction=null where username=#{username}")
    void resetUserIntroduction(@Param("username") String username);

    @Update("update introduction set introduction=#{introduction} where username=#{username}")
    void updateUserIntroduction(@Param("username") String username, @Param("introduction") String introduction) throws Exception;
}
