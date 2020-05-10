package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Dating;

import edu.gdei.gdeiassistant.Pojo.Entity.DatingMessage;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingPick;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingProfile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Deprecated
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
    public List<DatingProfile> selectDatingProfilePage(@Param("start") Integer start
            , @Param("size") Integer size, @Param("area") Integer area);

    @Select("select * from dating_profile where profile_id=#{profileId} limit 1")
    @ResultMap("DatingProfile")
    public DatingProfile selectDatingProfileById(Integer profileId);

    @Select("select * from dating_profile where username=#{username}")
    @ResultMap("DatingProfile")
    public List<DatingProfile> selectDatingProfileByUsername(String username);

    @Update("update dating_profile set nickname=#{nickname},grade=#{grade},faculty=#{faculty},hometown=#{hometown}" +
            ",condition=#{condition},qq=#{qq},wechat=#{wechat} where profile_id=#{profileId}")
    public void updateDatingProfile(DatingProfile datingProfile);

    @Update("update dating_profile set state=#{state} where profile_id=#{profileId}")
    public void updateDatingProfileState(@Param("profileId") Integer profileId, @Param("state") Integer state);

    @Update("insert into dating_profile (username,nickname,area,grade,faculty,hometown,content,qq,wechat,state)" +
            "values (#{username},#{nickname},#{area},#{grade},#{faculty},#{hometown},#{content},#{qq},#{wechat},1)")
    @Options(useGeneratedKeys = true, keyProperty = "profileId")
    public Integer insertDatingProfile(DatingProfile datingProfile);

    @Select("select dating_pick.pick_id as datingPickPickId,dating_profile.profile_id as datingProfileProfileId," +
            "dating_pick.username as datingPickUsername,dating_pick.content as datingPickContent," +
            "dating_pick.state as datingPickState,dating_profile.username as datingProfileUsername" +
            "from dating_pick,dating_profile where dating_pick.profile_id=dating_profile.profile_id and pick_id=#{pickId} limit 1")
    @Results(id = "DatingPick", value = {
            @Result(property = "pickId", column = "datingPickPickId"),
            @Result(property = "username", column = "datingPickUsername"),
            @Result(property = "content", column = "datingPickContent"),
            @Result(property = "state", column = "datingPickState"),
            @Result(property = "datingProfile.profileId", column = "profile_id"),
            @Result(property = "datingProfile.username", column = "username"),
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
    public DatingPick selectDatingPickById(Integer pickId);

    @Select("select * from dating_pick where username=#{username} and profile_id=#{profileId} order by pick_id desc limit 1")
    @ResultType(DatingPick.class)
    public DatingPick selectDatingPick(@Param("profileId") Integer profileId, @Param("username") String username);

    @Insert("insert into dating_pick (profile_id,username,content,state) values (#{datingProfile.profileId},#{username},#{content},0)")
    @Options(useGeneratedKeys = true, keyProperty = "pickId")
    public Integer insertDatingPick(DatingPick datingPick);

    @Update("update dating_pick set state=#{state} where pick_id=#{pickId}")
    public void updateDatingPickState(@Param("pickId") Integer pickId, @Param("state") Integer state);

    @Select("select message_id as datingMessageMessageId,dating_message.username as datingMessageUsername,type as datingMessageType,dating_message.state as datingMessageState," +
            "dating_pick.pick_id as datingPickPickId,dating_pick.username as datingPickUsername,dating_pick.content as datingPickContent,dating_pick.state as datingPickState," +
            "dating_profile.username as datingProfileUsername,dating_profile.nickname as datingProfileNickname,dating_Profile.profile_id as datingProfileProfileId " +
            "from dating_message,dating_pick,dating_profile " +
            "where dating_message.pick_id=dating_pick.pick_id and dating_pick.profile_id=dating_profile.profile_id " +
            "and dating_message.username=#{username} limit #{start},#{size}")
    @Results(id = "DatingMessage", value = {
            @Result(property = "messageId", column = "datingMessageMessageId"),
            @Result(property = "username", column = "datingMessageUsername"),
            @Result(property = "type", column = "datingMessageType"),
            @Result(property = "state", column = "datingMessageState"),
            @Result(property = "pickId", column = "datingPickPickId"),
            @Result(property = "username", column = "datingPickUsername"),
            @Result(property = "content", column = "datingPickContent"),
            @Result(property = "state", column = "datingPickState"),
            @Result(property = "datingPick.profileId", column = "profile_id"),
            @Result(property = "datingPick.username", column = "username"),
            @Result(property = "datingPick.nickname", column = "nickname"),
            @Result(property = "datingPick.grade", column = "grade"),
            @Result(property = "datingPick.faculty", column = "faculty"),
            @Result(property = "datingPick.hometown", column = "hometown"),
            @Result(property = "datingPick.content", column = "content"),
            @Result(property = "datingPick.qq", column = "qq"),
            @Result(property = "datingPick.wechat", column = "wechat"),
            @Result(property = "datingPick.area", column = "area"),
            @Result(property = "datingPick.state", column = "state")
    })
    public List<DatingMessage> selectUserDatingMessagePage(@Param("username") String username
            , @Param("start") Integer start, @Param("size") Integer size);

    @Select("select count(message_id) from dating_message where username=#{username} and state=0")
    @ResultType(Integer.class)
    public Integer selectUserUnReadDatingMessageCount(String username);

    @Insert("insert into dating_message (username,pick_id,type,state) values(#{username},#{datingPick.pickId},#{type},#{state})")
    public void insertDatingMessage(DatingMessage datingMessage);

    @Update("update dating_message set state=#{state} where message_id=#{messageId}")
    public void updateDatingMessageState(@Param("messageId") Integer messageId, @Param("state") Integer state);
}
