package cn.gdeiassistant.core.secret.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.secret.converter.SecretConverter;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.secret.pojo.vo.SecretVO;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretServiceTest {

    @Mock
    private SecretMapper secretMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private cn.gdeiassistant.core.objectstorage.service.StoredAssetService storedAssets;

    @Mock
    private InteractionNotificationService interactionNotificationService;

    @Mock
    private SecretConverter secretConverter;

    @InjectMocks
    private SecretService secretService;

    @Test
    void addSecretCommentThrowsIllegalArgumentExceptionForEmptyComment() {
        assertThrows(IllegalArgumentException.class,
                () -> secretService.addSecretComment(1, "session1", ""));
    }

    @Test
    void addSecretCommentThrowsIllegalArgumentExceptionForCommentOver50Chars() {
        String longComment = "a".repeat(51);

        assertThrows(IllegalArgumentException.class,
                () -> secretService.addSecretComment(1, "session1", longComment));
    }

    @Test
    void uploadVoiceSecretThrowsRuntimeExceptionOnR2Failure() {
        doThrow(new RuntimeException("R2 down"))
                .when(storedAssets).uploadObject(eq("secret/voice/1.mp3"), any(InputStream.class));

        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        assertThrows(RuntimeException.class,
                () -> secretService.uploadVoiceSecret(1, stream));
    }

    @Test
    void changeUserLikeStateInsertsLikeWhenLikeTrueAndNoExistingLike() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(secretMapper.selectSecretLike(1, "testuser")).thenReturn(0);
        when(secretMapper.insertSecretLike(1, "testuser")).thenReturn(1);
        SecretContentEntity content = new SecretContentEntity();
        content.setId(1);
        content.setUsername("poster");
        when(secretMapper.selectSecretByID(1)).thenReturn(content);

        secretService.changeUserLikeState(true, 1, "session1");

        verify(secretMapper).insertSecretLike(1, "testuser");
        verify(interactionNotificationService).createInteractionNotification(
                eq("secret"), eq("like"), eq("poster"), eq("testuser"),
                eq("1"), isNull(), eq("like"), anyString(), anyString()
        );
    }

    @Test
    void duplicateLikeInsertDoesNotNotifyAgain() throws Exception {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("testuser"));
        when(secretMapper.selectSecretLike(1, "testuser")).thenReturn(0);
        when(secretMapper.insertSecretLike(1, "testuser")).thenReturn(0);
        secretService.changeUserLikeState(true, 1, "session1");
        verifyNoInteractions(interactionNotificationService);
    }

    @Test
    void changeUserLikeStateDeletesLikeWhenLikeFalseAndExistingLike() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);

        secretService.changeUserLikeState(false, 1, "session1");

        verify(secretMapper).deleteSecretLike(1, "testuser");
        verify(secretMapper, never()).insertSecretLike(anyInt(), anyString());
    }

    // --- Batch query tests for list path ---

    private List<SecretContentEntity> buildEntities(int... ids) {
        List<SecretContentEntity> list = new ArrayList<>();
        for (int id : ids) {
            SecretContentEntity e = new SecretContentEntity();
            e.setId(id);
            e.setUsername("user" + id);
            list.add(e);
        }
        return list;
    }

    private List<SecretVO> buildVOs(int... ids) {
        List<SecretVO> list = new ArrayList<>();
        for (int id : ids) {
            SecretVO vo = new SecretVO();
            vo.setId(id);
            list.add(vo);
        }
        return list;
    }

    private void stubBatchQueries(List<Integer> contentIds, Map<Integer, Integer> commentCounts,
                                  Map<Integer, Integer> likeCounts, Set<Integer> likedIds, String username) {
        List<Map<String, Object>> commentRows = new ArrayList<>();
        commentCounts.forEach((k, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("content_id", k);
            row.put("cnt", v);
            commentRows.add(row);
        });
        when(secretMapper.selectSecretCommentCounts(contentIds)).thenReturn(commentRows);

        List<Map<String, Object>> likeRows = new ArrayList<>();
        likeCounts.forEach((k, v) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("content_id", k);
            row.put("cnt", v);
            likeRows.add(row);
        });
        when(secretMapper.selectSecretLikeCounts(contentIds)).thenReturn(likeRows);

        when(secretMapper.selectLikedSecretContentIds(username, contentIds))
                .thenReturn(new ArrayList<>(likedIds));
    }

    @Test
    void listPathReturnsCorrectCommentCountPerItem() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        List<SecretContentEntity> entities = buildEntities(10, 20, 30);
        when(secretMapper.selectSecretLight(0, 10)).thenReturn(entities);
        List<SecretVO> vos = buildVOs(10, 20, 30);
        when(secretConverter.toVOList(entities)).thenReturn(vos);

        Map<Integer, Integer> commentCounts = Map.of(10, 5, 20, 0, 30, 12);
        Map<Integer, Integer> likeCounts = Map.of(10, 1, 30, 3);
        stubBatchQueries(List.of(10, 20, 30), commentCounts, likeCounts, Set.of(30), "testuser");

        List<SecretVO> result = secretService.getSecretInfo(0, 10, "session1");

        assertEquals(3, result.size());
        assertEquals(5, result.get(0).getCommentCount());
        assertEquals(0, result.get(1).getCommentCount());
        assertEquals(12, result.get(2).getCommentCount());
    }

    @Test
    void listPathReturnsCorrectLikeCountPerItem() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        List<SecretContentEntity> entities = buildEntities(10, 20, 30);
        when(secretMapper.selectSecretLight(0, 10)).thenReturn(entities);
        List<SecretVO> vos = buildVOs(10, 20, 30);
        when(secretConverter.toVOList(entities)).thenReturn(vos);

        Map<Integer, Integer> commentCounts = Map.of(10, 5, 20, 0, 30, 12);
        Map<Integer, Integer> likeCounts = Map.of(10, 1, 30, 3);
        stubBatchQueries(List.of(10, 20, 30), commentCounts, likeCounts, Set.of(30), "testuser");

        List<SecretVO> result = secretService.getSecretInfo(0, 10, "session1");

        assertEquals(1, result.get(0).getLikeCount());
        assertEquals(0, result.get(1).getLikeCount());
        assertEquals(3, result.get(2).getLikeCount());
    }

    @Test
    void listPathReturnsCorrectLikedBoolean() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        List<SecretContentEntity> entities = buildEntities(10, 20, 30);
        when(secretMapper.selectSecretLight(0, 10)).thenReturn(entities);
        List<SecretVO> vos = buildVOs(10, 20, 30);
        when(secretConverter.toVOList(entities)).thenReturn(vos);

        Map<Integer, Integer> commentCounts = Map.of(10, 5, 20, 0, 30, 12);
        Map<Integer, Integer> likeCounts = Map.of(10, 1, 30, 3);
        stubBatchQueries(List.of(10, 20, 30), commentCounts, likeCounts, Set.of(30), "testuser");

        List<SecretVO> result = secretService.getSecretInfo(0, 10, "session1");

        assertEquals(0, result.get(0).getLiked());
        assertEquals(0, result.get(1).getLiked());
        assertEquals(1, result.get(2).getLiked());
    }

    @Test
    void profilePathReturnsCorrectDerivedFields() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        List<SecretContentEntity> entities = buildEntities(100, 200);
        when(secretMapper.selectSecretByUsernameLight("testuser", 0, 50)).thenReturn(entities);
        List<SecretVO> vos = buildVOs(100, 200);
        when(secretConverter.toVOList(entities)).thenReturn(vos);

        Map<Integer, Integer> commentCounts = Map.of(100, 3, 200, 7);
        Map<Integer, Integer> likeCounts = Map.of(100, 2);
        stubBatchQueries(List.of(100, 200), commentCounts, likeCounts, Set.of(100), "testuser");

        List<SecretVO> result = secretService.getSecretInfo("session1");

        assertEquals(2, result.size());
        assertEquals(3, result.get(0).getCommentCount());
        assertEquals(7, result.get(1).getCommentCount());
        assertEquals(2, result.get(0).getLikeCount());
        assertEquals(0, result.get(1).getLikeCount());
        assertEquals(1, result.get(0).getLiked());
        assertEquals(0, result.get(1).getLiked());
    }

    @Test
    void listPathUsesLightweightQueryNotEagerComments() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(secretMapper.selectSecretLight(0, 10)).thenReturn(new ArrayList<>());

        secretService.getSecretInfo(0, 10, "session1");

        verify(secretMapper).selectSecretLight(0, 10);
        verify(secretMapper, never()).selectSecret(anyInt(), anyInt());
        verify(secretMapper, never()).selectSecretCommentCount(anyInt());
        verify(secretMapper, never()).selectSecretLikeCount(anyInt());
        verify(secretMapper, never()).selectSecretLike(anyInt(), anyString());
    }

    /**
     * Performance contract: the list path must use batch queries for comment counts,
     * like counts, and liked state.  The per-item variants (selectSecretCommentCount,
     * selectSecretLikeCount, selectSecretLike) must NEVER be called in the list path.
     */
    @Test
    void listPathUsesBatchQueriesNotPerItemCounts() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        List<SecretContentEntity> entities = buildEntities(1, 2, 3);
        when(secretMapper.selectSecretLight(0, 10)).thenReturn(entities);
        List<SecretVO> vos = buildVOs(1, 2, 3);
        when(secretConverter.toVOList(entities)).thenReturn(vos);
        stubBatchQueries(List.of(1, 2, 3), Map.of(1, 0, 2, 0, 3, 0), Map.of(), Set.of(), "testuser");

        secretService.getSecretInfo(0, 10, "session1");

        // Per-item queries must NEVER be called — only batch variants are allowed
        verify(secretMapper, never()).selectSecretCommentCount(anyInt());
        verify(secretMapper, never()).selectSecretLikeCount(anyInt());
        verify(secretMapper, never()).selectSecretLike(anyInt(), anyString());

        // Batch queries should be called exactly once each
        verify(secretMapper).selectSecretCommentCounts(anyList());
        verify(secretMapper).selectSecretLikeCounts(anyList());
        verify(secretMapper).selectLikedSecretContentIds(eq("testuser"), anyList());
    }

    @Test
    void profilePathUsesLightweightQueryNotEagerComments() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(secretMapper.selectSecretByUsernameLight("testuser", 0, 50)).thenReturn(new ArrayList<>());

        secretService.getSecretInfo("session1");

        verify(secretMapper).selectSecretByUsernameLight("testuser", 0, 50);
        verify(secretMapper, never()).selectSecretByUsername(anyString());
    }

    @Test
    void profilePagedPathPassesRequestedOffsetAndSize() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(secretMapper.selectSecretByUsernameLight("testuser", 20, 10)).thenReturn(new ArrayList<>());

        secretService.getSecretInfo("session1", 20, 10);

        verify(secretMapper).selectSecretByUsernameLight("testuser", 20, 10);
        verify(secretMapper, never()).selectSecretByUsername(anyString());
    }
    @Test void voiceKeysUseCommittedObjectsThenBoundedFallbackAndDeleteAllFormats() {
        when(storedAssets.firstReadyKey(any(String[].class))).thenReturn("secret/voice/8.ogg");
        when(storedAssets.generatePresignedUrl("secret/voice/8.ogg",30,java.util.concurrent.TimeUnit.MINUTES)).thenReturn("https://assets.example.invalid/signed");
        assertEquals("https://assets.example.invalid/signed",secretService.getSecretVoiceURL(8));
        when(storedAssets.firstReadyKey(any(String[].class))).thenReturn(null);
        when(storedAssets.generatePresignedUrl(anyString(),anyLong(),any())).thenAnswer(inv->"secret/voice/8.webm".equals(inv.getArgument(0)) ? "https://assets.example.invalid/probe" : null);
        assertEquals("secret/voice/8.webm",secretService.findSecretVoiceObjectKey(8));
        assertEquals("",secretService.getSecretVoiceURL(9));
        doThrow(new IllegalStateException("synthetic storage outage")).when(storedAssets).deleteObject("secret/voice/8.mp3");
        secretService.deleteSecretVoice(8);verify(storedAssets,times(7)).deleteObject(anyString());
    }
    @Test void publishesVoiceWithGeneratedIdAndExpiresOnlyOldTimedContent() throws Exception {
        when(userCertificateService.getUserLoginCertificate("sid")).thenReturn(new User("synthetic-owner"));
        doAnswer(inv->{SecretContentEntity e=inv.getArgument(0);e.setId(8);assertEquals("synthetic-owner",e.getUsername());assertEquals(2,e.getTheme());assertEquals("synthetic",e.getContent());return 1;}).when(secretMapper).insertSecret(any());
        var dto=new cn.gdeiassistant.core.secret.pojo.dto.SecretPublishDTO();dto.setTheme(2);dto.setContent("synthetic");dto.setType(1);dto.setTimer(1);
        secretService.publishVoice("sid",dto,new org.springframework.mock.web.MockMultipartFile("voice","synthetic.mp3","audio/mpeg",new byte[]{1,2,3}),null);
        verify(storedAssets).uploadObject(eq("secret/voice/8.mp3"),any());
        var old=new SecretContentEntity();old.setId(10);old.setPublishTime(new Date(System.currentTimeMillis()-25*3600000L));var fresh=new SecretContentEntity();fresh.setId(11);fresh.setPublishTime(new Date());
        when(secretMapper.selectNotRemovedSecrets()).thenReturn(List.of(old,fresh));secretService.deleteTimerSecretInfos();verify(secretMapper).deleteSecret(10);verify(secretMapper,never()).deleteSecret(11);
    }

}
