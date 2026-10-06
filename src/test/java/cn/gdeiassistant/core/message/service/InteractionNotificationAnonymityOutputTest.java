package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.message.mapper.InteractionNotificationMapper;
import cn.gdeiassistant.core.message.pojo.entity.InteractionNotificationEntity;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteractionNotificationAnonymityOutputTest {

    @Mock
    private UserCertificateService userCertificateService;
    @Mock
    private InteractionNotificationMapper interactionNotificationMapper;

    private InteractionNotificationService service;

    @BeforeEach
    void setUp() {
        service = new InteractionNotificationService();
        ReflectionTestUtils.setField(service, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(service, "interactionNotificationMapper", interactionNotificationMapper);
    }

    @Test
    void createKeepsActorReceiverUsernames() {
        service.createInteractionNotification(
                "express", "comment", "campus_alice", "campus_bob",
                "5", "1", "comment", "表白墙收到新评论", "有人评论了你的表白：加油");

        ArgumentCaptor<InteractionNotificationEntity> captor =
                ArgumentCaptor.forClass(InteractionNotificationEntity.class);
        verify(interactionNotificationMapper).insertInteractionNotification(captor.capture());
        InteractionNotificationEntity stored = captor.getValue();
        assertEquals("campus_alice", stored.getReceiverUsername());
        assertEquals("campus_bob", stored.getActorUsername());
        assertFalse(stored.getContent().contains("campus_bob"));
    }

    @Test
    void publicQueryAnonymizesExpressContentButDoesNotClearStoredActor() {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("campus_alice"));
        InteractionNotificationEntity entity = new InteractionNotificationEntity();
        entity.setNotificationId(11L);
        entity.setModule("express");
        entity.setType("comment");
        entity.setReceiverUsername("campus_alice");
        entity.setActorUsername("campus_bob");
        entity.setContent("campus_bob 评论了你的表白：加油");
        entity.setTitle("表白墙收到新评论");
        entity.setCreateTime(new Date());
        entity.setIsRead(0);
        entity.setTargetId("5");
        when(interactionNotificationMapper.selectInteractionNotificationPage("campus_alice", 0, 10))
                .thenReturn(List.of(entity));

        List<InteractionMessageVO> vos = service.queryInteractionMessages("sid", 0, 10);
        assertEquals(1, vos.size());
        assertFalse(vos.get(0).getContent().contains("campus_bob"));
        assertEquals("campus_bob", entity.getActorUsername());
        assertEquals("campus_alice", entity.getReceiverUsername());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "校园账号", "del_123", "campus_bob"})
    void treeholeNotificationUsesRecordedActorWithoutChangingStoredIdentity(String actor) {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("receiver"));
        InteractionNotificationEntity entity = new InteractionNotificationEntity();
        entity.setModule("secret");
        entity.setActorUsername(actor);
        entity.setReceiverUsername("receiver");
        entity.setContent(actor + " 评论了你的树洞");
        when(interactionNotificationMapper.selectInteractionNotificationPage("receiver", 0, 10))
                .thenReturn(List.of(entity));
        InteractionMessageVO result = service.queryInteractionMessages("sid", 0, 10).get(0);
        assertEquals("匿名用户 评论了你的树洞", result.getContent());
        assertEquals(actor, entity.getActorUsername());
        assertEquals(actor + " 评论了你的树洞", entity.getContent());
    }
}
