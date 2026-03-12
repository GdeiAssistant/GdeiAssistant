package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.message.service.provider.InteractionMessageProvider;
import cn.gdeiassistant.core.message.service.provider.InteractionMessageRecord;
import cn.gdeiassistant.core.roommate.mapper.RoommateMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "roommateMapper")
    private RoommateMapper roommateMapper;

    @Autowired(required = false)
    private List<InteractionMessageProvider> interactionMessageProviderList = Collections.emptyList();

    public List<InteractionMessageVO> queryInteractionMessages(String sessionId, Integer start, Integer size) {
        if (start == null || start < 0 || size == null || size <= 0) {
            return new ArrayList<>();
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        int fetchSize = start + size;
        List<InteractionMessageRecord> records = new ArrayList<>();
        for (InteractionMessageProvider provider : interactionMessageProviderList) {
            List<InteractionMessageRecord> providerRecords = provider.queryMessages(user.getUsername(), fetchSize);
            if (providerRecords != null && !providerRecords.isEmpty()) {
                records.addAll(providerRecords);
            }
        }
        if (records.isEmpty()) {
            return new ArrayList<>();
        }

        records.sort(InteractionMessageRecord.DEFAULT_COMPARATOR);

        List<InteractionMessageVO> voList = new ArrayList<>();
        int end = Math.min(records.size(), start + size);
        for (int i = start; i < end; i++) {
            voList.add(records.get(i).getMessage());
        }
        return voList;
    }

    public Integer queryInteractionUnreadCount(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Integer datingUnreadCount = roommateMapper.selectUserUnReadRoommateMessageCount(user.getUsername());
        return datingUnreadCount == null ? 0 : Math.max(datingUnreadCount, 0);
    }
}
