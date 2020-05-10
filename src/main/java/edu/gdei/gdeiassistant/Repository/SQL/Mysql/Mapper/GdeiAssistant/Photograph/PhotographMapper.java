package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Photograph;

import edu.gdei.gdeiassistant.Pojo.Entity.Photograph;
import edu.gdei.gdeiassistant.Pojo.Entity.PhotographComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PhotographMapper {

    @Select("select p.id,p.title,p.content,p.count,p.type,p.username,p.create_time," +
            " count(distinct pl.like_id)as like_count,count(distinct pc.comment_id) as comment_count," +
            " sum(distinct CASE WHEN pl.username=#{username} THEN 1 ELSE 0 END) as liked" +
            " from photograph p" +
            " left join photograph_like pl on p.id=pl.photo_id" +
            " left join photograph_comment pc on p.id=pc.photo_id" +
            " where type=#{type} group by p.id order by p.id limit #{start},#{size}")
    @Results(id = "Photograph", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "count", column = "count"),
            @Result(property = "type", column = "type"),
            @Result(property = "username", column = "username"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "liked", column = "liked"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "commentCount", column = "comment_count")
    })
    public List<Photograph> selectPhotograph(@Param("start") int start, @Param("size") int size
            , @Param("type") int type, @Param("username") String username);

    @Select("select p.id,p.title,p.content,p.count,p.type,p.username,p.create_time," +
            " count(distinct pl.like_id)as like_count,count(distinct pc.comment_id) as comment_count," +
            " sum(distinct CASE WHEN pl.username=#{username} THEN 1 ELSE 0 END) as liked" +
            " from photograph p" +
            " left join photograph_like pl on p.id=pl.photo_id" +
            " left join photograph_comment pc on p.id=pc.photo_id" +
            " where id=#{id} group by p.id order by p.id")
    @Results(id = "PhotographContent", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "count", column = "count"),
            @Result(property = "type", column = "type"),
            @Result(property = "username", column = "username"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "liked", column = "liked"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "commentCount", column = "comment_count"),
            @Result(property = "photographCommentList", column = "id", javaType = List.class
                    , many = @Many(select = "selectPhotographCommentByPhotoId"))
    })
    public List<Photograph> selectPhotographById(int id);

    @Select("select IFNULL(sum(count),0) as count from photograph")
    @ResultType(Integer.class)
    public Integer selectPhotographImageCount();

    @Insert("insert into photograph (title,content,count,type,username,create_time) values(#{title},#{content},#{count},#{type},#{username},now())")
    @Options(useGeneratedKeys = true)
    public Integer insertPhotograph(Photograph photograph);

    @Select("select pc.comment_id,pc.photo_id,pc.comment,pc.username,p.nickname,pc.create_time from photograph_comment pc" +
            " inner join profile p on pc.username=p.username where photo_id=#{id}")
    @Results(id = "PhotographComment", value = {
            @Result(property = "commentId", column = "comment_id"),
            @Result(property = "photoId", column = "photo_id"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "createTime", column = "create_time")
    })
    public List<PhotographComment> selectPhotographCommentByPhotoId(int id);

    @Select("select count(comment_id) from photograph_comment")
    @ResultType(Integer.class)
    public Integer selectPhotographCommentCount();

    @Insert("insert into photograph_comment (photo_id,comment,username,create_time) values(#{photoId},#{comment},#{username},now())")
    public void insertPhotographComment(PhotographComment photographComment);

    @Select("select count(like_id) from photograph_like")
    @ResultType(Integer.class)
    public Integer selectPhotographLikeCount();

    @Select("select count(like_id) from photograph_like where photo_id=#{id} and username=#{username}")
    @ResultType(Integer.class)
    public Integer selectPhotographLikeCountByPhotoIdAndUsername(@Param("id") int id, @Param("username") String username);

    @Insert("insert into photograph_like (photo_id,username,create_time) values(#{id},#{username},now())")
    public void insertPhotographLike(@Param("id") int id, @Param("username") String username);
}
