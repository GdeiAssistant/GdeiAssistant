package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.social.mapper.SocialRelationMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SocialPermissionMatrixTest {

    @Mock
    private SocialRelationMapper socialRelationMapper;
    @Mock
    private PrivacyMapper privacyMapper;
    @Mock
    private cn.gdeiassistant.core.user.mapper.UserMapper userMapper;
    @Mock
    private cn.gdeiassistant.core.profile.mapper.ProfileMapper profileMapper;
    @Mock
    private cn.gdeiassistant.core.userLogin.service.UserCertificateService userCertificateService;

    @InjectMocks
    private SocialIdentityService identityService;

    private UserEntity sender;
    private UserEntity receiver;

    @BeforeEach
    void setUp() {
        sender = activeUser(1L, "11111111-1111-4111-8111-111111111111", "alice");
        receiver = activeUser(2L, "22222222-2222-4222-8222-222222222222", "bob");
    }

    @Test
    void followingPolicyChecksReceiverFollowsSenderNotReverse() {
        PrivacyEntity privacy = new PrivacyEntity();
        privacy.setDmPolicy("FOLLOWING");
        when(privacyMapper.selectPrivacy("bob")).thenReturn(privacy);
        when(socialRelationMapper.countAnyBlock(1L, 2L)).thenReturn(0);
        when(socialRelationMapper.countFollow(2L, 1L)).thenReturn(1);
        when(socialRelationMapper.countFollow(1L, 2L)).thenReturn(0);

        assertTrue(identityService.evaluateMessagePermission(sender, receiver).allowed);

        when(socialRelationMapper.countFollow(2L, 1L)).thenReturn(0);
        assertFalse(identityService.evaluateMessagePermission(sender, receiver).allowed);
        assertEquals("PRIVACY_RESTRICTED", identityService.evaluateMessagePermission(sender, receiver).reason);
    }

    @Test
    void mutualRequiresBothDirectionsAndUnknownDefaultsToMutual() {
        PrivacyEntity privacy = new PrivacyEntity();
        privacy.setDmPolicy("WEIRD");
        when(privacyMapper.selectPrivacy("bob")).thenReturn(privacy);
        when(socialRelationMapper.countAnyBlock(anyLong(), anyLong())).thenReturn(0);
        when(socialRelationMapper.countFollow(2L, 1L)).thenReturn(1);
        when(socialRelationMapper.countFollow(1L, 2L)).thenReturn(0);
        assertFalse(identityService.evaluateMessagePermission(sender, receiver).allowed);

        when(socialRelationMapper.countFollow(1L, 2L)).thenReturn(1);
        assertTrue(identityService.evaluateMessagePermission(sender, receiver).allowed);
    }

    @Test
    void blockOverridesPrivacyAndNoneStillAllowsOutgoingFromReceiver() {
        when(socialRelationMapper.countAnyBlock(1L, 2L)).thenReturn(1);
        SocialIdentityService.MessagePermission blocked = identityService.evaluateMessagePermission(sender, receiver);
        assertFalse(blocked.allowed);
        assertEquals("CONTACT_UNAVAILABLE", blocked.reason);

        PrivacyEntity none = new PrivacyEntity();
        none.setDmPolicy("NONE");
        when(privacyMapper.selectPrivacy("alice")).thenReturn(none);
        when(socialRelationMapper.countAnyBlock(2L, 1L)).thenReturn(0);
        // bob -> alice: alice NONE blocks incoming to alice
        assertFalse(identityService.evaluateMessagePermission(receiver, sender).allowed);
    }

    @Test
    void missingPolicyNeverBecomesAll() {
        when(privacyMapper.selectPrivacy("bob")).thenReturn(null);
        when(socialRelationMapper.countAnyBlock(1L, 2L)).thenReturn(0);
        when(socialRelationMapper.countFollow(2L, 1L)).thenReturn(0);
        when(socialRelationMapper.countFollow(1L, 2L)).thenReturn(0);
        assertEquals("MUTUAL", identityService.normalizeDmPolicy(null));
        assertFalse(identityService.evaluateMessagePermission(sender, receiver).allowed);
    }

    private static UserEntity activeUser(long id, String publicId, String username) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setPublicId(publicId);
        entity.setStatus("ACTIVE");
        entity.setUsername(username);
        return entity;
    }
}
