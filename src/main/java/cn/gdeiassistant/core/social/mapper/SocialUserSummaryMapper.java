package cn.gdeiassistant.core.social.mapper;

import org.apache.ibatis.annotations.*;
import java.util.*;

/** Request-scoped public projection, without campus credentials. */
public interface SocialUserSummaryMapper {
    @Select("<script>" +
            "SELECT au.id AS userId, au.public_id AS publicId, au.status, COALESCE(NULLIF(p.nickname,''),'用户') AS nickname, " +
            "CASE WHEN (au.id=#{viewerId} OR pr.is_introduction_open=1) THEN intro.introduction ELSE NULL END AS introduction, " +
            "UPPER(COALESCE(NULLIF(TRIM(pr.dm_policy),''),'MUTUAL')) AS policy, " +
            "EXISTS(SELECT 1 FROM user_follow WHERE follower_id=#{viewerId} AND followee_id=au.id) AS following, " +
            "EXISTS(SELECT 1 FROM user_follow WHERE follower_id=au.id AND followee_id=#{viewerId}) AS followedBy, " +
            "EXISTS(SELECT 1 FROM user_block WHERE blocker_id=#{viewerId} AND blocked_id=au.id) AS blockedByMe, " +
            "EXISTS(SELECT 1 FROM user_block WHERE (blocker_id=#{viewerId} AND blocked_id=au.id) OR (blocker_id=au.id AND blocked_id=#{viewerId})) AS anyBlock, " +
            "(SELECT COUNT(*) FROM user_follow f INNER JOIN app_user active ON active.id=f.followee_id AND active.status='ACTIVE' WHERE f.follower_id=au.id) AS followingCount, " +
            "(SELECT COUNT(*) FROM user_follow f INNER JOIN app_user active ON active.id=f.follower_id AND active.status='ACTIVE' WHERE f.followee_id=au.id) AS followerCount, " +
            "(SELECT COUNT(*) FROM user_follow f1 INNER JOIN user_follow f2 ON f1.follower_id=f2.followee_id AND f1.followee_id=f2.follower_id INNER JOIN app_user active ON active.id=f1.followee_id AND active.status='ACTIVE' WHERE f1.follower_id=au.id) AS friendCount " +
            "FROM app_user au LEFT JOIN campus_credential cc ON cc.user_id=au.id LEFT JOIN profile p ON p.username=cc.campus_username LEFT JOIN privacy pr ON pr.username=cc.campus_username LEFT JOIN introduction intro ON intro.username=cc.campus_username " +
            "WHERE au.id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "</script>")
    List<Map<String,Object>> selectUsers(@Param("viewerId") long viewerId, @Param("ids") List<Long> ids);
}
