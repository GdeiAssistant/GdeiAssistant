package cn.gdeiassistant.core.roommate.mapper;

import cn.gdeiassistant.core.roommate.pojo.entity.RoommateMessageEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommatePickEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommateProfileEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RoommateMapper {

    @Select("select * from dating_profile where state=1 and area=#{area} limit #{start},#{size}")
    @Results(id = "RoommateProfile", value = {
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
    List<RoommateProfileEntity> selectRoommateProfilePage(@Param("start") Integer start
            , @Param("size") Integer size, @Param("area") Integer area);

    @Select("select * from dating_profile where profile_id=#{profileId} limit 1")
    @ResultMap("RoommateProfile")
    RoommateProfileEntity selectRoommateProfileById(Integer profileId);

    @Select("select * from dating_profile where username=#{username}")
    @ResultMap("RoommateProfile")
    List<RoommateProfileEntity> selectRoommateProfileByUsername(String username);

    @Update("update dating_profile set nickname=#{nickname},grade=#{grade},faculty=#{faculty},hometown=#{hometown}" +
            ",content=#{content},qq=#{qq},wechat=#{wechat} where profile_id=#{profileId}")
    void updateRoommateProfile(RoommateProfileEntity entity);

    @Update("update dating_profile set state=#{state} where profile_id=#{profileId}")
    void updateRoommateProfileState(@Param("profileId") Integer profileId, @Param("state") Integer state);

    @Insert("insert into dating_profile (username,nickname,area,grade,faculty,hometown,content,qq,wechat,state)" +
            "values (#{username},#{nickname},#{area},#{grade},#{faculty},#{hometown},#{content},#{qq},#{wechat},1)")
    @Options(useGeneratedKeys = true, keyProperty = "profileId")
    void insertRoommateProfile(RoommateProfileEntity entity);

    @Select("select dating_pick.pick_id as datingPickPickId,dating_profile.profile_id as datingProfileProfileId," +
            "dating_pick.username as datingPickUsername,dating_pick.content as datingPickContent," +
            "dating_pick.state as datingPickState,dating_profile.username as datingProfileUsername " +
            "from dating_pick,dating_profile where dating_pick.profile_id=dating_profile.profile_id and pick_id=#{pickId} limit 1")
    @Results(id = "RoommatePick", value = {
            @Result(property = "pickId", column = "datingPickPickId"),
            @Result(property = "username", column = "datingPickUsername"),
            @Result(property = "content", column = "datingPickContent"),
            @Result(property = "state", column = "datingPickState"),
            @Result(property = "roommateProfile.profileId", column = "datingProfileProfileId"),
            @Result(property = "roommateProfile.username", column = "datingProfileUsername"),
            @Result(property = "roommateProfile.nickname", column = "nickname"),
            @Result(property = "roommateProfile.grade", column = "grade"),
            @Result(property = "roommateProfile.faculty", column = "faculty"),
            @Result(property = "roommateProfile.hometown", column = "hometown"),
            @Result(property = "roommateProfile.content", column = "content"),
            @Result(property = "roommateProfile.qq", column = "qq"),
            @Result(property = "roommateProfile.wechat", column = "wechat"),
            @Result(property = "roommateProfile.area", column = "area"),
            @Result(property = "roommateProfile.state", column = "state")
    })
    RoommatePickEntity selectRoommatePickById(Integer pickId);

    @Select("select * from dating_pick where username=#{username} and profile_id=#{profileId} order by pick_id desc limit 1")
    @Results(id = "RoommatePickSimple", value = {
            @Result(property = "pickId", column = "pick_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "content", column = "content"),
            @Result(property = "state", column = "state"),
            @Result(property = "roommateProfile.profileId", column = "profile_id")
    })
    RoommatePickEntity selectRoommatePick(@Param("profileId") Integer profileId, @Param("username") String username);

    @Insert("insert into dating_pick (profile_id,username,content,state) values (#{roommateProfile.profileId},#{username},#{content},0)")
    @Options(useGeneratedKeys = true, keyProperty = "pickId")
    void insertRoommatePick(RoommatePickEntity entity);

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
            @Result(property = "roommateProfile.profileId", column = "profile_id"),
            @Result(property = "roommateProfile.username", column = "profile_username"),
            @Result(property = "roommateProfile.nickname", column = "nickname"),
            @Result(property = "roommateProfile.grade", column = "grade"),
            @Result(property = "roommateProfile.faculty", column = "faculty"),
            @Result(property = "roommateProfile.hometown", column = "hometown"),
            @Result(property = "roommateProfile.content", column = "profile_content"),
            @Result(property = "roommateProfile.qq", column = "qq"),
            @Result(property = "roommateProfile.wechat", column = "wechat"),
            @Result(property = "roommateProfile.area", column = "area"),
            @Result(property = "roommateProfile.state", column = "profile_state")
    })
    List<RoommatePickEntity> selectRoommatePickListByUsername(@Param("username") String username);

    @Select("select message_id as datingMessageMessageId,dating_message.username as datingMessageUsername,type as datingMessageType,dating_message.state as datingMessageState,dating_message.create_time as datingMessageCreateTime," +
            "dating_pick.pick_id as datingPickPickId,dating_pick.username as datingPickUsername,dating_pick.content as datingPickContent,dating_pick.state as datingPickState," +
            "dating_profile.username as datingProfileUsername,dating_profile.nickname as datingProfileNickname,dating_profile.profile_id as datingProfileProfileId " +
            "from dating_message,dating_pick,dating_profile " +
            "where dating_message.pick_id=dating_pick.pick_id and dating_pick.profile_id=dating_profile.profile_id " +
            "and dating_message.username=#{username} order by dating_message.create_time desc, dating_message.message_id desc limit #{start},#{size}")
    @Results(id = "RoommateMessage", value = {
            @Result(property = "messageId", column = "datingMessageMessageId"),
            @Result(property = "username", column = "datingMessageUsername"),
            @Result(property = "type", column = "datingMessageType"),
            @Result(property = "state", column = "datingMessageState"),
            @Result(property = "createTime", column = "datingMessageCreateTime"),
            @Result(property = "roommatePick.pickId", column = "datingPickPickId"),
            @Result(property = "roommatePick.username", column = "datingPickUsername"),
            @Result(property = "roommatePick.content", column = "datingPickContent"),
            @Result(property = "roommatePick.state", column = "datingPickState"),
            @Result(property = "roommatePick.roommateProfile.profileId", column = "datingProfileProfileId"),
            @Result(property = "roommatePick.roommateProfile.username", column = "datingProfileUsername"),
            @Result(property = "roommatePick.roommateProfile.nickname", column = "datingProfileNickname"),
            @Result(property = "roommatePick.roommateProfile.grade", column = "grade"),
            @Result(property = "roommatePick.roommateProfile.faculty", column = "faculty"),
            @Result(property = "roommatePick.roommateProfile.hometown", column = "hometown"),
            @Result(property = "roommatePick.roommateProfile.content", column = "content"),
            @Result(property = "roommatePick.roommateProfile.qq", column = "qq"),
            @Result(property = "roommatePick.roommateProfile.wechat", column = "wechat"),
            @Result(property = "roommatePick.roommateProfile.area", column = "area"),
            @Result(property = "roommatePick.roommateProfile.state", column = "state")
    })
    List<RoommateMessageEntity> selectUserRoommateMessagePage(@Param("username") String username
            , @Param("start") Integer start, @Param("size") Integer size);

    /**
     * interaction 聚合接口专用分页查询。
     */
    @Select("select message_id as datingMessageMessageId,dating_message.username as datingMessageUsername,type as datingMessageType,dating_message.state as datingMessageState,dating_message.create_time as datingMessageCreateTime," +
            "dating_pick.pick_id as datingPickPickId,dating_pick.username as datingPickUsername,dating_pick.content as datingPickContent,dating_pick.state as datingPickState," +
            "dating_profile.username as datingProfileUsername,dating_profile.nickname as datingProfileNickname,dating_profile.profile_id as datingProfileProfileId " +
            "from dating_message,dating_pick,dating_profile " +
            "where dating_message.pick_id=dating_pick.pick_id and dating_pick.profile_id=dating_profile.profile_id " +
            "and dating_message.username=#{username} order by dating_message.create_time desc, dating_message.message_id desc limit #{start},#{size}")
    @ResultMap("RoommateMessage")
    List<RoommateMessageEntity> selectUserRoommateMessageInteractionPage(@Param("username") String username,
            @Param("start") Integer start, @Param("size") Integer size);

    @Select("select count(message_id) from dating_message where username=#{username} and state=0")
    @ResultType(Integer.class)
    Integer selectUserUnReadRoommateMessageCount(String username);

    @Insert("insert into dating_message (username,pick_id,type,state,create_time) values(#{username},#{roommatePick.pickId},#{type},#{state},now())")
    void insertRoommateMessage(RoommateMessageEntity entity);

    @Update("update dating_message set state=#{state} where message_id=#{messageId}")
    void updateRoommateMessageState(@Param("messageId") Integer messageId, @Param("state") Integer state);
}
