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
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
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

    private CampusAccountView me;
    private CampusAccountView peer;
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

    @Test
    void conversationPageUsesFixedBatchQueriesAndKeepsPerPeerPermission() {
        when(identityService.requireActiveViewer("session")).thenReturn(me);
        var second = new ConversationEntity(); second.setId(11L);second.setUserLowId(1L);second.setUserHighId(3L);second.setCreatedAt(new Date(1000));
        conversation.setCreatedAt(new Date(1000));
        var excluded = new ConversationEntity();excluded.setId(12L);
        when(chatMapper.listConversations(1L,null,null,3)).thenReturn(java.util.List.of(conversation,second,excluded));
        var allowed=new SocialUserDTO();allowed.setId(peer.getPublicId());allowed.setCanMessage(true);
        var blocked=new SocialUserDTO();blocked.setId("33333333-3333-4333-8333-333333333333");blocked.setCanMessage(false);blocked.setMessagePermissionReason("BLOCKED");
        when(identityService.buildSocialUsers(me,java.util.List.of(2L,3L))).thenReturn(java.util.Map.of(2L,allowed,3L,blocked));
        when(chatMapper.selectMembers(1L,java.util.List.of(10L,11L))).thenReturn(java.util.List.of(member(10L,1L,1L)));
        when(chatMapper.selectLastMessages(java.util.List.of(10L,11L))).thenReturn(java.util.List.of(message(100L,10L,3L,2L,"synthetic","last")));
        when(chatMapper.selectUnreadCounts(1L,java.util.List.of(10L,11L))).thenReturn(java.util.List.of(java.util.Map.of("conversationId",10L,"unreadCount",2L)));
        var result=chatService.listConversations("session",null,2);
        assertEquals(2,result.getItems().size());assertTrue(result.isHasMore());assertNotNull(result.getNextCursor());
        assertTrue(result.getItems().get(0).isCanSend());assertEquals(2,result.getItems().get(0).getUnreadCount());
        assertEquals(peer.getPublicId(),result.getItems().get(0).getLastMessage().getSenderId());
        assertFalse(result.getItems().get(1).isCanSend());assertEquals("BLOCKED",result.getItems().get(1).getSendPermissionReason());
        verify(chatMapper).listConversations(1L,null,null,3);
        verify(chatMapper).selectMembers(1L,java.util.List.of(10L,11L));verify(chatMapper).selectLastMessages(java.util.List.of(10L,11L));verify(chatMapper).selectUnreadCounts(1L,java.util.List.of(10L,11L));
        verifyNoMoreInteractions(chatMapper);verify(identityService,never()).findById(anyLong());
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
        closed.setMessagePermissionReason("CONTACT_UNAVAILABLE");
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

    @Test
    void messagePagesPreserveCursorDirectionAndHideUnknownSenderIdentity() {
        stubConversationLookup();
        var older = message(101L,10L,1L,2L,"first","older");
        var newer = message(102L,10L,2L,2L,"second","newer");
        var excluded = message(103L,10L,3L,2L,"third","excluded");
        when(chatMapper.selectMessages(10L,null,null,3)).thenReturn(java.util.List.of(newer,older,excluded));
        var initial=chatService.listMessages("sid","10",null,null,2);
        assertEquals(java.util.List.of("1","2"),initial.getItems().stream().map(ChatMessageDTO::getSeq).toList());
        assertEquals("1",initial.getNextCursor());assertTrue(initial.isHasMore());assertEquals("",initial.getItems().get(0).getSenderId());
        when(chatMapper.selectMessages(10L,4L,null,3)).thenReturn(java.util.List.of(newer,older,excluded));
        assertEquals("1",chatService.listMessages("sid","10","4",null,2).getNextCursor());
        when(chatMapper.selectMessages(10L,null,0L,3)).thenReturn(java.util.List.of(older,newer,excluded));
        assertEquals("2",chatService.listMessages("sid","10",null,"0",2).getNextCursor());
        assertThrows(SocialException.class,()->chatService.listMessages("sid","10","1","2",2));
        assertThrows(SocialException.class,()->chatService.listMessages("sid","10","bad",null,2));
        when(chatMapper.selectMessages(10L,null,null,21)).thenReturn(java.util.List.of());
        assertFalse(chatService.listMessages("sid","10"," ",null,0).isHasMore());
        when(chatMapper.selectMessages(10L,null,null,51)).thenReturn(java.util.List.of());
        assertNull(chatService.listMessages("sid","10",null,null,100).getNextCursor());
    }

    @Test
    void conversationCreationLocksOwnersAndInitializesBothMembers() {
        when(identityService.requireActiveViewer("sid")).thenReturn(me);
        when(identityService.requireActiveByPublicId(peer.getPublicId())).thenReturn(peer);
        when(identityService.requireActiveById(1L)).thenReturn(me);when(identityService.requireActiveById(2L)).thenReturn(peer);
        when(identityService.evaluateMessagePermission(me,peer)).thenReturn(SocialIdentityService.MessagePermission.allowed());
        doAnswer(inv->{ConversationEntity c=inv.getArgument(0);c.setId(10L);return 1;}).when(chatMapper).insertConversation(any());
        when(identityService.findById(2L)).thenReturn(peer);
        var peerDto=new SocialUserDTO();peerDto.setId(peer.getPublicId());peerDto.setCanMessage(true);
        when(identityService.buildSocialUser(me,peer)).thenReturn(peerDto);
        var dto=chatService.createOrGetConversation("sid",peer.getPublicId());
        assertEquals("10",dto.getId());assertTrue(dto.isCanSend());assertEquals("0",dto.getLastReadSeq());
        verify(identityService).lockUsersInOrder(1L,2L);verify(chatMapper).insertMember(10L,1L);verify(chatMapper).insertMember(10L,2L);
        when(chatMapper.selectConversationByPair(1L,2L)).thenReturn(conversation);
        assertEquals("10",chatService.createOrGetConversation("sid",peer.getPublicId()).getId());
        verify(chatMapper,times(1)).insertConversation(any());
    }

    @Test
    void unreadAndConversationCursorAreBoundedAndMalformedIdsNeverQuery() {
        when(identityService.requireActiveViewer("sid")).thenReturn(me);
        when(chatMapper.countTotalUnread(1L)).thenReturn(7);
        assertEquals(7,chatService.unreadTotal("sid").get("total"));
        String cursor=java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("1000:10".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(chatMapper.listConversations(1L,new Date(1000),10L,51)).thenReturn(java.util.List.of());
        assertFalse(chatService.listConversations("sid",cursor,999).isHasMore());
        verify(chatMapper).listConversations(1L,new Date(1000),10L,51);
        assertThrows(SocialException.class,()->chatService.listConversations("sid","broken",20));
        assertThrows(SocialException.class,()->chatService.getConversation("sid","not-an-id"));
        verify(chatMapper,never()).selectConversationById(anyLong());
    }

    private static byte[] tinyPng() {
        return java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
    }

    private static CampusAccountView user(long id, String publicId, String username) {
        CampusAccountView entity = new CampusAccountView();
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
