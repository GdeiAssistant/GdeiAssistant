package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.image.ChatImageCodec;
import cn.gdeiassistant.core.social.mapper.SocialChatMapper;
import cn.gdeiassistant.core.social.pojo.dto.ChatImageDTO;
import cn.gdeiassistant.core.social.pojo.dto.ChatMessageDTO;
import cn.gdeiassistant.core.social.pojo.dto.ConversationDTO;
import cn.gdeiassistant.core.social.pojo.dto.PageDTO;
import cn.gdeiassistant.core.social.pojo.dto.SocialUserDTO;
import cn.gdeiassistant.core.social.pojo.entity.ChatMessageEntity;
import cn.gdeiassistant.core.social.pojo.entity.ConversationEntity;
import cn.gdeiassistant.core.social.pojo.entity.ConversationMemberEntity;
import cn.gdeiassistant.core.social.websocket.SocialRealtimeHub;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class SocialChatService {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Autowired
    private SocialIdentityService identityService;
    @Autowired
    private SocialChatMapper chatMapper;
    @Autowired
    private SocialRealtimeHub realtimeHub;
    @Autowired
    private SocialChatImageService chatImageService;

    public Map<String, Object> unreadTotal(String sessionId) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        Map<String, Object> data = new HashMap<>();
        data.put("total", chatMapper.countTotalUnread(me.getId()));
        return data;
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public ConversationDTO createOrGetConversation(String sessionId, String peerPublicId) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        CampusAccountView peer = identityService.requireActiveByPublicId(peerPublicId);
        if (me.getId().equals(peer.getId())) {
            throw SocialException.invalidRequest("不能与自己创建会话");
        }
        identityService.lockUsersInOrder(me.getId(), peer.getId());
        me = identityService.requireActiveById(me.getId());
        peer = identityService.requireActiveById(peer.getId());
        SocialIdentityService.MessagePermission permission = identityService.evaluateMessagePermission(me, peer);
        long low = Math.min(me.getId(), peer.getId());
        long high = Math.max(me.getId(), peer.getId());
        ConversationEntity existing = chatMapper.selectConversationByPair(low, high);
        if (existing == null) {
            if (!permission.allowed && "CONTACT_UNAVAILABLE".equals(permission.reason)) {
                throw SocialException.contactUnavailable();
            }
            if (!permission.allowed) {
                throw SocialException.privacyRestricted();
            }
            ConversationEntity created = new ConversationEntity();
            created.setUserLowId(low);
            created.setUserHighId(high);
            try {
                chatMapper.insertConversation(created);
                chatMapper.insertMember(created.getId(), me.getId());
                chatMapper.insertMember(created.getId(), peer.getId());
            } catch (DuplicateKeyException ex) {
                created = chatMapper.selectConversationByPair(low, high);
            }
            existing = created;
        }
        return toConversationDTO(me, existing);
    }

    public PageDTO<ConversationDTO> listConversations(String sessionId, String cursor, Integer limit) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        int size = normalizeLimit(limit);
        Cursor c = decodeTimeCursor(cursor);
        List<ConversationEntity> rows = chatMapper.listConversations(me.getId(),
                c == null ? null : c.at, c == null ? null : c.id, size + 1);
        boolean hasMore = rows.size() > size;
        List<ConversationDTO> items = new ArrayList<>();
        int end = Math.min(rows.size(), size);
        var page = rows.subList(0, end);
        if (!page.isEmpty()) {
            var ids = page.stream().map(ConversationEntity::getId).toList();
            var peers = identityService.buildSocialUsers(me, page.stream().map(row -> peerIdOf(row, me.getId())).toList());
            var members = chatMapper.selectMembers(me.getId(), ids).stream().collect(java.util.stream.Collectors.toMap(ConversationMemberEntity::getConversationId, member -> member));
            var messages = chatMapper.selectLastMessages(ids).stream().collect(java.util.stream.Collectors.toMap(ChatMessageEntity::getConversationId, message -> message));
            var unread = new HashMap<Long,Integer>();
            for (var count : chatMapper.selectUnreadCounts(me.getId(), ids)) unread.put(((Number) count.get("conversationId")).longValue(), ((Number) count.get("unreadCount")).intValue());
            for (var row : page) {
                var peer = peers.getOrDefault(peerIdOf(row, me.getId()), identityService.buildClosedPeer(null));
                var member = members.get(row.getId());
                var last = messages.get(row.getId());
                items.add(conversationDTO(row, peer, member, last == null ? null : toMessageDTO(last,
                        last.getSenderId().equals(me.getId()) ? me.getPublicId() : peer.getId()), unread.getOrDefault(row.getId(), 0)));
            }
        }
        String next = null;
        if (hasMore && end > 0) {
            ConversationEntity last = rows.get(end - 1);
            next = encodeTimeCursor(last.getLastMessageAt() != null ? last.getLastMessageAt() : last.getCreatedAt(), last.getId());
        }
        return new PageDTO<>(items, next, hasMore);
    }

    public ConversationDTO getConversation(String sessionId, String conversationId) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        ConversationEntity conversation = requireMemberConversation(me.getId(), conversationId);
        return toConversationDTO(me, conversation);
    }

    public PageDTO<ChatMessageDTO> listMessages(String sessionId, String conversationId,
                                                String beforeSeq, String afterSeq, Integer limit) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        ConversationEntity conversation = requireMemberConversation(me.getId(), conversationId);
        if (beforeSeq != null && !beforeSeq.isBlank() && afterSeq != null && !afterSeq.isBlank()) {
            throw SocialException.invalidRequest("beforeSeq 与 afterSeq 互斥");
        }
        int size = normalizeLimit(limit);
        Long before = parseOptionalLong(beforeSeq, "beforeSeq");
        Long after = parseOptionalLong(afterSeq, "afterSeq");
        List<ChatMessageEntity> rows = chatMapper.selectMessages(conversation.getId(), before, after, size + 1);
        boolean hasMore = rows.size() > size;
        List<ChatMessageEntity> page = new ArrayList<>(rows.subList(0, Math.min(rows.size(), size)));
        if (after == null) {
            Collections.reverse(page);
        }
        List<ChatMessageDTO> items = new ArrayList<>();
        for (ChatMessageEntity row : page) {
            items.add(toMessageDTO(row));
        }
        String nextCursor = null;
        if (hasMore && !page.isEmpty()) {
            if (before != null) {
                nextCursor = String.valueOf(page.get(0).getSeq());
            } else if (after != null) {
                nextCursor = String.valueOf(page.get(page.size() - 1).getSeq());
            } else {
                nextCursor = String.valueOf(page.get(0).getSeq());
            }
        }
        return new PageDTO<>(items, hasMore ? nextCursor : null, hasMore);
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public ChatMessageDTO sendMessage(String sessionId, String conversationId, String clientMessageId, String content) {
        String trimmed = content == null ? "" : content.trim();
        int unicodeLen = trimmed.codePointCount(0, trimmed.length());
        if (unicodeLen < 1 || unicodeLen > 1000) {
            throw SocialException.invalidRequest("消息长度须为 1–1000 个 Unicode 字符");
        }
        String clientId = requireClientMessageId(clientMessageId);
        return commitNewMessage(sessionId, conversationId, clientId,
                existing -> sameTextPayload(existing, trimmed),
                message -> {
                    message.setType(TYPE_TEXT);
                    message.setContent(trimmed);
                });
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public ChatMessageDTO sendImageMessage(String sessionId, String conversationId,
                                           String clientMessageId, byte[] rawImage, String claimedContentType) {
        String clientId = requireClientMessageId(clientMessageId);

        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        ConversationEntity conversation = requireMemberConversation(me.getId(), conversationId);
        ChatMessageEntity existing = chatMapper.selectByClientMessageId(conversation.getId(), me.getId(), clientId);
        if (existing != null && !TYPE_IMAGE.equals(normalizeType(existing.getType()))) {
            throw SocialException.clientMessageConflict();
        }
        ChatImageCodec.CanonicalImage canonical = ChatImageCodec.canonicalize(rawImage, claimedContentType);
        if (existing != null) {
            return confirmExistingImage(existing, canonical.sha256Hex);
        }
        if (!chatImageService.isImageMessagingEnabled()) {
            throw SocialException.imageUnavailable();
        }

        long peerId = peerIdOf(conversation, me.getId());
        identityService.lockUsersInOrder(me.getId(), peerId);
        me = identityService.requireActiveById(me.getId());
        existing = chatMapper.selectByClientMessageId(conversation.getId(), me.getId(), clientId);
        if (existing != null) {
            return confirmExistingImage(existing, canonical.sha256Hex);
        }
        CampusAccountView peer = identityService.findById(peerId);
        if (peer == null || !peer.isActive()) {
            throw SocialException.contactUnavailable();
        }
        SocialIdentityService.MessagePermission permission = identityService.evaluateMessagePermission(me, peer);
        if (!permission.allowed) {
            if ("CONTACT_UNAVAILABLE".equals(permission.reason)) {
                throw SocialException.contactUnavailable();
            }
            throw SocialException.privacyRestricted();
        }
        ConversationEntity locked = chatMapper.selectConversationByIdForUpdate(conversation.getId());
        if (locked == null) {
            throw SocialException.conversationNotFound();
        }
        existing = chatMapper.selectByClientMessageId(locked.getId(), me.getId(), clientId);
        if (existing != null) {
            return confirmExistingImage(existing, canonical.sha256Hex);
        }

        String objectKey = chatImageService.newObjectKey();
        chatImageService.uploadCanonical(objectKey, canonical);
        chatImageService.registerRollbackDelete(objectKey);

        long nextSeq = (locked.getLastSeq() == null ? 0L : locked.getLastSeq()) + 1L;
        Date now = new Date();
        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversationId(locked.getId());
        message.setSeq(nextSeq);
        message.setSenderId(me.getId());
        message.setClientMessageId(clientId);
        message.setType(TYPE_IMAGE);
        message.setContent("");
        message.setImageKey(objectKey);
        message.setImageContentType(canonical.contentType);
        message.setImageWidth(canonical.width);
        message.setImageHeight(canonical.height);
        message.setImageSize(canonical.size());
        message.setImageSha256(canonical.sha256Hex);
        message.setCreatedAt(now);
        try {
            chatMapper.insertMessage(message);
        } catch (DuplicateKeyException ex) {
            ChatMessageEntity raced = chatMapper.selectByClientMessageId(locked.getId(), me.getId(), clientId);
            if (raced != null) {
                // 同 ID 已提交：返回原消息；多余对象尽量删除，失败只记诊断不改幂等成功语义。
                ChatMessageDTO confirmed = confirmExistingImage(raced, canonical.sha256Hex);
                try {
                    chatImageService.deleteStrictOrDiagnose(objectKey, "duplicate-client-message");
                } catch (SocialException ignored) {
                    // 已留下诊断日志；原消息确认不受影响
                }
                return confirmed;
            }
            throw SocialException.invalidRequest("消息发送冲突，请重试");
        }
        chatMapper.updateConversationSummary(locked.getId(), nextSeq, now);
        ChatMessageDTO dto = toMessageDTO(message);
        pushMessageCreated(locked.getId(), me.getId(), peerId, dto);
        return dto;
    }

    /**
     * 历史读图：会话成员 + 消息属于该会话即可；不按当前 canSend / 拉黑 / 隐私判断。
     */
    public ImageDownload downloadImage(String sessionId, String conversationId, String messageId) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        ConversationEntity conversation = requireMemberConversation(me.getId(), conversationId);
        long mid;
        try {
            mid = Long.parseLong(messageId);
        } catch (Exception ex) {
            throw SocialException.conversationNotFound();
        }
        ChatMessageEntity message = chatMapper.selectByIdInConversation(conversation.getId(), mid);
        if (message == null || !TYPE_IMAGE.equals(normalizeType(message.getType()))) {
            throw SocialException.conversationNotFound();
        }
        if (message.getImageKey() == null || message.getImageKey().isBlank()) {
            throw SocialException.conversationNotFound();
        }
        if (!chatImageService.isImageMessagingEnabled()) {
            throw SocialException.imageUnavailable();
        }
        byte[] bytes = chatImageService.downloadBounded(message.getImageKey());
        if (bytes == null) {
            return null;
        }
        String contentType = message.getImageContentType() == null || message.getImageContentType().isBlank()
                ? ChatImageCodec.MIME_JPEG
                : message.getImageContentType();
        return new ImageDownload(bytes, contentType);
    }

    public static final class ImageDownload {
        public final byte[] bytes;
        public final String contentType;

        public ImageDownload(byte[] bytes, String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> markRead(String sessionId, String conversationId, String lastReadSeqRaw) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        ConversationEntity conversation = requireMemberConversation(me.getId(), conversationId);
        long lastReadSeq;
        try {
            lastReadSeq = Long.parseLong(lastReadSeqRaw);
        } catch (Exception ex) {
            throw SocialException.invalidRequest("lastReadSeq 无效");
        }
        if (lastReadSeq < 0) {
            throw SocialException.invalidRequest("lastReadSeq 无效");
        }
        ConversationEntity locked = chatMapper.selectConversationByIdForUpdate(conversation.getId());
        if (locked == null) {
            throw SocialException.conversationNotFound();
        }
        long maxSeq = locked.getLastSeq() == null ? 0L : locked.getLastSeq();
        if (lastReadSeq > maxSeq) {
            throw SocialException.invalidRequest("lastReadSeq 不能超过已提交序号");
        }
        ConversationMemberEntity member = chatMapper.selectMemberForUpdate(locked.getId(), me.getId());
        if (member == null) {
            throw SocialException.conversationNotFound();
        }
        long current = member.getLastReadSeq() == null ? 0L : member.getLastReadSeq();
        if (lastReadSeq > current) {
            chatMapper.advanceReadSeq(locked.getId(), me.getId(), lastReadSeq);
            ConversationMemberEntity refreshed = chatMapper.selectMember(locked.getId(), me.getId());
            current = refreshed == null || refreshed.getLastReadSeq() == null ? lastReadSeq : refreshed.getLastReadSeq();
        }
        long peerId = peerIdOf(locked, me.getId());
        int unread = chatMapper.countUnreadFromPeer(locked.getId(), peerId, current);
        Map<String, Object> event = new HashMap<>();
        event.put("type", "conversation.read");
        event.put("conversationId", String.valueOf(locked.getId()));
        event.put("readerId", me.getPublicId());
        event.put("lastReadSeq", String.valueOf(current));
        realtimeHub.pushToUserAfterCommit(peerId, event);
        realtimeHub.pushToUserAfterCommit(me.getId(), event);
        Map<String, Object> data = new HashMap<>();
        data.put("lastReadSeq", String.valueOf(current));
        data.put("unreadCount", unread);
        return data;
    }

    /**
     * 公用发送步骤：已提交先查 / 账号升序锁 / 锁后再查 / 权限 / 会话锁 / seq / 插入 / 提交后推送。
     */
    private ChatMessageDTO commitNewMessage(String sessionId, String conversationId, String clientId,
                                            Predicate<ChatMessageEntity> samePayload,
                                            Consumer<ChatMessageEntity> populate) {
        CampusAccountView me = identityService.requireActiveViewer(sessionId);
        ConversationEntity conversation = requireMemberConversation(me.getId(), conversationId);
        ChatMessageEntity existing = chatMapper.selectByClientMessageId(conversation.getId(), me.getId(), clientId);
        if (existing != null) {
            return confirmExisting(existing, samePayload);
        }
        long peerId = peerIdOf(conversation, me.getId());
        identityService.lockUsersInOrder(me.getId(), peerId);
        me = identityService.requireActiveById(me.getId());
        // 等待账号锁期间，原发送请求可能已经提交；先确认结果再应用新隐私。
        existing = chatMapper.selectByClientMessageId(conversation.getId(), me.getId(), clientId);
        if (existing != null) {
            return confirmExisting(existing, samePayload);
        }
        CampusAccountView peer = identityService.findById(peerId);
        if (peer == null || !peer.isActive()) {
            throw SocialException.contactUnavailable();
        }
        SocialIdentityService.MessagePermission permission = identityService.evaluateMessagePermission(me, peer);
        if (!permission.allowed) {
            if ("CONTACT_UNAVAILABLE".equals(permission.reason)) {
                throw SocialException.contactUnavailable();
            }
            throw SocialException.privacyRestricted();
        }
        ConversationEntity locked = chatMapper.selectConversationByIdForUpdate(conversation.getId());
        if (locked == null) {
            throw SocialException.conversationNotFound();
        }
        existing = chatMapper.selectByClientMessageId(locked.getId(), me.getId(), clientId);
        if (existing != null) {
            return confirmExisting(existing, samePayload);
        }
        long nextSeq = (locked.getLastSeq() == null ? 0L : locked.getLastSeq()) + 1L;
        Date now = new Date();
        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversationId(locked.getId());
        message.setSeq(nextSeq);
        message.setSenderId(me.getId());
        message.setClientMessageId(clientId);
        message.setCreatedAt(now);
        populate.accept(message);
        try {
            chatMapper.insertMessage(message);
        } catch (DuplicateKeyException ex) {
            ChatMessageEntity raced = chatMapper.selectByClientMessageId(locked.getId(), me.getId(), clientId);
            if (raced != null) {
                return confirmExisting(raced, samePayload);
            }
            throw SocialException.invalidRequest("消息发送冲突，请重试");
        }
        chatMapper.updateConversationSummary(locked.getId(), nextSeq, now);
        ChatMessageDTO dto = toMessageDTO(message);
        pushMessageCreated(locked.getId(), me.getId(), peerId, dto);
        return dto;
    }

    private ChatMessageDTO confirmExisting(ChatMessageEntity existing, Predicate<ChatMessageEntity> samePayload) {
        if (!samePayload.test(existing)) {
            throw SocialException.clientMessageConflict();
        }
        return toMessageDTO(existing);
    }

    private ChatMessageDTO confirmExistingImage(ChatMessageEntity existing, String sha256Hex) {
        if (!TYPE_IMAGE.equals(normalizeType(existing.getType()))
                || !Objects.equals(sha256Hex, existing.getImageSha256())) {
            throw SocialException.clientMessageConflict();
        }
        return toMessageDTO(existing);
    }

    private static boolean sameTextPayload(ChatMessageEntity existing, String trimmed) {
        String type = normalizeType(existing.getType());
        if (!TYPE_TEXT.equals(type)) {
            return false;
        }
        return trimmed.equals(existing.getContent());
    }

    private void pushMessageCreated(long conversationId, long senderId, long peerId, ChatMessageDTO dto) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "message.created");
        event.put("conversationId", String.valueOf(conversationId));
        event.put("messageId", dto.getId());
        event.put("seq", dto.getSeq());
        realtimeHub.pushToUserAfterCommit(senderId, event);
        realtimeHub.pushToUserAfterCommit(peerId, event);
    }

    private ConversationEntity requireMemberConversation(long userId, String conversationIdRaw) {
        long conversationId;
        try {
            conversationId = Long.parseLong(conversationIdRaw);
        } catch (Exception ex) {
            throw SocialException.conversationNotFound();
        }
        ConversationMemberEntity member = chatMapper.selectMember(conversationId, userId);
        if (member == null) {
            throw SocialException.conversationNotFound();
        }
        ConversationEntity conversation = chatMapper.selectConversationById(conversationId);
        if (conversation == null) {
            throw SocialException.conversationNotFound();
        }
        return conversation;
    }

    private ConversationDTO toConversationDTO(CampusAccountView me, ConversationEntity conversation) {
        long peerId = peerIdOf(conversation, me.getId());
        CampusAccountView peer = identityService.findById(peerId);
        SocialUserDTO peerDto;
        boolean canSend;
        String reason;
        if (peer == null || !peer.isActive()) {
            peerDto = identityService.buildClosedPeer(peer);
            canSend = false;
            reason = "CONTACT_UNAVAILABLE";
        } else {
            peerDto = identityService.buildSocialUser(me, peer);
            canSend = peerDto.isCanMessage();
            reason = peerDto.getMessagePermissionReason();
        }
        ConversationMemberEntity member = chatMapper.selectMember(conversation.getId(), me.getId());
        long lastRead = member == null || member.getLastReadSeq() == null ? 0L : member.getLastReadSeq();
        ChatMessageEntity last = chatMapper.selectLastMessage(conversation.getId());
        return conversationDTO(conversation, peerDto, member, last == null ? null : toMessageDTO(last),
                chatMapper.countUnreadFromPeer(conversation.getId(), peerId, lastRead));
    }

    private ConversationDTO conversationDTO(ConversationEntity conversation, SocialUserDTO peer,
            ConversationMemberEntity member, ChatMessageDTO last, int unread) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(String.valueOf(conversation.getId()));
        dto.setPeer(peer);
        dto.setLastMessage(last);
        dto.setUpdatedAt(formatTime(conversation.getLastMessageAt() != null ? conversation.getLastMessageAt() : conversation.getCreatedAt()));
        dto.setUnreadCount(unread);
        dto.setLastReadSeq(String.valueOf(member == null || member.getLastReadSeq() == null ? 0 : member.getLastReadSeq()));
        dto.setCanSend(peer.isCanMessage());
        dto.setSendPermissionReason(peer.getMessagePermissionReason());
        dto.setImageMessagingEnabled(chatImageService.isImageMessagingEnabled());
        return dto;
    }

    private ChatMessageDTO toMessageDTO(ChatMessageEntity entity) {
        CampusAccountView sender = identityService.findById(entity.getSenderId());
        return toMessageDTO(entity, sender != null && sender.getPublicId() != null ? sender.getPublicId() : "");
    }

    private ChatMessageDTO toMessageDTO(ChatMessageEntity entity, String senderPublicId) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(String.valueOf(entity.getId()));
        dto.setConversationId(String.valueOf(entity.getConversationId()));
        dto.setSeq(String.valueOf(entity.getSeq()));
        dto.setSenderId(senderPublicId);
        dto.setClientMessageId(entity.getClientMessageId());
        String type = normalizeType(entity.getType());
        dto.setType(type);
        if (TYPE_IMAGE.equals(type)) {
            dto.setContent("");
            ChatImageDTO image = new ChatImageDTO();
            image.setUrl("/api/social/conversations/" + entity.getConversationId()
                    + "/messages/" + entity.getId() + "/image");
            image.setWidth(entity.getImageWidth() == null ? 0 : entity.getImageWidth());
            image.setHeight(entity.getImageHeight() == null ? 0 : entity.getImageHeight());
            image.setSize(entity.getImageSize() == null ? 0 : entity.getImageSize());
            image.setContentType(entity.getImageContentType());
            dto.setImage(image);
        } else {
            dto.setContent(entity.getContent());
            dto.setImage(null);
        }
        dto.setCreatedAt(formatTime(entity.getCreatedAt()));
        return dto;
    }

    static String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return TYPE_TEXT;
        }
        return type.trim();
    }

    private static long peerIdOf(ConversationEntity conversation, long meId) {
        return conversation.getUserLowId().equals(meId)
                ? conversation.getUserHighId() : conversation.getUserLowId();
    }

    private String requireClientMessageId(String clientMessageId) {
        if (clientMessageId == null || clientMessageId.isBlank()) {
            throw SocialException.invalidRequest("clientMessageId 必填");
        }
        try {
            UUID.fromString(clientMessageId.trim());
        } catch (IllegalArgumentException ex) {
            throw SocialException.invalidRequest("clientMessageId 必须为 UUID");
        }
        return clientMessageId.trim();
    }

    private String formatTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZONE).format(ISO);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 20;
        }
        return Math.min(limit, 50);
    }

    private Long parseOptionalLong(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw);
        } catch (Exception ex) {
            throw SocialException.invalidRequest(field + " 无效");
        }
    }

    private String encodeTimeCursor(Date at, Long id) {
        String raw = at.getTime() + ":" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private Cursor decodeTimeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split(":");
            return new Cursor(new Date(Long.parseLong(parts[0])), Long.parseLong(parts[1]));
        } catch (Exception e) {
            throw SocialException.invalidRequest("cursor 无效");
        }
    }

    private static final class Cursor {
        final Date at;
        final Long id;

        Cursor(Date at, Long id) {
            this.at = at;
            this.id = id;
        }
    }
}
