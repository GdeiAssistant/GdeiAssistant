package cn.gdeiassistant.core.secret.mapper;

import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretLikeEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SecretMapper {

    @Select("select sc.id,sc.username,sc.content,sc.theme,sc.type,sc.timer,sc.state,sc.publish_time,count(scl.id) " +
            "as like_count from secret_content sc left outer join secret_like scl on sc.id=scl.content_id " +
            "where sc.id=#{id} and state=0 group by sc.id,sc.username,sc.content,sc.theme,sc.type,sc.timer,sc.state," +
            "sc.publish_time,scl.id limit 1")
    @Results(id = "SecretContent", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "theme", column = "theme"),
            @Result(property = "content", column = "content"),
            @Result(property = "type", column = "type"),
            @Result(property = "timer", column = "timer"),
            @Result(property = "state", column = "state"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "secretCommentList", column = "id", javaType = List.class,
                    many = @Many(select = "selectSecretCommentsByContentId"))
    })
    SecretContentEntity selectSecretByID(int id);

    @Select("select * from secret_content where username=#{username} and state=0 order by id desc")
    @ResultMap("SecretContent")
    List<SecretContentEntity> selectSecretByUsername(String username);

    @Select("select * from secret_content where state=0 and timer=1")
    @ResultMap("SecretContent")
    List<SecretContentEntity> selectNotRemovedSecrets();

    @Select("select * from secret_content where state=0 order by id desc limit #{start},#{size}")
    @ResultMap("SecretContent")
    List<SecretContentEntity> selectSecret(@Param("start") int start, @Param("size") int size);

    @Select("select count(id) from secret_like where content_id=#{contentId}")
    @ResultType(Integer.class)
    Integer selectSecretLikeCount(int contentId);

    @Select("select count(id) from secret_like where content_id=#{contentId} and username=#{username} limit 1")
    @ResultType(Integer.class)
    Integer selectSecretLike(@Param("contentId") int contentId, @Param("username") String username);

    @Select("select sc.id,sc.content_id,sc.username,sc.comment,sc.publish_time,sc.avatar_theme from secret_comment sc " +
            "inner join secret_content s on sc.content_id=s.id where s.username=#{username} and sc.username!=#{username} " +
            "order by sc.publish_time desc, sc.id desc limit #{start},#{size}")
    @ResultMap("SecretComment")
    List<SecretCommentEntity> selectReceivedSecretCommentPage(@Param("username") String username,
            @Param("start") int start, @Param("size") int size);

    @Select("select sl.id,sl.content_id,sl.username,sl.create_time from secret_like sl " +
            "inner join secret_content s on sl.content_id=s.id where s.username=#{username} and sl.username!=#{username} " +
            "order by sl.create_time desc, sl.id desc limit #{start},#{size}")
    @Results(id = "SecretLike", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "contentId", column = "content_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "createTime", column = "create_time"),
    })
    List<SecretLikeEntity> selectReceivedSecretLikePage(@Param("username") String username,
            @Param("start") int start, @Param("size") int size);

    @Select("select * from secret_comment where content_id=#{contentId} order by id")
    @Results(id = "SecretComment", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "contentId", column = "content_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "avatarTheme", column = "avatar_theme")
    })
    List<SecretCommentEntity> selectSecretCommentsByContentId(int contentId);

    @Select("select count(id) from secret_comment where content_id=#{contentId}")
    @ResultType(Integer.class)
    Integer selectSecretCommentCount(int contentId);

    @Insert("insert into secret_content (username,content,theme,type,timer,state,publish_time) values(#{username},#{content},#{theme},#{type},#{timer},0,now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSecret(SecretContentEntity secretContent);

    @Insert("insert into secret_comment (content_id,username,comment,avatar_theme,publish_time) values(#{contentId},#{username},#{comment},#{avatarTheme},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertSecretComment(SecretCommentEntity secretComment);

    @Insert("insert into secret_like (content_id,username,create_time) values(#{content_id},#{username},now())")
    void insertSecretLike(@Param("content_id") int contentId, @Param("username") String username);

    @Update("update secret_content set state=1 where id=#{id}")
    void deleteSecret(@Param("id") int id);

    @Delete("delete from secret_like where content_id=#{content_id} and username=#{username}")
    void deleteSecretLike(@Param("content_id") int contentId, @Param("username") String username);

    @Update("update secret_content set username=#{newUsername} where username=#{oldUsername}")
    void anonymizeUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);

    // ---- Lightweight list queries (no eager comment loading) ----

    @Select("select * from secret_content where state=0 order by id desc limit #{start},#{size}")
    @Results(id = "SecretContentLight", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "theme", column = "theme"),
            @Result(property = "content", column = "content"),
            @Result(property = "type", column = "type"),
            @Result(property = "timer", column = "timer"),
            @Result(property = "state", column = "state"),
            @Result(property = "publishTime", column = "publish_time")
    })
    List<SecretContentEntity> selectSecretLight(@Param("start") int start, @Param("size") int size);

    @Select("select * from secret_content where username=#{username} and state=0 order by id desc limit #{start},#{size}")
    @ResultMap("SecretContentLight")
    List<SecretContentEntity> selectSecretByUsernameLight(@Param("username") String username,
            @Param("start") int start, @Param("size") int size);

    // ---- Batch count/like queries ----

    @Select("<script>" +
            "select content_id, count(id) as cnt from secret_comment " +
            "where content_id in " +
            "<foreach item='id' collection='contentIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " group by content_id" +
            "</script>")
    @MapKey("content_id")
    List<Map<String, Object>> selectSecretCommentCounts(@Param("contentIds") List<Integer> contentIds);

    @Select("<script>" +
            "select content_id, count(id) as cnt from secret_like " +
            "where content_id in " +
            "<foreach item='id' collection='contentIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " group by content_id" +
            "</script>")
    @MapKey("content_id")
    List<Map<String, Object>> selectSecretLikeCounts(@Param("contentIds") List<Integer> contentIds);

    @Select("<script>" +
            "select content_id from secret_like " +
            "where username=#{username} and content_id in " +
            "<foreach item='id' collection='contentIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Integer> selectLikedSecretContentIds(@Param("username") String username,
                                              @Param("contentIds") List<Integer> contentIds);
}
