package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.mapper.SocialRelationMapper;
import cn.gdeiassistant.core.social.pojo.dto.PageDTO;
import cn.gdeiassistant.core.social.pojo.dto.SocialUserDTO;
import cn.gdeiassistant.core.social.websocket.SocialRealtimeHub;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocialRelationService {

    @Autowired
    private SocialIdentityService identityService;
    @Autowired
    private SocialRelationMapper relationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrivacyMapper privacyMapper;
    @Autowired
    private SocialRealtimeHub realtimeHub;

    public SocialUserDTO getMe(String sessionId) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        return identityService.buildSocialUser(me, me);
    }

    public SocialUserDTO getUser(String sessionId, String publicId) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        UserEntity target = identityService.requireActiveByPublicId(publicId);
        if (!me.getId().equals(target.getId())
                && relationMapper.countAnyBlock(me.getId(), target.getId()) > 0) {
            throw SocialException.userNotFound();
        }
        return identityService.buildSocialUser(me, target);
    }

    public PageDTO<SocialUserDTO> searchUsers(String sessionId, String query, String cursor, Integer limit) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        int size = normalizeLimit(limit);
        Cursor c = decodeCursor(cursor);
        List<UserEntity> rows = userMapper.searchActiveUsers(
                query == null ? "" : query.trim(),
                me.getId(),
                c == null ? null : c.at,
                c == null ? null : c.id,
                size + 1);
        boolean hasMore = rows.size() > size;
        List<SocialUserDTO> items = new ArrayList<>();
        int end = Math.min(rows.size(), size);
        for (int i = 0; i < end; i++) {
            items.add(identityService.buildSocialUser(me, rows.get(i)));
        }
        String next = null;
        if (hasMore && end > 0) {
            UserEntity last = rows.get(end - 1);
            next = encodeCursor(last.getCreatedAt(), last.getId());
        }
        return new PageDTO<>(items, next, hasMore);
    }

    public PageDTO<SocialUserDTO> listRelationships(String sessionId, String publicId, String kind,
                                                    String cursor, Integer limit) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        UserEntity owner = identityService.requireActiveByPublicId(publicId);
        if (!me.getId().equals(owner.getId())
                && relationMapper.countAnyBlock(me.getId(), owner.getId()) > 0) {
            throw SocialException.userNotFound();
        }
        int size = normalizeLimit(limit);
        Cursor c = decodeCursor(cursor);
        List<Map<String, Object>> rows;
        switch (kind == null ? "" : kind.trim().toLowerCase()) {
            case "following" -> rows = relationMapper.listFollowing(owner.getId(), me.getId(),
                    c == null ? null : c.at, c == null ? null : c.id, size + 1);
            case "followers" -> rows = relationMapper.listFollowers(owner.getId(), me.getId(),
                    c == null ? null : c.at, c == null ? null : c.id, size + 1);
            case "friends" -> rows = relationMapper.listFriends(owner.getId(), me.getId(),
                    c == null ? null : c.at, c == null ? null : c.id, size + 1);
            default -> throw SocialException.invalidRequest("kind 必须为 following|followers|friends");
        }
        return toUserPage(me, rows, size, false);
    }

    public PageDTO<SocialUserDTO> listBlocks(String sessionId, String cursor, Integer limit) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        int size = normalizeLimit(limit);
        Cursor c = decodeCursor(cursor);
        List<Map<String, Object>> rows = relationMapper.listBlocks(me.getId(),
                c == null ? null : c.at, c == null ? null : c.id, size + 1);
        return toUserPage(me, rows, size, true);
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public SocialUserDTO follow(String sessionId, String publicId) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        UserEntity target = identityService.requireActiveByPublicId(publicId);
        if (me.getId().equals(target.getId())) {
            throw SocialException.invalidRequest("不能关注自己");
        }
        identityService.lockUsersInOrder(me.getId(), target.getId());
        me = identityService.requireActiveById(me.getId());
        target = identityService.requireActiveById(target.getId());
        if (relationMapper.countAnyBlock(me.getId(), target.getId()) > 0) {
            throw SocialException.contactUnavailable();
        }
        relationMapper.insertFollow(me.getId(), target.getId());
        notifySocialChanged(me.getId(), target.getId());
        return identityService.buildSocialUser(me, target);
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public SocialUserDTO unfollow(String sessionId, String publicId) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        UserEntity target = identityService.requireActiveByPublicId(publicId);
        identityService.lockUsersInOrder(me.getId(), target.getId());
        me = identityService.requireActiveById(me.getId());
        target = identityService.findById(target.getId());
        if (target == null || !target.isActive()) {
            throw SocialException.userNotFound();
        }
        relationMapper.deleteFollow(me.getId(), target.getId());
        notifySocialChanged(me.getId(), target.getId());
        return identityService.buildSocialUser(me, target);
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> block(String sessionId, String publicId) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        UserEntity target = identityService.requireActiveByPublicId(publicId);
        if (me.getId().equals(target.getId())) {
            throw SocialException.invalidRequest("不能拉黑自己");
        }
        identityService.lockUsersInOrder(me.getId(), target.getId());
        me = identityService.requireActiveById(me.getId());
        target = identityService.requireActiveById(target.getId());
        relationMapper.insertBlock(me.getId(), target.getId());
        relationMapper.deleteMutualFollows(me.getId(), target.getId());
        notifySocialChanged(me.getId(), target.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("blocked", true);
        return data;
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> unblock(String sessionId, String publicId) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        UserEntity target = identityService.requireActiveByPublicId(publicId);
        identityService.lockUsersInOrder(me.getId(), target.getId());
        me = identityService.requireActiveById(me.getId());
        target = identityService.requireActiveById(target.getId());
        relationMapper.deleteBlock(me.getId(), target.getId());
        notifySocialChanged(me.getId(), target.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("blocked", false);
        return data;
    }

    public Map<String, Object> getPrivacy(String sessionId) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        PrivacyEntity privacy = privacyMapper.selectPrivacy(me.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("dmPolicy", identityService.normalizeDmPolicy(privacy));
        return data;
    }

    @Transactional(value = "appTransactionManager", isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> updatePrivacy(String sessionId, String dmPolicy) {
        UserEntity me = identityService.requireActiveViewer(sessionId);
        String normalized = dmPolicy == null ? "" : dmPolicy.trim().toUpperCase();
        if (!List.of("ALL", "FOLLOWING", "MUTUAL", "NONE").contains(normalized)) {
            throw SocialException.invalidRequest("dmPolicy 无效");
        }
        identityService.lockUsersInOrder(me.getId());
        me = identityService.requireActiveById(me.getId());
        privacyMapper.updateDmPolicy(normalized, me.getUsername());
        notifySocialChanged(me.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("dmPolicy", normalized);
        return data;
    }

    private PageDTO<SocialUserDTO> toUserPage(UserEntity me, List<Map<String, Object>> rows,
                                              int size, boolean allowOwnBlocks) {
        List<SocialUserDTO> items = new ArrayList<>();
        boolean hasMore = rows != null && rows.size() > size;
        int end = rows == null ? 0 : Math.min(rows.size(), size);
        for (int i = 0; i < end; i++) {
            Map<String, Object> row = rows.get(i);
            long userId = ((Number) row.get("userId")).longValue();
            UserEntity target = identityService.findById(userId);
            if (target == null) {
                continue;
            }
            if (!allowOwnBlocks && !target.isActive()) {
                continue;
            }
            if (!target.isActive() && allowOwnBlocks) {
                items.add(identityService.buildClosedPeer(target));
                continue;
            }
            items.add(identityService.buildSocialUser(me, target));
        }
        String next = null;
        if (hasMore && end > 0) {
            Map<String, Object> last = rows.get(end - 1);
            Date at = (Date) last.get("relationAt");
            Long id = ((Number) last.get("userId")).longValue();
            next = encodeCursor(at, id);
        }
        return new PageDTO<>(items, next, hasMore);
    }

    private void notifySocialChanged(long... userIds) {
        for (long userId : userIds) {
            realtimeHub.pushToUserAfterCommit(userId, Map.of("type", "social.changed"));
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 20;
        }
        return Math.min(limit, 50);
    }

    private String encodeCursor(Date at, Long id) {
        if (at == null || id == null) {
            return null;
        }
        String raw = at.getTime() + ":" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private Cursor decodeCursor(String cursor) {
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
