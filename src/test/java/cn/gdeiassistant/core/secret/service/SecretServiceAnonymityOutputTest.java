package cn.gdeiassistant.core.secret.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.utils.AnonymizeUtils;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.secret.converter.SecretCommentConverter;
import cn.gdeiassistant.core.secret.converter.SecretCommentConverterImpl;
import cn.gdeiassistant.core.secret.converter.SecretConverter;
import cn.gdeiassistant.core.secret.converter.SecretConverterImpl;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.pojo.dto.SecretPublishDTO;
import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.secret.pojo.vo.SecretCommentVO;
import cn.gdeiassistant.core.secret.pojo.vo.SecretVO;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.objectstorage.service.StoredAssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretServiceAnonymityOutputTest {

    @Mock
    private SecretMapper secretMapper;
    @Mock
    private UserCertificateService userCertificateService;
    @Mock
    private StoredAssetService storedAssets;
    @Mock
    private InteractionNotificationService interactionNotificationService;

    private SecretService secretService;

    @BeforeEach
    void setUp() {
        SecretCommentConverter commentConverter = new SecretCommentConverterImpl();
        SecretConverterImpl converter = new SecretConverterImpl();
        ReflectionTestUtils.setField(converter, "secretCommentConverter", commentConverter);
        secretService = new SecretService();
        ReflectionTestUtils.setField(secretService, "secretMapper", secretMapper);
        ReflectionTestUtils.setField(secretService, "userCertificateService", userCertificateService);
        ReflectionTestUtils.setField(secretService, "storedAssets", storedAssets);
        ReflectionTestUtils.setField(secretService, "interactionNotificationService", interactionNotificationService);
        ReflectionTestUtils.setField(secretService, "secretConverter", converter);
        ReflectionTestUtils.setField(secretService, "secretCommentConverter", commentConverter);
    }

    @Test
    void publishInsertKeepsInternalCampusUsername() throws Exception {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("campus_alice"));
        doAnswer(inv -> {
            SecretContentEntity e = inv.getArgument(0);
            e.setId(42);
            return null;
        }).when(secretMapper).insertSecret(any());

        SecretPublishDTO dto = new SecretPublishDTO();
        dto.setContent("secret body");
        dto.setTheme(1);
        dto.setType(0);
        dto.setTimer(0);
        secretService.addSecretInfo("sid", dto);

        ArgumentCaptor<SecretContentEntity> captor = ArgumentCaptor.forClass(SecretContentEntity.class);
        verify(secretMapper).insertSecret(captor.capture());
        assertEquals("campus_alice", captor.getValue().getUsername());
    }

    @Test
    void listAndDetailDoNotMutateMapperEntitiesButHideUsernameInVo() throws Exception {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("viewer"));
        SecretContentEntity entity = new SecretContentEntity();
        entity.setId(9);
        entity.setUsername("campus_alice");
        entity.setContent("body");
        entity.setPublishTime(new Date());
        when(secretMapper.selectSecretLight(0, 10)).thenReturn(new ArrayList<>(List.of(entity)));
        when(secretMapper.selectSecretCommentCounts(anyList())).thenReturn(List.of());
        when(secretMapper.selectSecretLikeCounts(anyList())).thenReturn(List.of());
        when(secretMapper.selectLikedSecretContentIds(eq("viewer"), anyList())).thenReturn(List.of());

        List<SecretVO> list = secretService.getSecretInfo(0, 10, "sid");
        assertEquals(1, list.size());
        assertEquals(AnonymizeUtils.treeholeAnonymousLabel(), list.get(0).getUsername());
        assertEquals("campus_alice", entity.getUsername());

        when(secretMapper.selectSecretByID(9)).thenReturn(entity);
        when(secretMapper.selectSecretCommentCount(9)).thenReturn(0);
        when(secretMapper.selectSecretLikeCount(9)).thenReturn(0);
        when(secretMapper.selectSecretLike(9, "viewer")).thenReturn(0);
        SecretVO detail = secretService.getSecretDetailInfo(9, "sid");
        assertEquals(AnonymizeUtils.treeholeAnonymousLabel(), detail.getUsername());
        assertEquals("campus_alice", entity.getUsername());
    }

    @Test
    void commentsReturnAnonymousVoWithoutMutatingMapperRows() throws Exception {
        SecretCommentEntity comment = new SecretCommentEntity();
        comment.setId(1);
        comment.setContentId(9);
        comment.setUsername("campus_bob");
        comment.setComment("hi");
        when(secretMapper.selectSecretCommentsByContentId(9)).thenReturn(new ArrayList<>(List.of(comment)));

        List<SecretCommentVO> vos = secretService.getSecretComments(9);
        assertEquals(1, vos.size());
        assertEquals(AnonymizeUtils.treeholeAnonymousLabel(), vos.get(0).getUsername());
        assertEquals("campus_bob", comment.getUsername());
    }

    @Test
    void commentInsertKeepsInternalUsernameAndAnonymousNotificationBody() throws Exception {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("campus_bob"));
        SecretContentEntity content = new SecretContentEntity();
        content.setId(9);
        content.setUsername("campus_alice");
        when(secretMapper.selectSecretByID(9)).thenReturn(content);
        doAnswer(inv -> {
            SecretCommentEntity e = inv.getArgument(0);
            e.setId(55);
            return null;
        }).when(secretMapper).insertSecretComment(any());

        secretService.addSecretComment(9, "sid", "nice");

        ArgumentCaptor<SecretCommentEntity> commentCaptor = ArgumentCaptor.forClass(SecretCommentEntity.class);
        verify(secretMapper).insertSecretComment(commentCaptor.capture());
        assertEquals("campus_bob", commentCaptor.getValue().getUsername());

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(interactionNotificationService).createInteractionNotification(
                eq("secret"), eq("comment"), eq("campus_alice"), eq("campus_bob"),
                eq("9"), eq("55"), eq("comment"), anyString(), contentCaptor.capture());
        assertFalse(contentCaptor.getValue().contains("campus_bob"));
        assertTrue(contentCaptor.getValue().contains(AnonymizeUtils.treeholeAnonymousLabel()));
    }
}
