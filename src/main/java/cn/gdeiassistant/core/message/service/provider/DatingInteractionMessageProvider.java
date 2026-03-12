package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.roommate.mapper.RoommateMapper;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommateMessageEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommatePickEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommateProfileEntity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DatingInteractionMessageProvider extends BaseInteractionMessageProvider {

    @Resource(name = "roommateMapper")
    private RoommateMapper roommateMapper;

    @Override
    public List<InteractionMessageRecord> queryMessages(String username, int limit) {
        if (StringUtils.isBlank(username) || limit <= 0) {
            return new ArrayList<>();
        }
        List<RoommateMessageEntity> entityList = roommateMapper.selectUserRoommateMessageInteractionPage(username, 0, limit);
        List<InteractionMessageRecord> records = new ArrayList<>();
        if (entityList != null) {
            for (RoommateMessageEntity entity : entityList) {
                records.add(buildRecord(toInteractionMessage(entity), entity.getCreateTime(), entity.getMessageId()));
            }
        }
        return records;
    }

    private InteractionMessageVO toInteractionMessage(RoommateMessageEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        RoommatePickEntity pick = entity != null ? entity.getRoommatePick() : null;
        RoommateProfileEntity profile = pick != null ? pick.getRoommateProfile() : null;
        Integer type = entity != null ? entity.getType() : null;
        Integer pickState = pick != null ? pick.getState() : null;
        vo.setId(toStringValue(entity != null ? entity.getMessageId() : null));
        vo.setModule("dating");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(entity != null && !Integer.valueOf(0).equals(entity.getState()));
        vo.setTargetId(toStringValue(pick != null ? pick.getPickId() : null));
        vo.setTargetSubId(toStringValue(profile != null ? profile.getProfileId() : null));
        if (Integer.valueOf(0).equals(type)) {
            String sender = pick != null && StringUtils.isNotBlank(pick.getUsername()) ? pick.getUsername() : "有同学";
            vo.setType("pick_received");
            vo.setTitle("收到新的撩一下");
            vo.setContent(StringUtils.isNotBlank(pick != null ? pick.getContent() : null)
                    ? sender + " 向你发起了撩一下：" + pick.getContent()
                    : sender + " 向你发起了撩一下");
            vo.setTargetType("received");
            return vo;
        }
        if (Integer.valueOf(1).equals(type)) {
            String nickname = profile != null && StringUtils.isNotBlank(profile.getNickname()) ? profile.getNickname() : "对方";
            vo.setTargetType("sent");
            if (Integer.valueOf(1).equals(pickState)) {
                vo.setType("pick_accepted");
                vo.setTitle("撩一下已通过");
                vo.setContent(nickname + " 通过了你的请求");
                return vo;
            }
            if (Integer.valueOf(-1).equals(pickState) || Integer.valueOf(2).equals(pickState)) {
                vo.setType("pick_rejected");
                vo.setTitle("撩一下未通过");
                vo.setContent(nickname + " 拒绝了你的请求");
                return vo;
            }
            vo.setType("pick_updated");
            vo.setTitle("撩一下状态更新");
            vo.setContent(nickname + " 更新了你的请求状态");
            return vo;
        }
        vo.setType("pick_message");
        vo.setTitle("卖室友互动消息");
        vo.setContent(StringUtils.isNotBlank(pick != null ? pick.getContent() : null) ? pick.getContent() : "你有一条新的互动消息");
        vo.setTargetType(null);
        return vo;
    }
}
