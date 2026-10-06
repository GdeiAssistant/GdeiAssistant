package cn.gdeiassistant.core.social.pojo.dto;

import java.io.Serializable;

public class SocialUserDTO implements Serializable {
    private String id;
    private String nickname;
    private String avatarUrl;
    private String introduction;
    private int followingCount;
    private int followerCount;
    private int friendCount;
    private String relationship;
    private boolean blockedByMe;
    private boolean canMessage;
    private String messagePermissionReason;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }
    public int getFollowerCount() { return followerCount; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }
    public int getFriendCount() { return friendCount; }
    public void setFriendCount(int friendCount) { this.friendCount = friendCount; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public boolean isBlockedByMe() { return blockedByMe; }
    public void setBlockedByMe(boolean blockedByMe) { this.blockedByMe = blockedByMe; }
    public boolean isCanMessage() { return canMessage; }
    public void setCanMessage(boolean canMessage) { this.canMessage = canMessage; }
    public String getMessagePermissionReason() { return messagePermissionReason; }
    public void setMessagePermissionReason(String messagePermissionReason) { this.messagePermissionReason = messagePermissionReason; }
}
