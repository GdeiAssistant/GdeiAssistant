package cn.gdeiassistant.core.feedback.mapper;

import cn.gdeiassistant.core.feedback.pojo.entity.FeedbackEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

/**
 * 用户反馈表 persistence。表：feedback(id, username, content, contact, type, create_time)
 */
public interface FeedbackMapper {

    @Insert("insert into feedback (username, content, contact, type, create_time) " +
            "values (#{username}, #{content}, #{contact}, #{type}, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertFeedback(FeedbackEntity entity);
}
