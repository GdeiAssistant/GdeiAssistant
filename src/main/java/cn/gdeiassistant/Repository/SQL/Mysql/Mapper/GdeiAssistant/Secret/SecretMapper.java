package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Secret;

import cn.gdeiassistant.Pojo.Entity.Secret;
import cn.gdeiassistant.Pojo.Entity.SecretComment;
import cn.gdeiassistant.Pojo.Entity.SecretContent;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface SecretMapper {

    @Select("select sc.id,sc.content,sc.theme,sc.type,sc.timer,sc.state,sc.publish_time,count(scl.id) " +
            "as like_count from secret_content sc left outer join secret_like scl on sc.id=scl.content_id " +
            "where sc.id=#{id} and state=0 group by sc.id,sc.content,sc.theme,sc.type,sc.timer,sc.state," +
            "sc.publish_time,scl.id limit 1")
    @Results(id = "SecretContent", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "theme", column = "theme"),
            @Result(property = "content", column = "content"),
            @Result(property = "type", column = "type"),
            @Result(property = "timer", column = "timer"),
            @Result(property = "state", column = "state"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "secretCommentList", column = "id", javaType = List.class
                    , many = @Many(select = "selectSecretComment"))
    })
    Secret selectSecretByID(int id);

    @Select("select * from secret_content where username=#{username} and state=0 order by id desc")
    @ResultMap("SecretContent")
    List<Secret> selectSecretByUsername(String username);

    @Select("select * from secret_content where state=0 and timer=1")
    @ResultMap("SecretContent")
    List<Secret> selectNotRemovedSecrets();

    @Select("select * from secret_content where state=0 order by id desc limit #{start},#{size}")
    @ResultMap("SecretContent")
    List<Secret> selectSecret(@Param("start") int start, @Param("size") int size);

    @Select("select count(id) from secret_like where content_id=#{contentId}")
    @ResultType(Integer.class)
    Integer selectSecretLikeCount(int contentId);

    @Select("select count(id) from secret_like where content_id=#{contentId} and username=#{username} limit 1")
    @ResultType(Integer.class)
    Integer selectSecretLike(@Param("contentId") int contentId, @Param("username") String username);

    @Select("select * from secret_comment where content_id=#{id}")
    @Results(id = "SecretComment", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "contentId", column = "content_id"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "avatarTheme", column = "avatar_theme")
    })
    List<SecretComment> selectSecretComment();

    @Select("select count(id) from secret_comment where content_id=#{contentId}")
    @ResultType(Integer.class)
    Integer selectSecretCommentCount(int contentId);

    @Insert("insert into secret_content (username,content,theme,type,timer,state,publish_time) values(#{username},#{content},#{theme},#{type},#{timer},0,now())")
    @Options(useGeneratedKeys = true)
    void insertSecret(SecretContent secretContent);

    @Insert("insert into secret_comment (content_id,username,comment,avatar_theme,publish_time) values(#{contentId},#{username},#{comment},#{avatarTheme},now())")
    void insertSecretComment(SecretComment secretComment);

    @Insert("insert into secret_like (content_id,username) values(#{content_id},#{username})")
    void insertSecretLike(@Param("content_id") int contentId, @Param("username") String username);

    @Update("update secret_content set state=1 where id=#{id}")
    void deleteSecret(@Param("id") int id);

    @Delete("delete from secret_like where content_id=#{content_id} and username=#{username}")
    void deleteSecretLike(@Param("content_id") int contentId, @Param("username") String username);
}
