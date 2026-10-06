package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.mapper.SocialChatMapper;
import cn.gdeiassistant.core.social.pojo.dto.ChatMessageDTO;
import cn.gdeiassistant.core.social.pojo.dto.ConversationDTO;
import cn.gdeiassistant.core.social.pojo.dto.SocialUserDTO;
import cn.gdeiassistant.core.social.pojo.entity.ChatMessageEntity;
import cn.gdeiassistant.core.social.pojo.entity.ConversationEntity;
import cn.gdeiassistant.core.social.pojo.entity.ConversationMemberEntity;
import cn.gdeiassistant.core.social.websocket.SocialRealtimeHub;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialChatServiceTest {

    @Mock
    private SocialIdentityService identityService;
    @Mock
    private SocialChatMapper chatMapper;
    @Mock
    private SocialRealtimeHub realtimeHub;
    @Mock
    private SocialChatImageService chatImageService;

    @InjectMocks
    private SocialChatService chatService;

    private UserEntity me;
    private UserEntity peer;
    private ConversationEntity conversation;

    @BeforeEach
    void setUp() {
        me = user(1L, "11111111-1111-4111-8111-111111111111", "alice");
        peer = user(2L, "22222222-2222-4222-8222-222222222222", "bob");
        conversation = new ConversationEntity();
        conversation.setId(10L);
        conversation.setUserLowId(1L);
        conversation.setUserHighId(2L);
        conversation.setLastSeq(3L);
        lenient().when(chatImageService.isImageMessagingEnabled()).thenReturn(false);
    }

    private void stubConversationLookup() {
        when(identityService.requireActiveViewer(anyString())).thenReturn(me);
        when(chatMapper.selectMember(10L, 1L)).thenReturn(member(10L, 1L, 1L));
        when(chatMapper.selectConversationById(10L)).thenReturn(conversation);
    }

    @Test
    void idempotentRetryReturnsSameMessageWithoutPermissionRecheck() {
        stubConversationLookup();
        String clientId = UUID.randomUUID().toString();
        ChatMessageEntity existing = message(100L, 10L, 2L, 1L, clientId, "hello");
        when(chatMapper.selectByClientMessageId(10L, 1L, clientId)).thenReturn(existing);
        when(identityService.findById(1L)).thenReturn(me);

        ChatMessageDTO dto = chatService.sendMessage("sid", "10", clientId, "hello");
        assertEquals("100", dto.getId());
        assertEquals("2", dto.getSeq());
        assertEquals(SocialChatService.TYPE_TEXT, dto.getType());
        verify(chatMapper, never()).insertMessage(any());
        verify(identityService, never()).lockUsersInOrder(anyLong(), anyLong());
        verify(identityService, never()).evaluateMessagePermission(any(), any());

        SocialException conflict = assertThrows(SocialException.class,
                () -> chatService.sendMessage("sid", "10", clientId, "other"));
        assertEquals("CLIENT_MESSAGE_CONFLICT", conflict.getErrorCode());
    }

    @Test
    void retryCommittedWhileWaitingForUserLockReturnsBeforeNewPrivacyCheck() {
        stubConversationLookup();
        String clientId = UUID.randomUUID().toString();
        ChatMessageEntity existing = message(100L, 10L, 2L, 1L, clientId, "hello");
        when(chatMapper.selectByClientMessageId(10L, 1L, clientId)).thenReturn(null, existing);
        when(identityService.requireActiveById(1L)).thenReturn(me);
        when(identityService.findById(1L)).thenReturn(me);

        ChatMessageDTO dto = chatService.sendMessage("sid", "10", clientId, "hello");
        assertEquals("100", dto.getId());
        verify(identityService).lockUsersInOrder(1L, 2L);
        verify(identityService, never()).evaluateMessagePermission(any(), any());
        verify(chatMapper, never()).insertMessage(any());
    }

    @Test
    void sendLocksUsersRechecksPermissionAndPushesAfterCommit() {
        stubConversationLookup();
        String clientId = UUID.randomUUID().toString();
        when(chatMapper.selectByClientMessageId(10L, 1L, clientId)).thenReturn(null);
        when(identityService.requireActiveById(1L)).thenReturn(me);
        when(identityService.findById(2L)).thenReturn(peer);
        when(identityService.evaluateMessagePermission(me, peer))
                .thenReturn(SocialIdentityService.MessagePermission.allowed());
        when(chatMapper.selectConversationByIdForUpdate(10L)).thenReturn(conversation);
        doAnswer(inv -> {
            ChatMessageEntity m = inv.getArgument(0);
            m.setId(200L);
            return 1;
        }).when(chatMapper).insertMessage(any());
        when(identityService.findById(1L)).thenReturn(me);

        ChatMessageDTO dto = chatService.sendMessage("sid", "10", clientId, "hi");
        assertEquals("200", dto.getId());
        assertEquals("4", dto.getSeq());
        assertEquals(SocialChatService.TYPE_TEXT, dto.getType());
        verify(identityService).lockUsersInOrder(1L, 2L);
        verify(realtimeHub, times(2)).pushToUserAfterCommit(anyLong(), any());
        verify(realtimeHub, never()).pushToUser(anyLong(), any());
    }

    @Test
    void closedPeerConversationStillReadableButCannotSend() {
        stubConversationLookup();
        when(identityService.findById(2L)).thenReturn(null);
        SocialUserDTO closed = new SocialUserDTO();
        closed.setNickname("已注销");
        when(identityService.buildClosedPeer(null)).thenReturn(closed);
        when(chatMapper.selectLastMessage(10L)).thenReturn(null);
        when(chatMapper.countUnreadFromPeer(10L, 2L, 1L)).thenReturn(0);

        ConversationDTO dto = chatService.getConversation("sid", "10");
        assertEquals("已注销", dto.getPeer().getNickname());
        assertFalse(dto.isCanSend());
        assertEquals("CONTACT_UNAVAILABLE", dto.getSendPermissionReason());
        assertFalse(dto.isImageMessagingEnabled());
    }

    @Test
    void textImageClientIdConflict() {
        stubConversationLookup();
        String clientId = UUID.randomUUID().toString();
        ChatMessageEntity existing = message(100L, 10L, 2L, 1L, clientId, "hello");
        when(chatMapper.selectByClientMessageId(10L, 1L, clientId)).thenReturn(existing);

        SocialException conflict = assertThrows(SocialException.class,
                () -> chatService.sendImageMessage("sid", "10", clientId, tinyPng(), "image/png"));
        assertEquals("CLIENT_MESSAGE_CONFLICT", conflict.getErrorCode());
        verify(chatImageService, never()).uploadCanonical(anyString(), any());
    }

    @Test
    void imageIdempotentSameShaReturnsWithoutReupload() {
        stubConversationLookup();
        String clientId = UUID.randomUUID().toString();
        byte[] png = tinyPng();
        var canonical = cn.gdeiassistant.core.social.image.ChatImageCodec.canonicalize(png, "image/png");
        ChatMessageEntity existing = message(100L, 10L, 2L, 1L, clientId, "");
        existing.setType(SocialChatService.TYPE_IMAGE);
        existing.setImageSha256(canonical.sha256Hex);
        existing.setImageWidth(canonical.width);
        existing.setImageHeight(canonical.height);
        existing.setImageSize(canonical.size());
        existing.setImageContentType(canonical.contentType);
        existing.setImageKey("chat/kept");
        when(chatMapper.selectByClientMessageId(10L, 1L, clientId)).thenReturn(existing);
        when(identityService.findById(1L)).thenReturn(me);

        ChatMessageDTO dto = chatService.sendImageMessage("sid", "10", clientId, png, "image/png");
        assertEquals("100", dto.getId());
        assertEquals(SocialChatService.TYPE_IMAGE, dto.getType());
        assertNotNull(dto.getImage());
        assertEquals("/api/social/conversations/10/messages/100/image", dto.getImage().getUrl());
        verify(chatImageService, never()).uploadCanonical(anyString(), any());
        verify(identityService, never()).lockUsersInOrder(anyLong(), anyLong());
    }

    @Test
    void imageSendChecksPermissionThenUploadsAndRegistersRollback() {
        stubConversationLookup();
        String clientId = UUID.randomUUID().toString();
        byte[] png = tinyPng();
        when(chatImageService.isImageMessagingEnabled()).thenReturn(true);
        when(chatMapper.selectByClientMessageId(10L, 1L, clientId)).thenReturn(null);
        when(identityService.requireActiveById(1L)).thenReturn(me);
        when(identityService.findById(2L)).thenReturn(peer);
        when(identityService.evaluateMessagePermission(me, peer))
                .thenReturn(SocialIdentityService.MessagePermission.allowed());
        when(chatMapper.selectConversationByIdForUpdate(10L)).thenReturn(conversation);
        when(chatImageService.newObjectKey()).thenReturn("chat/rand");
        doAnswer(inv -> {
            ChatMessageEntity m = inv.getArgument(0);
            m.setId(300L);
            return 1;
        }).when(chatMapper).insertMessage(any());
        when(identityService.findById(1L)).thenReturn(me);

        ChatMessageDTO dto = chatService.sendImageMessage("sid", "10", clientId, png, "image/png");
        assertEquals("300", dto.getId());
        assertEquals(SocialChatService.TYPE_IMAGE, dto.getType());
        assertEquals("", dto.getContent());
        verify(chatImageService).uploadCanonical(eq("chat/rand"), any());
        verify(chatImageService).registerRollbackDelete("chat/rand");
        verify(identityService).lockUsersInOrder(1L, 2L);
        verify(realtimeHub, times(2)).pushToUserAfterCommit(anyLong(), any());
    }

    @Test
    void downloadImageAllowsHistoricalMemberWithoutCanSend() {
        stubConversationLookup();
        ChatMessageEntity existing = message(55L, 10L, 2L, 2L, UUID.randomUUID().toString(), "");
        existing.setType(SocialChatService.TYPE_IMAGE);
        existing.setImageKey("chat/obj");
        existing.setImageContentType("image/png");
        when(chatMapper.selectByIdInConversation(10L, 55L)).thenReturn(existing);
        when(chatImageService.isImageMessagingEnabled()).thenReturn(true);
        when(chatImageService.downloadBounded("chat/obj")).thenReturn(new byte[]{1, 2, 3});

        SocialChatService.ImageDownload download = chatService.downloadImage("sid", "10", "55");
        assertNotNull(download);
        assertEquals("image/png", download.contentType);
        assertArrayEquals(new byte[]{1, 2, 3}, download.bytes);
        verify(identityService, never()).evaluateMessagePermission(any(), any());
    }

    @Test
    void downloadImageRejectsNonMember() {
        when(identityService.requireActiveViewer(anyString())).thenReturn(me);
        when(chatMapper.selectMember(10L, 1L)).thenReturn(null);
        SocialException ex = assertThrows(SocialException.class,
                () -> chatService.downloadImage("sid", "10", "55"));
        assertEquals("CONVERSATION_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void imageMessagingDisabledReturnsUnavailable() {
        stubConversationLookup();
        when(chatImageService.isImageMessagingEnabled()).thenReturn(false);
        SocialException ex = assertThrows(SocialException.class,
                () -> chatService.sendImageMessage("sid", "10", UUID.randomUUID().toString(), tinyPng(), "image/png"));
        assertEquals("SOCIAL_IMAGE_UNAVAILABLE", ex.getErrorCode());
    }

    @Test
    void readCursorUsesLockedStateAndNotifiesOwnDevices() {
        stubConversationLookup();
        when(chatMapper.selectConversationByIdForUpdate(10L)).thenReturn(conversation);
        when(chatMapper.selectMemberForUpdate(10L, 1L)).thenReturn(member(10L, 1L, 1L));
        when(chatMapper.selectMember(10L, 1L)).thenReturn(member(10L, 1L, 2L));
        when(chatMapper.countUnreadFromPeer(eq(10L), eq(2L), anyLong())).thenReturn(0);

        var data = chatService.markRead("sid", "10", "2");
        assertEquals("2", data.get("lastReadSeq"));
        verify(chatMapper).advanceReadSeq(10L, 1L, 2L);
        verify(realtimeHub).pushToUserAfterCommit(eq(2L), any());
        verify(realtimeHub).pushToUserAfterCommit(eq(1L), any());

        SocialException invalid = assertThrows(SocialException.class,
                () -> chatService.markRead("sid", "10", "9"));
        assertEquals("INVALID_REQUEST", invalid.getErrorCode());
    }

    @Test
    void nonMemberCannotReadConversation() {
        when(identityService.requireActiveViewer(anyString())).thenReturn(me);
        when(chatMapper.selectMember(10L, 1L)).thenReturn(null);
        SocialException ex = assertThrows(SocialException.class,
                () -> chatService.getConversation("sid", "10"));
        assertEquals("CONVERSATION_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void unicodeLengthValidated() {
        String clientId = UUID.randomUUID().toString();
        String tooLong = "你".repeat(1001);
        SocialException ex = assertThrows(SocialException.class,
                () -> chatService.sendMessage("sid", "10", clientId, tooLong));
        assertEquals("INVALID_REQUEST", ex.getErrorCode());
        verify(chatMapper, never()).selectByClientMessageId(anyLong(), anyLong(), anyString());
    }

    private static byte[] tinyPng() {
        return java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
    }

    private static UserEntity user(long id, String publicId, String username) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setPublicId(publicId);
        entity.setStatus("ACTIVE");
        entity.setUsername(username);
        return entity;
    }

    private static ConversationMemberEntity member(long cid, long uid, long lastRead) {
        ConversationMemberEntity m = new ConversationMemberEntity();
        m.setConversationId(cid);
        m.setUserId(uid);
        m.setLastReadSeq(lastRead);
        return m;
    }

    private static ChatMessageEntity message(long id, long cid, long seq, long sender, String clientId, String content) {
        ChatMessageEntity m = new ChatMessageEntity();
        m.setId(id);
        m.setConversationId(cid);
        m.setSeq(seq);
        m.setSenderId(sender);
        m.setClientMessageId(clientId);
        m.setType(SocialChatService.TYPE_TEXT);
        m.setContent(content);
        m.setCreatedAt(new Date());
        return m;
    }
}
