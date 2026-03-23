package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.message.mapper.InteractionNotificationMapper;
import cn.gdeiassistant.core.message.pojo.entity.InteractionNotificationEntity;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionNotificationServiceTest {

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private InteractionNotificationMapper interactionNotificationMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private InteractionNotificationService interactionNotificationService;

    private User stubUser(String username) {
        User user = new User();
        user.setUsername(username);
        return user;
    }

    private InteractionNotificationEntity makeEntity(Long id, String actor, String content) {
        InteractionNotificationEntity e = new InteractionNotificationEntity();
        e.setNotificationId(id);
        e.setModule("dating");
        e.setType("comment");
        e.setReceiverUsername("receiver");
        e.setActorUsername(actor);
        e.setTitle("互动消息");
        e.setContent(content);
        e.setCreateTime(new Date());
        e.setIsRead(0);
        return e;
    }

    @Test
    void existingActor_remainsUnchanged() {
        when(userCertificateService.getUserLoginCertificate("sess1")).thenReturn(stubUser("receiver"));
        InteractionNotificationEntity entity = makeEntity(1L, "alice", "alice 评论了你");
        when(interactionNotificationMapper.selectInteractionNotificationPage("receiver", 0, 10))
                .thenReturn(List.of(entity));
        when(userMapper.selectExistingUsernames(anyList())).thenReturn(List.of("alice"));

        List<InteractionMessageVO> result = interactionNotificationService.queryInteractionMessages("sess1", 0, 10);

        assertEquals(1, result.size());
        assertEquals("alice 评论了你", result.get(0).getContent());
    }

    @Test
    void deletedActor_notInUserTable_getsAnonymized() {
        when(userCertificateService.getUserLoginCertificate("sess1")).thenReturn(stubUser("receiver"));
        InteractionNotificationEntity entity = makeEntity(2L, "ghost", "ghost 评论了你");
        when(interactionNotificationMapper.selectInteractionNotificationPage("receiver", 0, 10))
                .thenReturn(List.of(entity));
        // ghost is not in the user table
        when(userMapper.selectExistingUsernames(anyList())).thenReturn(Collections.emptyList());

        List<InteractionMessageVO> result = interactionNotificationService.queryInteractionMessages("sess1", 0, 10);

        assertEquals(1, result.size());
        // Content should have the username replaced with the anonymous label
        assertFalse(result.get(0).getContent().contains("ghost"));
        assertTrue(result.get(0).getContent().contains("已注销用户"));
    }

    @Test
    void delPrefixActor_getsAnonymizedViaExistingPath() {
        when(userCertificateService.getUserLoginCertificate("sess1")).thenReturn(stubUser("receiver"));
        InteractionNotificationEntity entity = makeEntity(3L, "del_12345", "del_12345 评论了你");
        when(interactionNotificationMapper.selectInteractionNotificationPage("receiver", 0, 10))
                .thenReturn(List.of(entity));
        // del_ prefix actors are NOT included in the batch lookup, so no selectExistingUsernames call expected

        List<InteractionMessageVO> result = interactionNotificationService.queryInteractionMessages("sess1", 0, 10);

        assertEquals(1, result.size());
        // del_ prefix actors go through anonymizeContent in toInteractionMessageVO
        assertFalse(result.get(0).getContent().contains("del_12345"));
        assertTrue(result.get(0).getContent().contains("已注销用户"));
        // selectExistingUsernames should not be called since only del_ actors exist
        verify(userMapper, never()).selectExistingUsernames(anyList());
    }

    @Test
    void emptyEntityList_returnsEmptyList() {
        when(userCertificateService.getUserLoginCertificate("sess1")).thenReturn(stubUser("receiver"));
        when(interactionNotificationMapper.selectInteractionNotificationPage("receiver", 0, 10))
                .thenReturn(Collections.emptyList());

        List<InteractionMessageVO> result = interactionNotificationService.queryInteractionMessages("sess1", 0, 10);

        assertTrue(result.isEmpty());
        verify(userMapper, never()).selectExistingUsernames(anyList());
    }

    @Test
    void mixedActors_batchQueryOnlyForNonDelActors() {
        when(userCertificateService.getUserLoginCertificate("sess1")).thenReturn(stubUser("receiver"));
        InteractionNotificationEntity e1 = makeEntity(1L, "alice", "alice 点赞了");
        InteractionNotificationEntity e2 = makeEntity(2L, "del_999", "del_999 评论了");
        InteractionNotificationEntity e3 = makeEntity(3L, "bob", "bob 关注了");
        when(interactionNotificationMapper.selectInteractionNotificationPage("receiver", 0, 10))
                .thenReturn(List.of(e1, e2, e3));
        // alice exists, bob does not
        when(userMapper.selectExistingUsernames(anyList())).thenReturn(List.of("alice"));

        List<InteractionMessageVO> result = interactionNotificationService.queryInteractionMessages("sess1", 0, 10);

        assertEquals(3, result.size());
        // alice: unchanged
        assertEquals("alice 点赞了", result.get(0).getContent());
        // del_999: anonymized via del_ path
        assertTrue(result.get(1).getContent().contains("已注销用户"));
        // bob: legacy deleted, anonymized
        assertFalse(result.get(2).getContent().contains("bob"));
        assertTrue(result.get(2).getContent().contains("已注销用户"));
    }
}
