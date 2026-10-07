package cn.gdeiassistant.core.user.service;

import cn.gdeiassistant.common.tools.utils.AnonymizeUtils;

import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 公开响应中的作者身份：UUID authorId + 展示名，不泄漏校园 username。
 */
@Component
public class PublicAuthorResolver {

    @Autowired(required = false)
    private UserMapper userMapper;
    @Autowired(required = false)
    private ProfileMapper profileMapper;

    @Autowired(required = false)
    private cn.gdeiassistant.core.user.mapper.PublicAuthorMapper publicAuthorMapper;

    public java.util.Map<String, AuthorPublic> resolveAll(java.util.Collection<String> usernames) {
        var result = new java.util.HashMap<String, AuthorPublic>();
        var active = usernames.stream().filter(java.util.Objects::nonNull).filter(name -> !name.isBlank() && !name.startsWith("del_")).distinct().toList();
        if (!active.isEmpty() && publicAuthorMapper != null) {
            for (var author : publicAuthorMapper.selectAuthors(active)) result.put(author.username(), new AuthorPublic(author.authorId(), author.displayName()));
        }
        for (String username : usernames) result.putIfAbsent(username, new AuthorPublic(null, username != null && username.startsWith("del_") ? AnonymizeUtils.sanitizeUsername(username) : "用户"));
        return result;
    }

    public record AuthorPublic(String authorId, String displayName) {}

    public AuthorPublic resolve(String campusUsername) {
        if (campusUsername == null || campusUsername.isBlank() || campusUsername.startsWith("del_")) {
            return new AuthorPublic(null, AnonymizeUtils.sanitizeUsername(campusUsername));
        }
        String authorId = null;
        if (userMapper != null) {
            CampusAccountView author = userMapper.selectUser(campusUsername);
            if (author != null && author.isActive() && author.getPublicId() != null) {
                authorId = author.getPublicId();
            }
        }
        String display = "用户";
        if (profileMapper != null) {
            ProfileEntity profile = profileMapper.selectUserProfile(campusUsername);
            if (profile != null && profile.getNickname() != null && !profile.getNickname().isBlank()) {
                display = profile.getNickname();
            }
        }
        return new AuthorPublic(authorId, display);
    }
}
