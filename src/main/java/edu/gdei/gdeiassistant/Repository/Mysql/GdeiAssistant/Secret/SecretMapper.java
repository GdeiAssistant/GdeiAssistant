package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Secret;

import edu.gdei.gdeiassistant.Pojo.Entity.Secret;
import edu.gdei.gdeiassistant.Pojo.Entity.SecretComment;
import edu.gdei.gdeiassistant.Pojo.Entity.SecretContent;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface SecretMapper {

    @Select("select * from secret_content where id=#{id} limit 1")
    @Results(id = "SecretContent", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "theme", column = "theme"),
            @Result(property = "content", column = "content"),
            @Result(property = "type", column = "type"),
            @Result(property = "secretCommentList", column = "id", javaType = List.class
                    , many = @Many(select = "selectSecretComment"))
    })
    public Secret selectSecretByID(int id) throws Exception;

    @Select("select * from secret_content where username=#{username} order by id desc")
    @ResultMap("SecretContent")
    public List<Secret> selectSecretByUsername(String username) throws Exception;

    @Select("select * from secret_content order by id desc limit #{start},#{size}")
    @ResultMap("SecretContent")
    public List<Secret> selectSecret(@Param("start") int start, @Param("size") int size) throws Exception;

    @Select("select count(id) from secret_like where id=#{id}")
    @ResultType(Integer.class)
    public Integer selectSecretLikeCount(int id) throws Exception;

    @Select("select count(id) from secret_like where id=#{id} and username=#{username} limit 1")
    @ResultType(Integer.class)
    public Integer selectSecretLike(@Param("id") int id, @Param("username") String username) throws Exception;

    @Select("select * from secret_comment where id=#{id}")
    @Results(id = "SecretComment", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "avatarTheme", column = "avatar_theme")
    })
    public List<SecretComment> selectSecretComment();

    @Select("select count(id) from secret_comment where id=#{id}")
    @ResultType(Integer.class)
    public Integer selectSecretCommentCount(int id) throws Exception;

    @Insert("insert into secret_content (username,content,theme,type) values(#{username},#{content},#{theme},#{type})")
    @Options(useGeneratedKeys = true)
    public void insertSecret(SecretContent secretContent) throws Exception;

    @Insert("insert into secret_comment (id,username,comment,avatar_theme,publish_time) values(#{id},#{username},#{comment},#{avatarTheme},now())")
    public void insertSecretComment(SecretComment secretComment) throws Exception;

    @Insert("insert into secret_like (id,username) values(#{id},#{username})")
    public void insertSecretLike(@Param("id") int id, @Param("username") String username) throws Exception;

    @Delete("delete from secret_like where id=#{id} and username=#{username}")
    public void deleteSecretLike(@Param("id") int id, @Param("username") String username) throws Exception;
}
