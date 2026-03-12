package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.common.pojo.Entity.PhotographLike;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographCommentEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PhotographInteractionMessageProvider extends BaseInteractionMessageProvider {

    @Resource(name = "photographMapper")
    private PhotographMapper photographMapper;

    @Override
    public List<InteractionMessageRecord> queryMessages(String username, int limit) {
        if (StringUtils.isBlank(username) || limit <= 0) {
            return new ArrayList<>();
        }
        List<InteractionMessageRecord> records = new ArrayList<>();
        List<PhotographCommentEntity> commentList = photographMapper.selectReceivedPhotographCommentPage(username, 0, limit);
        if (commentList != null) {
            for (PhotographCommentEntity entity : commentList) {
                records.add(buildRecord(toCommentMessage(entity), entity.getCreateTime(), entity.getCommentId()));
            }
        }
        List<PhotographLike> likeList = photographMapper.selectReceivedPhotographLikePage(username, 0, limit);
        if (likeList != null) {
            for (PhotographLike entity : likeList) {
                records.add(buildRecord(toLikeMessage(entity), entity.getCreateTime(), entity.getLikeId()));
            }
        }
        return limitRecords(records, limit);
    }

    private InteractionMessageVO toCommentMessage(PhotographCommentEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = StringUtils.isNotBlank(entity != null ? entity.getNickname() : null)
                ? entity.getNickname()
                : entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        vo.setId(entity != null && entity.getCommentId() != null ? "photograph-comment-" + entity.getCommentId() : null);
        vo.setModule("photograph");
        vo.setType("comment");
        vo.setTitle("作品收到新评论");
        vo.setContent(StringUtils.isNotBlank(entity != null ? entity.getComment() : null)
                ? sender + " 评论了你的作品：" + entity.getComment()
                : sender + " 评论了你的作品");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("comment");
        vo.setTargetId(toStringValue(entity != null ? entity.getPhotoId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getCommentId() : null));
        return vo;
    }

    private InteractionMessageVO toLikeMessage(PhotographLike entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        vo.setId(entity != null && entity.getLikeId() != null ? "photograph-like-" + entity.getLikeId() : null);
        vo.setModule("photograph");
        vo.setType("like");
        vo.setTitle("作品收到新点赞");
        vo.setContent(sender + " 点赞了你的作品");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("like");
        vo.setTargetId(toStringValue(entity != null ? entity.getPhotoId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getLikeId() : null));
        return vo;
    }
}
