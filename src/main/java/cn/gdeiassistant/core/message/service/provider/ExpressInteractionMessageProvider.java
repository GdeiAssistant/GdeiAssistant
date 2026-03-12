package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.common.pojo.Entity.ExpressComment;
import cn.gdeiassistant.common.pojo.Entity.ExpressGuess;
import cn.gdeiassistant.common.pojo.Entity.ExpressLike;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpressInteractionMessageProvider extends BaseInteractionMessageProvider {

    @Resource(name = "expressMapper")
    private ExpressMapper expressMapper;

    @Override
    public List<InteractionMessageRecord> queryMessages(String username, int limit) {
        if (StringUtils.isBlank(username) || limit <= 0) {
            return new ArrayList<>();
        }
        List<InteractionMessageRecord> records = new ArrayList<>();
        List<ExpressComment> commentList = expressMapper.selectReceivedExpressCommentPage(username, 0, limit);
        if (commentList != null) {
            for (ExpressComment entity : commentList) {
                records.add(buildRecord(toCommentMessage(entity), entity.getPublishTime(), entity.getId()));
            }
        }
        List<ExpressLike> likeList = expressMapper.selectReceivedExpressLikePage(username, 0, limit);
        if (likeList != null) {
            for (ExpressLike entity : likeList) {
                records.add(buildRecord(toLikeMessage(entity), entity.getCreateTime(), entity.getId()));
            }
        }
        List<ExpressGuess> guessList = expressMapper.selectReceivedExpressGuessPage(username, 0, limit);
        if (guessList != null) {
            for (ExpressGuess entity : guessList) {
                records.add(buildRecord(toGuessMessage(entity), entity.getCreateTime(), entity.getId()));
            }
        }
        return limitRecords(records, limit);
    }

    private InteractionMessageVO toCommentMessage(ExpressComment entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = StringUtils.isNotBlank(entity != null ? entity.getNickname() : null)
                ? entity.getNickname()
                : entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        vo.setId(entity != null && entity.getId() != null ? "express-comment-" + entity.getId() : null);
        vo.setModule("express");
        vo.setType("comment");
        vo.setTitle("表白墙收到新评论");
        vo.setContent(StringUtils.isNotBlank(entity != null ? entity.getComment() : null)
                ? sender + " 评论了你的表白：" + entity.getComment()
                : sender + " 评论了你的表白");
        vo.setCreatedAt(formatDate(entity != null ? entity.getPublishTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("comment");
        vo.setTargetId(toStringValue(entity != null ? entity.getExpressId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getId() : null));
        return vo;
    }

    private InteractionMessageVO toLikeMessage(ExpressLike entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        vo.setId(entity != null && entity.getId() != null ? "express-like-" + entity.getId() : null);
        vo.setModule("express");
        vo.setType("like");
        vo.setTitle("表白墙收到新点赞");
        vo.setContent(sender + " 点赞了你的表白");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("like");
        vo.setTargetId(toStringValue(entity != null ? entity.getExpressId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getId() : null));
        return vo;
    }

    private InteractionMessageVO toGuessMessage(ExpressGuess entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        String sender = entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        boolean correct = entity != null && Integer.valueOf(1).equals(entity.getResult());
        vo.setId(entity != null && entity.getId() != null ? "express-guess-" + entity.getId() : null);
        vo.setModule("express");
        vo.setType("guess");
        vo.setTitle(correct ? "表白墙有人猜中了" : "表白墙有人参与猜名字");
        vo.setContent(correct ? sender + " 猜中了你的表白对象" : sender + " 参与了你的猜名字");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("guess");
        vo.setTargetId(toStringValue(entity != null ? entity.getExpressId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getId() : null));
        return vo;
    }
}
