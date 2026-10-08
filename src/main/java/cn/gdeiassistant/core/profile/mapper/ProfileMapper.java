package cn.gdeiassistant.core.profile.mapper;

import cn.gdeiassistant.common.pojo.entity.Introduction;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;


public interface ProfileMapper {

    @Select("select * from profile where username=#{username} limit 1")
    @Results(id = "Profile", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "birthday", column = "birthday", javaType = Date.class, jdbcType = JdbcType.DATE),
            @Result(property = "degree", column = "degree"),
            @Result(property = "faculty", column = "faculty"),
            @Result(property = "major", column = "major"),
            @Result(property = "enrollment", column = "enrollment"),
            @Result(property = "profession", column = "profession"),
            @Result(property = "colleges", column = "colleges"),
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
    ProfileEntity selectUserProfile(String username);

    @Select("select * from introduction where username=#{username} limit 1")
    @Results(id = "Introduction", value = {
            @Result(property = "introductionContent", column = "introduction"),
            @Result(property = "username", column = "username")
    })
    Introduction selectUserIntroduction(String username);

    @Insert("insert into profile (username,nickname) values (#{username},#{nickname})")
    void initUserProfile(@Param("username") String username, @Param("nickname") String nickname);

    @Insert("insert into introduction (username) values (#{username})")
    void initUserIntroduction(String username);

    @Update("update profile set nickname=#{nickname} where username=#{username}")
    void updateNickname(ProfileEntity profile);

    @Update("update profile set birthday=#{birthday} where username=#{username}")
    void updateBirthday(ProfileEntity profile);

    @Update("update profile set faculty=#{faculty} where username=#{username}")
    void updateFaculty(ProfileEntity profile);

    @Update("update profile set major=#{major} where username=#{username}")
    void updateMajor(ProfileEntity profile);

    @Update("update profile set degree=#{degree} where username=#{username}")
    void updateDegree(ProfileEntity profile);

    @Update("update profile set enrollment=#{enrollment} where username=#{username}")
    void updateEnrollment(ProfileEntity profile);

    @Update("update profile set profession=#{profession} where username=#{username}")
    void updateProfession(ProfileEntity profile);

    @Update("update profile set location_region=#{locationRegion},location_state=#{locationState},location_city=#{locationCity} where username=#{username}")
    void updateLocation(ProfileEntity profile);

    @Update("update profile set hometown_region=#{hometownRegion},hometown_state=#{hometownState},hometown_city=#{hometownCity} where username=#{username}")
    void updateHometown(ProfileEntity profile);

    @Update("update profile set colleges=#{colleges} where username=#{username}")
    void updateColleges(ProfileEntity profile);

    @Update("update profile set high_school=#{highSchool} where username=#{username}")
    void updateHighSchool(ProfileEntity profile);

    @Update("update profile set junior_high_school=#{juniorHighSchool} where username=#{username}")
    void updateJuniorHighSchool(ProfileEntity profile);

    @Update("update profile set primary_school=#{primarySchool} where username=#{username}")
    void updatePrimarySchool(ProfileEntity profile);

    @Update("update profile set nickname=#{nickname},birthday=null,enrollment=null,profession=null,location_region=null" +
            ",location_state=null,location_city=null,hometown_region=null,hometown_state=null,hometown_city=null,high_school=null,junior_high_school=null,primary_school=null where username=#{username}")
    void resetUserProfile(@Param("username") String username, @Param("nickname") String nickname);

    @Update("update introduction set introduction=null where username=#{username}")
    void resetUserIntroduction(@Param("username") String username);

    @Update("update introduction set introduction=#{introduction} where username=#{username}")
    void updateUserIntroduction(@Param("username") String username, @Param("introduction") String introduction);
    @Update({"<script>update profile <set>",
            "<if test=\"patch.containsKey('nickname')\">nickname=#{patch.nickname},</if>",
            "<if test=\"patch.containsKey('birthday')\">birthday=#{patch.birthday},</if>",
            "<if test=\"patch.containsKey('faculty')\">faculty=#{patch.faculty},</if>",
            "<if test=\"patch.containsKey('major')\">major=#{patch.major},</if>",
            "<if test=\"patch.containsKey('enrollment')\">enrollment=#{patch.enrollment},</if>",
            "<if test=\"patch.containsKey('locationRegion')\">location_region=#{patch.locationRegion},</if>",
            "<if test=\"patch.containsKey('locationState')\">location_state=#{patch.locationState},</if>",
            "<if test=\"patch.containsKey('locationCity')\">location_city=#{patch.locationCity},</if>",
            "<if test=\"patch.containsKey('hometownRegion')\">hometown_region=#{patch.hometownRegion},</if>",
            "<if test=\"patch.containsKey('hometownState')\">hometown_state=#{patch.hometownState},</if>",
            "<if test=\"patch.containsKey('hometownCity')\">hometown_city=#{patch.hometownCity},</if>",
            "</set> where username=#{username}</script>"})
    int updateProfilePatch(@Param("username") String username, @Param("patch") java.util.Map<String, Object> patch);

    @Select("select * from profile where username=#{username} for update")
    @ResultMap("Profile")
    ProfileEntity lockUserProfile(@Param("username") String username);

    @Insert("insert into introduction(username,introduction) values(#{username},#{content}) " +
            "on duplicate key update introduction=#{content}")
    void saveIntroduction(@Param("username") String username, @Param("content") String content);
}
