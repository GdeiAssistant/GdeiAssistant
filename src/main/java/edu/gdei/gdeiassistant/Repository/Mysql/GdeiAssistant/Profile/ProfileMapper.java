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
            @Result(property = "region", column = "region"),
            @Result(property = "state", column = "state"),
            @Result(property = "city", column = "city")
    })
    public Profile selectUserProfile(String username) throws Exception;

    @Select("select * from introduction where username=#{username} limit 1")
    @Results(id = "Introduction", value = {
            @Result(property = "introductionContent", column = "introduction"),
            @Result(property = "username", column = "username")
    })
    public Introduction selectUserIntroduction(String username) throws Exception;

    @Insert("insert into profile (username,kickname) values (#{username},#{kickname})")
    public void initUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    @Insert("insert into introduction (username) values (#{username})")
    public void initUserIntroduction(String username) throws Exception;

    @Update("update profile" +
            "        <set>" +
            "            <if test='kickname!=null'>" +
            "                kickname=#{kickname}," +
            "            </if>" +
            "            <if test='gender!=null'>" +
            "                gender=#{gender}," +
            "            </if>" +
            "            <if test='genderOrientation!=null'>" +
            "                gender_orientation=#{genderOrientation}," +
            "            </if>" +
            "            <if test='faculty!=null'>" +
            "                faculty=#{faculty}," +
            "            </if>" +
            "            <if test='major!=null'>" +
            "                major=#{major}," +
            "            </if>" +
            "            <if test='region!=null'>" +
            "                region=#{region}," +
            "            </if>" +
            "            <if test='state!=null'>" +
            "                state=#{state}," +
            "            </if>" +
            "            <if test='city!=null'>" +
            "                city=#{city}," +
            "            </if>" +
            "        </set>" +
            "        where username=#{username}")
    public void updateUserProfile(Profile profile) throws Exception;

    @Update("update profile set kickname=#{kickname},gender=null,gender_orientation=null,region=null,state=null,city=null where username=#{username}")
    public void resetUserProfile(@Param("username") String username, @Param("kickname") String kickname) throws Exception;

    @Update("update introduction set introduction=null where username=#{username}")
    public void resetUserIntroduction(@Param("username") String username);

    @Update("update introduction set introduction=#{introduction} where username=#{username}")
    public void updateUserIntroduction(@Param("username") String username, @Param("introduction") String introduction) throws Exception;

}
