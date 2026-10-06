package cn.gdeiassistant.core.social.mapper;

import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SocialRelationMapper {

    @Select("select count(*) from user_follow where follower_id=#{followerId} and followee_id=#{followeeId}")
    int countFollow(@Param("followerId") long followerId, @Param("followeeId") long followeeId);

    @Insert("insert ignore into user_follow (follower_id, followee_id, created_at) values (#{followerId}, #{followeeId}, now())")
    int insertFollow(@Param("followerId") long followerId, @Param("followeeId") long followeeId);

    @Delete("delete from user_follow where follower_id=#{followerId} and followee_id=#{followeeId}")
    int deleteFollow(@Param("followerId") long followerId, @Param("followeeId") long followeeId);

    @Select("select count(*) from user_follow uf " +
            "inner join app_user au on au.id = uf.followee_id and au.status='ACTIVE' " +
            "where uf.follower_id=#{userId}")
    int countFollowing(@Param("userId") long userId);

    @Select("select count(*) from user_follow uf " +
            "inner join app_user au on au.id = uf.follower_id and au.status='ACTIVE' " +
            "where uf.followee_id=#{userId}")
    int countFollowers(@Param("userId") long userId);

    @Select("select count(*) from user_follow f1 " +
            "inner join user_follow f2 on f1.follower_id=f2.followee_id and f1.followee_id=f2.follower_id " +
            "inner join app_user au on au.id = f1.followee_id and au.status='ACTIVE' " +
            "where f1.follower_id=#{userId}")
    int countFriends(@Param("userId") long userId);

    @Select("select count(*) from user_block where blocker_id=#{blockerId} and blocked_id=#{blockedId}")
    int countBlock(@Param("blockerId") long blockerId, @Param("blockedId") long blockedId);

    @Select("select count(*) from user_block where " +
            "(blocker_id=#{a} and blocked_id=#{b}) or (blocker_id=#{b} and blocked_id=#{a})")
    int countAnyBlock(@Param("a") long a, @Param("b") long b);

    @Insert("insert ignore into user_block (blocker_id, blocked_id, created_at) values (#{blockerId}, #{blockedId}, now())")
    int insertBlock(@Param("blockerId") long blockerId, @Param("blockedId") long blockedId);

    @Delete("delete from user_block where blocker_id=#{blockerId} and blocked_id=#{blockedId}")
    int deleteBlock(@Param("blockerId") long blockerId, @Param("blockedId") long blockedId);

    @Delete("delete from user_follow where " +
            "(follower_id=#{a} and followee_id=#{b}) or (follower_id=#{b} and followee_id=#{a})")
    int deleteMutualFollows(@Param("a") long a, @Param("b") long b);

    @Delete("delete from user_follow where follower_id=#{userId} or followee_id=#{userId}")
    int deleteAllFollowsForUser(@Param("userId") long userId);

    @Delete("delete from user_block where blocker_id=#{userId} or blocked_id=#{userId}")
    int deleteAllBlocksForUser(@Param("userId") long userId);

    @Select("<script>" +
            "select au.id as userId, au.public_id as publicId, au.created_at as createdAt, uf.created_at as relationAt " +
            "from user_follow uf " +
            "inner join app_user au on au.id = uf.followee_id and au.status='ACTIVE' " +
            "where uf.follower_id = #{ownerId} " +
            "and not exists (" +
            "  select 1 from user_block ub where " +
            "  (ub.blocker_id = #{viewerId} and ub.blocked_id = au.id) or " +
            "  (ub.blocker_id = au.id and ub.blocked_id = #{viewerId})" +
            ") " +
            "<if test='cursorAt != null'>" +
            "and (uf.created_at &lt; #{cursorAt} or (uf.created_at = #{cursorAt} and uf.followee_id &lt; #{cursorId})) " +
            "</if>" +
            "order by uf.created_at desc, uf.followee_id desc limit #{limit}" +
            "</script>")
    List<Map<String, Object>> listFollowing(@Param("ownerId") long ownerId,
                                            @Param("viewerId") long viewerId,
                                            @Param("cursorAt") Date cursorAt,
                                            @Param("cursorId") Long cursorId,
                                            @Param("limit") int limit);

    @Select("<script>" +
            "select au.id as userId, au.public_id as publicId, au.created_at as createdAt, uf.created_at as relationAt " +
            "from user_follow uf " +
            "inner join app_user au on au.id = uf.follower_id and au.status='ACTIVE' " +
            "where uf.followee_id = #{ownerId} " +
            "and not exists (" +
            "  select 1 from user_block ub where " +
            "  (ub.blocker_id = #{viewerId} and ub.blocked_id = au.id) or " +
            "  (ub.blocker_id = au.id and ub.blocked_id = #{viewerId})" +
            ") " +
            "<if test='cursorAt != null'>" +
            "and (uf.created_at &lt; #{cursorAt} or (uf.created_at = #{cursorAt} and uf.follower_id &lt; #{cursorId})) " +
            "</if>" +
            "order by uf.created_at desc, uf.follower_id desc limit #{limit}" +
            "</script>")
    List<Map<String, Object>> listFollowers(@Param("ownerId") long ownerId,
                                            @Param("viewerId") long viewerId,
                                            @Param("cursorAt") Date cursorAt,
                                            @Param("cursorId") Long cursorId,
                                            @Param("limit") int limit);

    @Select("<script>" +
            "select au.id as userId, au.public_id as publicId, au.created_at as createdAt, f1.created_at as relationAt " +
            "from user_follow f1 " +
            "inner join user_follow f2 on f1.follower_id=f2.followee_id and f1.followee_id=f2.follower_id " +
            "inner join app_user au on au.id = f1.followee_id and au.status='ACTIVE' " +
            "where f1.follower_id = #{ownerId} " +
            "and not exists (" +
            "  select 1 from user_block ub where " +
            "  (ub.blocker_id = #{viewerId} and ub.blocked_id = au.id) or " +
            "  (ub.blocker_id = au.id and ub.blocked_id = #{viewerId})" +
            ") " +
            "<if test='cursorAt != null'>" +
            "and (f1.created_at &lt; #{cursorAt} or (f1.created_at = #{cursorAt} and f1.followee_id &lt; #{cursorId})) " +
            "</if>" +
            "order by f1.created_at desc, f1.followee_id desc limit #{limit}" +
            "</script>")
    List<Map<String, Object>> listFriends(@Param("ownerId") long ownerId,
                                          @Param("viewerId") long viewerId,
                                          @Param("cursorAt") Date cursorAt,
                                          @Param("cursorId") Long cursorId,
                                          @Param("limit") int limit);

    @Select("<script>" +
            "select au.id as userId, au.public_id as publicId, au.created_at as createdAt, ub.created_at as relationAt " +
            "from user_block ub " +
            "inner join app_user au on au.id = ub.blocked_id " +
            "where ub.blocker_id = #{ownerId} " +
            "<if test='cursorAt != null'>" +
            "and (ub.created_at &lt; #{cursorAt} or (ub.created_at = #{cursorAt} and ub.blocked_id &lt; #{cursorId})) " +
            "</if>" +
            "order by ub.created_at desc, ub.blocked_id desc limit #{limit}" +
            "</script>")
    List<Map<String, Object>> listBlocks(@Param("ownerId") long ownerId,
                                         @Param("cursorAt") Date cursorAt,
                                         @Param("cursorId") Long cursorId,
                                         @Param("limit") int limit);
}
