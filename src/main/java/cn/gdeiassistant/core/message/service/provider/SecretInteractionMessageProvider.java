package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretLikeEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SecretInteractionMessageProvider extends BaseInteractionMessageProvider {

    @Resource(name = "secretMapper")
    private SecretMapper secretMapper;

    @Override
    public List<InteractionMessageRecord> queryMessages(String username, int limit) {
        if (StringUtils.isBlank(username) || limit <= 0) {
            return new ArrayList<>();
        }
        List<InteractionMessageRecord> records = new ArrayList<>();
        List<SecretCommentEntity> commentList = secretMapper.selectReceivedSecretCommentPage(username, 0, limit);
        if (commentList != null) {
            for (SecretCommentEntity entity : commentList) {
                records.add(buildRecord(toCommentMessage(entity), entity.getPublishTime(), entity.getId()));
            }
        }
        List<SecretLikeEntity> likeList = secretMapper.selectReceivedSecretLikePage(username, 0, limit);
        if (likeList != null) {
            for (SecretLikeEntity entity : likeList) {
                records.add(buildRecord(toLikeMessage(entity), entity.getCreateTime(), entity.getId()));
            }
        }
        return limitRecords(records, limit);
    }

    private InteractionMessageVO toCommentMessage(SecretCommentEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        vo.setId(entity != null && entity.getId() != null ? "secret-comment-" + entity.getId() : null);
        vo.setModule("secret");
        vo.setType("comment");
        vo.setTitle("树洞收到新评论");
        vo.setContent(StringUtils.isNotBlank(entity != null ? entity.getComment() : null)
                ? sender + " 评论了你的树洞：" + entity.getComment()
                : sender + " 评论了你的树洞");
        vo.setCreatedAt(formatDate(entity != null ? entity.getPublishTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("comment");
        vo.setTargetId(toStringValue(entity != null ? entity.getContentId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getId() : null));
        return vo;
    }

    private InteractionMessageVO toLikeMessage(SecretLikeEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        vo.setId(entity != null && entity.getId() != null ? "secret-like-" + entity.getId() : null);
        vo.setModule("secret");
        vo.setType("like");
        vo.setTitle("树洞收到新点赞");
        vo.setContent(sender + " 点赞了你的树洞");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("like");
        vo.setTargetId(toStringValue(entity != null ? entity.getContentId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getId() : null));
        return vo;
    }
}
