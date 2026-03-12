package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.pojo.entity.TopicLikeEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TopicInteractionMessageProvider extends BaseInteractionMessageProvider {

    @Resource(name = "topicMapper")
    private TopicMapper topicMapper;

    @Override
    public List<InteractionMessageRecord> queryMessages(String username, int limit) {
        if (StringUtils.isBlank(username) || limit <= 0) {
            return new ArrayList<>();
        }
        List<TopicLikeEntity> entityList = topicMapper.selectReceivedTopicLikePage(username, 0, limit);
        List<InteractionMessageRecord> records = new ArrayList<>();
        if (entityList != null) {
            for (TopicLikeEntity entity : entityList) {
                records.add(buildRecord(toInteractionMessage(entity), entity.getCreateTime(), entity.getId()));
            }
        }
        return records;
    }

    private InteractionMessageVO toInteractionMessage(TopicLikeEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        vo.setId(entity != null && entity.getId() != null ? "topic-like-" + entity.getId() : null);
        vo.setModule("topic");
        vo.setType("like");
        vo.setTitle("话题收到新点赞");
        vo.setContent(sender + " 点赞了你的话题");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("like");
        vo.setTargetId(toStringValue(entity != null ? entity.getTopicId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getId() : null));
        return vo;
    }
}
