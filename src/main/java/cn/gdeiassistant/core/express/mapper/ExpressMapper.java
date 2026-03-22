package cn.gdeiassistant.core.express.mapper;

import cn.gdeiassistant.common.pojo.Entity.ExpressComment;
import cn.gdeiassistant.common.pojo.Entity.ExpressLike;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ExpressMapper {

    @Select("select e.id,e.username,e.nickname,e.self_gender,e.name,e.content,e.person_gender,e.publish_time," +
            " count(distinct el.id) as like_count,count(distinct em.id) as comment_count,count(distinct eg.id) as guess_sum," +
            " sum(distinct CASE WHEN el.username=#{username} THEN 1 ELSE 0 END) as liked," +
            " sum(distinct CASE WHEN eg.result=1 THEN 1 ELSE 0 END) as guess_count" +
            " from express e" +
            " left join express_like el on e.id=el.express_id" +
            " left join express_comment em on e.id=em.express_id" +
            " left join express_guess eg on e.id=eg.express_id" +
            " group by e.id order by e.id limit #{start},#{size}")
    @Results(id = "express", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "selfGender", column = "self_gender"),
            @Result(property = "name", column = "name"),
            @Result(property = "content", column = "content"),
            @Result(property = "personGender", column = "person_gender"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "liked", column = "liked"),
            @Result(property = "guessCount", column = "guess_count"),
            @Result(property = "guessSum", column = "guess_sum"),
            @Result(property = "commentCount", column = "comment_count")
    })
    List<ExpressEntity> selectExpress(@Param("start") int start, @Param("size") int size, @Param("username") String username);

    @Select("select e.id,e.username,e.nickname,e.self_gender,e.name,e.content,e.person_gender,e.publish_time," +
            " count(distinct el.id) as like_count,count(distinct em.id) as comment_count,count(distinct eg.id) as guess_sum," +
            " sum(distinct CASE WHEN el.username=#{username} THEN 1 ELSE 0 END) as liked," +
            " sum(distinct CASE WHEN eg.result=1 THEN 1 ELSE 0 END) as guess_count" +
            " from express e" +
            " left join express_like el on e.id=el.express_id" +
            " left join express_comment em on e.id=em.express_id" +
            " left join express_guess eg on e.id=eg.express_id" +
            " where e.username=#{publisherUsername} group by e.id order by e.id desc limit #{start},#{size}")
    @ResultMap("express")
    List<ExpressEntity> selectExpressByUsername(@Param("start") int start, @Param("size") int size, @Param("username") String username, @Param("publisherUsername") String publisherUsername);

    @Select("select e.id,e.username,e.nickname,e.realname,e.self_gender,e.name,e.content,e.person_gender,e.publish_time," +
            " count(distinct el.id) as like_count,count(distinct em.id) as comment_count,count(distinct eg.id) as guess_sum," +
            " sum(distinct CASE WHEN el.username=#{username} THEN 1 ELSE 0 END) as liked," +
            " sum(distinct CASE WHEN eg.result=1 THEN 1 ELSE 0 END) as guess_count" +
            " from express e" +
            " left join express_like el on e.id=el.express_id" +
            " left join express_comment em on e.id=em.express_id" +
            " left join express_guess eg on e.id=eg.express_id" +
            " where e.id=#{id} group by e.id order by e.id")
    @Results(id = "expressContent", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "realname", column = "realname"),
            @Result(property = "selfGender", column = "self_gender"),
            @Result(property = "name", column = "name"),
            @Result(property = "content", column = "content"),
            @Result(property = "personGender", column = "person_gender"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "liked", column = "liked"),
            @Result(property = "guessCount", column = "guess_count"),
            @Result(property = "guessSum", column = "guess_sum"),
            @Result(property = "commentCount", column = "comment_count")
    })
    ExpressEntity selectExpressById(@Param("id") int id, @Param("username") String username);

    @Select("select e.id,e.username,e.nickname,e.self_gender,e.name,e.content,e.person_gender,e.publish_time," +
            " count(distinct el.id) as like_count,count(distinct em.id) as comment_count,count(distinct eg.id) as guess_sum," +
            " sum(distinct CASE WHEN el.username=#{username} THEN 1 ELSE 0 END) as liked," +
            " sum(distinct CASE WHEN eg.result=1 THEN 1 ELSE 0 END) as guess_count" +
            " from express e" +
            " left join express_like el on e.id=el.express_id" +
            " left join express_comment em on e.id=em.express_id" +
            " left join express_guess eg on e.id=eg.express_id" +
            " where e.nickname like concat(concat('%',#{keyword}),'%') or e.name like concat(concat('%',#{keyword}),'%')" +
            " group by e.id order by e.id limit #{start},#{size}")
    @ResultMap("express")
    List<ExpressEntity> selectExpressByKeyWord(@Param("start") int start, @Param("size") int size, @Param("username") String username, @Param("keyword") String keyword);

    @Insert("insert into express (username,nickname,realname,self_gender,name,content,person_gender,publish_time) values(#{username},#{nickname},#{realname},#{selfGender},#{name},#{content},#{personGender},now())")
    void insertExpress(ExpressEntity express);

    @Select("select * from express_like where username=#{username} and express_id=#{expressId}")
    @Results(id = "expressLike", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "expressId", column = "express_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "createTime", column = "create_time")
    })
    ExpressLike selectExpressLike(@Param("expressId") int expressId, @Param("username") String username);

    @Insert("insert into express_like (express_id,username,create_time) values(#{expressId},#{username},now())")
    void insertExpressLike(@Param("expressId") int expressId, @Param("username") String username);

    @Select("select el.id,el.express_id,el.username,el.create_time from express_like el " +
            "inner join express e on el.express_id=e.id where e.username=#{username} and el.username!=#{username} " +
            "order by el.create_time desc, el.id desc limit #{start},#{size}")
    @ResultMap("expressLike")
    List<ExpressLike> selectReceivedExpressLikePage(@Param("username") String username,
            @Param("start") int start, @Param("size") int size);

    @Select("select count(id) from express_guess where username=#{username} and result=1")
    Integer selectCorrectExpressGuessRecord(String username);

    @Insert("insert into express_guess (express_id,username,result,create_time) values(#{expressId},#{username},#{result},now())")
    void insertExpressGuess(@Param("expressId") int expressId, @Param("username") String username, @Param("result") int result);

    @Select("select eg.id,eg.express_id,eg.username,eg.result,eg.create_time from express_guess eg " +
            "inner join express e on eg.express_id=e.id where e.username=#{username} and eg.username!=#{username} " +
            "order by eg.create_time desc, eg.id desc limit #{start},#{size}")
    @Results(id = "expressGuess", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "expressId", column = "express_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "result", column = "result"),
            @Result(property = "createTime", column = "create_time")
    })
    List<cn.gdeiassistant.common.pojo.Entity.ExpressGuess> selectReceivedExpressGuessPage(@Param("username") String username,
            @Param("start") int start, @Param("size") int size);

    @Select("select ec.id,ec.username,p.nickname,ec.express_id,ec.comment,ec.publish_time" +
            " from express_comment ec" +
            " left join profile p on p.username=ec.username" +
            " where express_id=#{expressId}")
    @Results(id = "expressComment", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "expressId", column = "express_id"),
            @Result(property = "comment", column = "comment"),
            @Result(property = "publishTime", column = "publish_time")
    })
    List<ExpressComment> selectExpressComment(@Param("expressId") int expressId);

    @Insert("insert into express_comment (username,express_id,comment,publish_time) values(#{username},#{expressId},#{comment},now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertExpressComment(ExpressComment expressComment);

    @Select("select ec.id,ec.username,p.nickname,ec.express_id,ec.comment,ec.publish_time from express_comment ec " +
            "left join profile p on p.username=ec.username inner join express e on ec.express_id=e.id " +
            "where e.username=#{username} and ec.username!=#{username} order by ec.publish_time desc, ec.id desc limit #{start},#{size}")
    @ResultMap("expressComment")
    List<ExpressComment> selectReceivedExpressCommentPage(@Param("username") String username,
            @Param("start") int start, @Param("size") int size);

    @Update("update express set username=#{newUsername}, nickname='已注销用户', realname=null where username=#{oldUsername}")
    void anonymizeByUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);

    @Update("update express_comment set username=#{newUsername} where username=#{oldUsername}")
    void anonymizeCommentsByUsername(@Param("oldUsername") String oldUsername, @Param("newUsername") String newUsername);
}
