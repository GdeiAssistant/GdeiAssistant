package cn.gdeiassistant.core.message.mapper;

import cn.gdeiassistant.core.message.pojo.entity.InteractionNotificationEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface InteractionNotificationMapper {

    @Insert("insert into interaction_notification " +
            "(module,type,receiver_username,actor_username,target_id,target_sub_id,target_type,title,content,create_time,is_read) " +
            "values (#{module},#{type},#{receiverUsername},#{actorUsername},#{targetId},#{targetSubId},#{targetType},#{title},#{content},now(),#{isRead})")
    @Options(useGeneratedKeys = true, keyProperty = "notificationId", keyColumn = "notification_id")
    void insertInteractionNotification(InteractionNotificationEntity entity);

    @Select("select notification_id,module,type,receiver_username,actor_username,target_id,target_sub_id,target_type,title,content,create_time,is_read " +
            "from interaction_notification where receiver_username=#{username} " +
            "order by create_time desc, notification_id desc limit #{start},#{size}")
    @Results(id = "InteractionNotification", value = {
            @Result(property = "notificationId", column = "notification_id"),
            @Result(property = "module", column = "module"),
            @Result(property = "type", column = "type"),
            @Result(property = "receiverUsername", column = "receiver_username"),
            @Result(property = "actorUsername", column = "actor_username"),
            @Result(property = "targetId", column = "target_id"),
            @Result(property = "targetSubId", column = "target_sub_id"),
            @Result(property = "targetType", column = "target_type"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "isRead", column = "is_read")
    })
    List<InteractionNotificationEntity> selectInteractionNotificationPage(@Param("username") String username,
            @Param("start") Integer start, @Param("size") Integer size);

    @Select("select count(notification_id) from interaction_notification where receiver_username=#{username} and is_read=0")
    Integer selectUnreadInteractionNotificationCount(@Param("username") String username);

    @Select("select module,target_id,target_sub_id,target_type " +
            "from interaction_notification where receiver_username=#{username} and module in ('dating','roommate')")
    @Results(id = "InteractionNotificationKey", value = {
            @Result(property = "module", column = "module"),
            @Result(property = "targetId", column = "target_id"),
            @Result(property = "targetSubId", column = "target_sub_id"),
            @Result(property = "targetType", column = "target_type")
    })
    List<InteractionNotificationEntity> selectDatingInteractionNotificationKeys(@Param("username") String username);

    @Update("update interaction_notification set is_read=1 where receiver_username=#{username} and notification_id=#{notificationId}")
    int updateInteractionNotificationRead(@Param("username") String username, @Param("notificationId") Long notificationId);

    @Update("update interaction_notification set is_read=1 where receiver_username=#{username} and is_read=0")
    int updateAllInteractionNotificationRead(@Param("username") String username);
}
