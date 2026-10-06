package cn.gdeiassistant.core.express.service;

import cn.gdeiassistant.common.pojo.Entity.ExpressComment;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.express.converter.ExpressCommentConverterImpl;
import cn.gdeiassistant.core.express.converter.ExpressConverterImpl;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.express.pojo.dto.ExpressPublishDTO;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.express.pojo.vo.ExpressCommentVO;
import cn.gdeiassistant.core.express.pojo.vo.ExpressVO;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpressServiceAnonymityOutputTest {

    @Mock
    private ExpressMapper expressMapper;
    @Mock
    private UserCertificateService userCertificateService;
    @Mock
    private InteractionNotificationService interactionNotificationService;

    private ExpressService expressService;

    @BeforeEach
    void setUp() {
        expressService = new ExpressService();
        ReflectionTestUtils.setField(expressService, "expressMapper", expressMapper);
        ReflectionTestUtils.setField(expressService, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(expressService, "interactionNotificationService", interactionNotificationService);
        ReflectionTestUtils.setField(expressService, "expressConverter", new ExpressConverterImpl());
        ReflectionTestUtils.setField(expressService, "expressCommentConverter", new ExpressCommentConverterImpl());
    }

    @Test
    void publishInsertCapturesInternalUsername() {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("campus_alice"));
        ExpressPublishDTO dto = new ExpressPublishDTO();
        dto.setNickname("墙昵称");
        dto.setRealname("真名");
        dto.setContent("content");
        dto.setName("对方");
        dto.setSelfGender(1);
        dto.setPersonGender(0);

        expressService.addExpress(dto, "sid");

        ArgumentCaptor<ExpressEntity> captor = ArgumentCaptor.forClass(ExpressEntity.class);
        verify(expressMapper).insertExpress(captor.capture());
        assertEquals("campus_alice", captor.getValue().getUsername());
        assertEquals("墙昵称", captor.getValue().getNickname());
    }

    @Test
    void listAndDetailHideInternalUsernameKeepNicknameAndGuessableRealnameOnEntity() throws Exception {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("viewer"));
        ExpressEntity entity = new ExpressEntity();
        entity.setId(5);
        entity.setUsername("campus_alice");
        entity.setNickname("墙昵称");
        entity.setRealname("真名");
        entity.setContent("表白");
        when(expressMapper.selectExpress(0, 10, "viewer")).thenReturn(List.of(entity));

        List<ExpressVO> list = expressService.queryExpressPage(0, 10, "sid");
        assertEquals(1, list.size());
        assertNull(list.get(0).getUsername());
        assertEquals("墙昵称", list.get(0).getNickname());
        assertNull(list.get(0).getRealname());
        assertEquals("campus_alice", entity.getUsername());
        assertEquals("真名", entity.getRealname());

        when(expressMapper.selectExpressById(5, "viewer")).thenReturn(entity);
        ExpressVO detail = expressService.queryExpressById(5, "sid");
        assertNull(detail.getUsername());
        assertNull(detail.getRealname());
        assertEquals(Boolean.TRUE, detail.getCanGuess());
        assertEquals("真名", entity.getRealname());

        when(expressMapper.selectCorrectExpressGuessRecord("viewer")).thenReturn(0);
        assertTrue(expressService.guessExpress(5, "sid", "真名"));
    }

    @Test
    void commentsReturnIndependentCopiesWithoutMutatingMapperEntities() {
        ExpressComment row = new ExpressComment();
        row.setId(1);
        row.setExpressId(5);
        row.setUsername("campus_bob");
        row.setNickname("评论昵称");
        row.setComment("加油");
        when(expressMapper.selectExpressComment(5)).thenReturn(new ArrayList<>(List.of(row)));

        List<ExpressCommentVO> vos = expressService.queryExpressComment(5);
        assertEquals(1, vos.size());
        assertEquals("评论昵称", vos.get(0).getNickname());
        assertEquals("campus_bob", row.getUsername());
        try {
            ExpressCommentVO.class.getMethod("getUsername");
            fail("ExpressCommentVO must not expose username");
        } catch (NoSuchMethodException expected) {
            // expected
        }
    }

    @Test
    void commentNotificationKeepsActorReceiverButAnonymousPublicBody() throws Exception {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("campus_bob"));
        ExpressEntity entity = new ExpressEntity();
        entity.setId(5);
        entity.setUsername("campus_alice");
        when(expressMapper.selectExpressById(5, "campus_bob")).thenReturn(entity);

        expressService.addExpressComment(5, "sid", "加油");

        ArgumentCaptor<ExpressComment> insertCaptor = ArgumentCaptor.forClass(ExpressComment.class);
        verify(expressMapper).insertExpressComment(insertCaptor.capture());
        assertEquals("campus_bob", insertCaptor.getValue().getUsername());

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(interactionNotificationService).createInteractionNotification(
                eq("express"), eq("comment"), eq("campus_alice"), eq("campus_bob"),
                eq("5"), any(), eq("comment"), anyString(), contentCaptor.capture());
        assertFalse(contentCaptor.getValue().contains("campus_bob"));
        assertTrue(contentCaptor.getValue().contains("加油"));
    }
}
