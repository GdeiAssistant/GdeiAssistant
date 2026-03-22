package cn.gdeiassistant.core.dating.mapper;

import cn.gdeiassistant.core.dating.pojo.entity.DatingPickEntity;
import cn.gdeiassistant.core.dating.pojo.entity.DatingProfileEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DatingMapper {

    @Select("select * from dating_profile where state=1 and area=#{area} limit #{start},#{size}")
    @Results(id = "DatingProfile", value = {
            @Result(property = "profileId", column = "profile_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "grade", column = "grade"),
            @Result(property = "faculty", column = "faculty"),
            @Result(property = "hometown", column = "hometown"),
            @Result(property = "content", column = "content"),
            @Result(property = "qq", column = "qq"),
            @Result(property = "wechat", column = "wechat"),
            @Result(property = "area", column = "area"),
            @Result(property = "state", column = "state")
    })
    List<DatingProfileEntity> selectDatingProfilePage(@Param("start") Integer start
            , @Param("size") Integer size, @Param("area") Integer area);

    @Select("select * from dating_profile where profile_id=#{profileId} limit 1")
    @ResultMap("DatingProfile")
    DatingProfileEntity selectDatingProfileById(Integer profileId);

    @Select("select * from dating_profile where username=#{username}")
    @ResultMap("DatingProfile")
    List<DatingProfileEntity> selectDatingProfileByUsername(String username);

    @Update("update dating_profile set nickname=#{nickname},grade=#{grade},faculty=#{faculty},hometown=#{hometown}" +
            ",content=#{content},qq=#{qq},wechat=#{wechat} where profile_id=#{profileId}")
    void updateRoommateProfile(DatingProfileEntity entity);

    @Update("update dating_profile set state=#{state} where profile_id=#{profileId}")
    void updateRoommateProfileState(@Param("profileId") Integer profileId, @Param("state") Integer state);

    @Insert("insert into dating_profile (username,nickname,area,grade,faculty,hometown,content,qq,wechat,state)" +
            "values (#{username},#{nickname},#{area},#{grade},#{faculty},#{hometown},#{content},#{qq},#{wechat},1)")
    @Options(useGeneratedKeys = true, keyProperty = "profileId")
    void insertRoommateProfile(DatingProfileEntity entity);

    @Select("select dating_pick.pick_id as datingPickPickId,dating_profile.profile_id as datingProfileProfileId," +
            "dating_pick.username as datingPickUsername,dating_pick.content as datingPickContent," +
            "dating_pick.state as datingPickState,dating_profile.username as datingProfileUsername " +
            "from dating_pick,dating_profile where dating_pick.profile_id=dating_profile.profile_id and pick_id=#{pickId} limit 1")
    @Results(id = "DatingPick", value = {
            @Result(property = "pickId", column = "datingPickPickId"),
            @Result(property = "username", column = "datingPickUsername"),
            @Result(property = "content", column = "datingPickContent"),
            @Result(property = "state", column = "datingPickState"),
            @Result(property = "datingProfile.profileId", column = "datingProfileProfileId"),
            @Result(property = "datingProfile.username", column = "datingProfileUsername"),
            @Result(property = "datingProfile.nickname", column = "nickname"),
            @Result(property = "datingProfile.grade", column = "grade"),
            @Result(property = "datingProfile.faculty", column = "faculty"),
            @Result(property = "datingProfile.hometown", column = "hometown"),
            @Result(property = "datingProfile.content", column = "content"),
            @Result(property = "datingProfile.qq", column = "qq"),
            @Result(property = "datingProfile.wechat", column = "wechat"),
            @Result(property = "datingProfile.area", column = "area"),
            @Result(property = "datingProfile.state", column = "state")
    })
    DatingPickEntity selectDatingPickById(Integer pickId);

    @Select("select * from dating_pick where username=#{username} and profile_id=#{profileId} order by pick_id desc limit 1")
    @Results(id = "RoommatePickSimple", value = {
            @Result(property = "pickId", column = "pick_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "content", column = "content"),
            @Result(property = "state", column = "state"),
            @Result(property = "datingProfile.profileId", column = "profile_id")
    })
    DatingPickEntity selectDatingPick(@Param("profileId") Integer profileId, @Param("username") String username);

    @Insert("insert into dating_pick (profile_id,username,content,state) values (#{datingProfile.profileId},#{username},#{content},0)")
    @Options(useGeneratedKeys = true, keyProperty = "pickId")
    void insertRoommatePick(DatingPickEntity entity);

    @Update("update dating_pick set state=#{state} where pick_id=#{pickId}")
    void updateRoommatePickState(@Param("pickId") Integer pickId, @Param("state") Integer state);

    @Select("select p.pick_id as pick_id,p.username as pick_username,p.content as pick_content,p.state as pick_state," +
            "d.profile_id as profile_id,d.username as profile_username,d.nickname,d.grade,d.faculty,d.hometown," +
            "d.content as profile_content,d.qq,d.wechat,d.area,d.state as profile_state " +
            "from dating_pick p inner join dating_profile d on p.profile_id=d.profile_id " +
            "where p.username=#{username} order by p.pick_id desc")
    @Results(id = "RoommatePickSent", value = {
            @Result(property = "pickId", column = "pick_id"),
            @Result(property = "username", column = "pick_username"),
            @Result(property = "content", column = "pick_content"),
            @Result(property = "state", column = "pick_state"),
            @Result(property = "datingProfile.profileId", column = "profile_id"),
            @Result(property = "datingProfile.username", column = "profile_username"),
            @Result(property = "datingProfile.nickname", column = "nickname"),
            @Result(property = "datingProfile.grade", column = "grade"),
            @Result(property = "datingProfile.faculty", column = "faculty"),
            @Result(property = "datingProfile.hometown", column = "hometown"),
            @Result(property = "datingProfile.content", column = "profile_content"),
            @Result(property = "datingProfile.qq", column = "qq"),
            @Result(property = "datingProfile.wechat", column = "wechat"),
            @Result(property = "datingProfile.area", column = "area"),
            @Result(property = "datingProfile.state", column = "profile_state")
    })
    List<DatingPickEntity> selectDatingPickListByUsername(@Param("username") String username);

    @Select("select p.pick_id as pick_id,p.username as pick_username,p.content as pick_content,p.state as pick_state," +
            "d.profile_id as profile_id,d.username as profile_username,d.nickname,d.grade,d.faculty,d.hometown," +
            "d.content as profile_content,d.qq,d.wechat,d.area,d.state as profile_state " +
            "from dating_pick p inner join dating_profile d on p.profile_id=d.profile_id " +
            "where d.username=#{username} order by p.pick_id desc")
    @ResultMap("RoommatePickSent")
    List<DatingPickEntity> selectReceivedRoommatePickListByProfileOwner(@Param("username") String username);

    @Delete("delete from dating_profile where profile_id=#{profileId}")
    void deleteRoommateProfile(@Param("profileId") int profileId);
}
