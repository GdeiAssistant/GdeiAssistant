package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.common.pojo.entity.Introduction;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.mapper.SocialRelationMapper;
import cn.gdeiassistant.core.social.pojo.dto.SocialUserDTO;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SocialIdentityService {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String REL_SELF = "SELF";
    public static final String REL_NONE = "NONE";
    public static final String REL_FOLLOWING = "FOLLOWING";
    public static final String REL_FOLLOWED_BY = "FOLLOWED_BY";
    public static final String REL_MUTUAL = "MUTUAL";

    @Autowired
    private UserCertificateService userCertificateService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProfileMapper profileMapper;
    @Autowired
    private PrivacyMapper privacyMapper;
    @Autowired
    private SocialRelationMapper socialRelationMapper;

    public CampusAccountView requireActiveViewer(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw SocialException.authRequired();
        }
        User login = userCertificateService.getUserLoginCertificate(sessionId);
        if (login == null || login.getUsername() == null) {
            throw SocialException.authRequired();
        }
        CampusAccountView entity = userMapper.selectUser(login.getUsername());
        if (entity == null || entity.getId() == null || !entity.isActive()) {
            throw SocialException.authRequired();
        }
        return entity;
    }

    public CampusAccountView requireActiveByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw SocialException.userNotFound();
        }
        CampusAccountView entity = userMapper.selectUserByPublicId(publicId.trim());
        if (entity == null || entity.getId() == null || !entity.isActive()) {
            throw SocialException.userNotFound();
        }
        return entity;
    }

    public CampusAccountView requireActiveById(long userId) {
        CampusAccountView entity = userMapper.selectUserById(userId);
        if (entity == null || entity.getId() == null || !entity.isActive()) {
            throw SocialException.userNotFound();
        }
        return entity;
    }

    public CampusAccountView findById(long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 固定升序锁定 app_user 行，配合 READ_COMMITTED 做 current read，避免隐私/关注与发信交叉竞态。
     */
    public void lockUsersInOrder(long... userIds) {
        long[] sorted = Arrays.stream(userIds).distinct().sorted().toArray();
        for (long id : sorted) {
            userMapper.selectUserByIdForUpdate(id);
        }
    }

    @Autowired
    private cn.gdeiassistant.core.social.mapper.SocialUserSummaryMapper summaries;

    public java.util.Map<Long,SocialUserDTO> buildSocialUsers(CampusAccountView viewer, java.util.Collection<Long> ids) {
        var result = new java.util.HashMap<Long,SocialUserDTO>();
        if (ids.isEmpty()) return result;
        for (var row : summaries.selectUsers(viewer.getId(), ids.stream().distinct().toList())) {
            long id = ((Number) row.get("userId")).longValue();
            if (!"ACTIVE".equals(row.get("status"))) {
                CampusAccountView closed = new CampusAccountView();
                closed.setPublicId((String) row.get("publicId"));
                result.put(id, buildClosedPeer(closed));
                continue;
            }
            boolean following = flag(row.get("following")), followedBy = flag(row.get("followedBy"));
            boolean self = viewer.getId() == id, blocked = flag(row.get("anyBlock"));
            String policy = String.valueOf(row.get("policy"));
            if (!java.util.Set.of("ALL","FOLLOWING","MUTUAL","NONE").contains(policy)) policy = "MUTUAL";
            boolean allowed = !self && viewer.isActive() && !blocked && switch (policy) {
                case "ALL" -> true;
                case "FOLLOWING" -> followedBy;
                case "MUTUAL" -> following && followedBy;
                default -> false;
            };
            SocialUserDTO dto = new SocialUserDTO();
            dto.setId((String) row.get("publicId"));
            dto.setNickname((String) row.get("nickname"));
            dto.setIntroduction((String) row.get("introduction"));
            dto.setAvatarUrl("/api/social/users/" + dto.getId() + "/avatar");
            dto.setFollowingCount(((Number) row.get("followingCount")).intValue());
            dto.setFollowerCount(((Number) row.get("followerCount")).intValue());
            dto.setFriendCount(((Number) row.get("friendCount")).intValue());
            dto.setRelationship(self ? REL_SELF : following && followedBy ? REL_MUTUAL : following ? REL_FOLLOWING : followedBy ? REL_FOLLOWED_BY : REL_NONE);
            dto.setBlockedByMe(flag(row.get("blockedByMe")));
            dto.setCanMessage(allowed);
            dto.setMessagePermissionReason(allowed ? null : self ? "SELF" : blocked || !viewer.isActive() ? "CONTACT_UNAVAILABLE" : "PRIVACY_RESTRICTED");
            result.put(id, dto);
        }
        return result;
    }

    private static boolean flag(Object value) { return Boolean.TRUE.equals(value) || value instanceof Number n && n.intValue() != 0; }

    public SocialUserDTO buildSocialUser(CampusAccountView viewer, CampusAccountView target) {
        SocialUserDTO dto = new SocialUserDTO();
        dto.setId(target.getPublicId());
        ProfileEntity profile = target.getUsername() == null ? null : profileMapper.selectUserProfile(target.getUsername());
        dto.setNickname(profile != null && profile.getNickname() != null ? profile.getNickname() : "用户");
        PrivacyEntity privacy = target.getUsername() == null ? null : privacyMapper.selectPrivacy(target.getUsername());
        // 简介隐私缺失不能默认公开
        boolean introOpen = viewer.getId().equals(target.getId())
                || (privacy != null && Boolean.TRUE.equals(privacy.getIntroductionOpen()));
        if (introOpen && target.getUsername() != null) {
            Introduction introduction = profileMapper.selectUserIntroduction(target.getUsername());
            if (introduction != null && introduction.getIntroductionContent() != null
                    && !introduction.getIntroductionContent().isBlank()) {
                dto.setIntroduction(introduction.getIntroductionContent());
            }
        }
        dto.setAvatarUrl("/api/social/users/" + target.getPublicId() + "/avatar");
        dto.setFollowingCount(socialRelationMapper.countFollowing(target.getId()));
        dto.setFollowerCount(socialRelationMapper.countFollowers(target.getId()));
        dto.setFriendCount(socialRelationMapper.countFriends(target.getId()));
        dto.setRelationship(resolveRelationship(viewer.getId(), target.getId()));
        dto.setBlockedByMe(socialRelationMapper.countBlock(viewer.getId(), target.getId()) > 0);
        MessagePermission permission = evaluateMessagePermission(viewer, target);
        dto.setCanMessage(permission.allowed);
        dto.setMessagePermissionReason(permission.reason);
        return dto;
    }

    public SocialUserDTO buildClosedPeer(CampusAccountView closed) {
        SocialUserDTO dto = new SocialUserDTO();
        dto.setId(closed != null && closed.getPublicId() != null ? closed.getPublicId() : "");
        dto.setNickname("已注销");
        dto.setIntroduction(null);
        dto.setAvatarUrl(null);
        dto.setFollowingCount(0);
        dto.setFollowerCount(0);
        dto.setFriendCount(0);
        dto.setRelationship(REL_NONE);
        dto.setBlockedByMe(false);
        dto.setCanMessage(false);
        dto.setMessagePermissionReason("CONTACT_UNAVAILABLE");
        return dto;
    }

    public String resolveRelationship(long viewerId, long targetId) {
        if (viewerId == targetId) {
            return REL_SELF;
        }
        boolean following = socialRelationMapper.countFollow(viewerId, targetId) > 0;
        boolean followedBy = socialRelationMapper.countFollow(targetId, viewerId) > 0;
        if (following && followedBy) {
            return REL_MUTUAL;
        }
        if (following) {
            return REL_FOLLOWING;
        }
        if (followedBy) {
            return REL_FOLLOWED_BY;
        }
        return REL_NONE;
    }

    public MessagePermission evaluateMessagePermission(CampusAccountView sender, CampusAccountView receiver) {
        if (sender.getId().equals(receiver.getId())) {
            return MessagePermission.denied("SELF");
        }
        if (!sender.isActive() || !receiver.isActive()) {
            return MessagePermission.denied("CONTACT_UNAVAILABLE");
        }
        if (socialRelationMapper.countAnyBlock(sender.getId(), receiver.getId()) > 0) {
            return MessagePermission.denied("CONTACT_UNAVAILABLE");
        }
        String policy = normalizeDmPolicy(receiver.getUsername() == null ? null
                : privacyMapper.selectPrivacy(receiver.getUsername()));
        return switch (policy) {
            case "ALL" -> MessagePermission.allowed();
            case "FOLLOWING" -> socialRelationMapper.countFollow(receiver.getId(), sender.getId()) > 0
                    ? MessagePermission.allowed()
                    : MessagePermission.denied("PRIVACY_RESTRICTED");
            case "MUTUAL" -> (socialRelationMapper.countFollow(receiver.getId(), sender.getId()) > 0
                    && socialRelationMapper.countFollow(sender.getId(), receiver.getId()) > 0)
                    ? MessagePermission.allowed()
                    : MessagePermission.denied("PRIVACY_RESTRICTED");
            case "NONE" -> MessagePermission.denied("PRIVACY_RESTRICTED");
            default -> MessagePermission.denied("PRIVACY_RESTRICTED");
        };
    }

    public String normalizeDmPolicy(PrivacyEntity privacy) {
        if (privacy == null || privacy.getDmPolicy() == null || privacy.getDmPolicy().isBlank()) {
            return "MUTUAL";
        }
        String policy = privacy.getDmPolicy().trim().toUpperCase();
        return switch (policy) {
            case "ALL", "FOLLOWING", "MUTUAL", "NONE" -> policy;
            default -> "MUTUAL";
        };
    }

    public static final class MessagePermission {
        public final boolean allowed;
        public final String reason;

        private MessagePermission(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        public static MessagePermission allowed() {
            return new MessagePermission(true, null);
        }

        public static MessagePermission denied(String reason) {
            return new MessagePermission(false, reason);
        }
    }
}
