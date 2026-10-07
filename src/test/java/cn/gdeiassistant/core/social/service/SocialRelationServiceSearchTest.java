package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.mapper.SocialRelationMapper;
import cn.gdeiassistant.core.social.pojo.dto.PageDTO;
import cn.gdeiassistant.core.social.pojo.dto.SocialUserDTO;
import cn.gdeiassistant.core.social.websocket.SocialRealtimeHub;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialRelationServiceSearchTest {

    @Mock
    private SocialIdentityService identityService;
    @Mock
    private SocialRelationMapper relationMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PrivacyMapper privacyMapper;
    @Mock
    private SocialRealtimeHub realtimeHub;

    @InjectMocks
    private SocialRelationService relationService;

    private CampusAccountView me;

    @BeforeEach
    void setUp() {
        me = new CampusAccountView();
        me.setId(1L);
        me.setPublicId("11111111-1111-4111-8111-111111111111");
        me.setStatus("ACTIVE");
        me.setUsername("alice");
        when(identityService.requireActiveViewer(anyString())).thenReturn(me);
    }

    @Test
    void searchPassesViewerIdForSqlBlockFilterAndStableCursor() {
        CampusAccountView u2 = new CampusAccountView();
        u2.setId(2L);
        u2.setPublicId("22222222-2222-4222-8222-222222222222");
        u2.setStatus("ACTIVE");
        u2.setCreatedAt(new Date(1000));
        CampusAccountView u3 = new CampusAccountView();
        u3.setId(3L);
        u3.setPublicId("33333333-3333-4333-8333-333333333333");
        u3.setStatus("ACTIVE");
        u3.setCreatedAt(new Date(900));
        when(userMapper.searchActiveUsers(eq("n"), eq(1L), isNull(), isNull(), eq(2)))
                .thenReturn(List.of(u2, u3));
        when(identityService.buildSocialUser(eq(me), any())).thenAnswer(inv -> {
            CampusAccountView t = inv.getArgument(1);
            SocialUserDTO dto = new SocialUserDTO();
            dto.setId(t.getPublicId());
            return dto;
        });

        PageDTO<SocialUserDTO> page = relationService.searchUsers("sid", "n", null, 1);
        assertEquals(1, page.getItems().size());
        assertTrue(page.isHasMore());
        assertNotNull(page.getNextCursor());
        verify(userMapper).searchActiveUsers(eq("n"), eq(1L), isNull(), isNull(), eq(2));
    }

    @Test
    void relationshipOwnerBlockedReturnsNotFound() {
        CampusAccountView owner = new CampusAccountView();
        owner.setId(9L);
        owner.setPublicId("99999999-9999-4999-8999-999999999999");
        owner.setStatus("ACTIVE");
        when(identityService.requireActiveByPublicId(owner.getPublicId())).thenReturn(owner);
        when(relationMapper.countAnyBlock(1L, 9L)).thenReturn(1);

        SocialException ex = assertThrows(SocialException.class,
                () -> relationService.listRelationships("sid", owner.getPublicId(), "following", null, 20));
        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
    }

    private CampusAccountView peer() {
        var target = new CampusAccountView(); target.setId(2L); target.setPublicId("peer"); target.setStatus("ACTIVE");
        when(identityService.requireActiveByPublicId("peer")).thenReturn(target);
        lenient().when(identityService.requireActiveById(1L)).thenReturn(me);
        lenient().when(identityService.requireActiveById(2L)).thenReturn(target);
        return target;
    }
    @Test void followChecksBlocksAfterLockingAndNotifiesOnlyOnAcceptedChange() {
        var target = peer(); var dto = new SocialUserDTO(); when(identityService.buildSocialUser(me, target)).thenReturn(dto);
        assertSame(dto, relationService.follow("sid", "peer"));
        var order = inOrder(identityService, relationMapper, realtimeHub);
        order.verify(identityService).requireActiveViewer("sid"); order.verify(identityService).requireActiveByPublicId("peer");
        order.verify(identityService).lockUsersInOrder(1L, 2L); order.verify(identityService).requireActiveById(1L);
        order.verify(identityService).requireActiveById(2L); order.verify(relationMapper).countAnyBlock(1L, 2L);
        order.verify(relationMapper).insertFollow(1L, 2L);
        order.verify(realtimeHub).pushToUserAfterCommit(eq(1L), anyMap()); order.verify(realtimeHub).pushToUserAfterCommit(eq(2L), anyMap());
        when(relationMapper.countAnyBlock(1L, 2L)).thenReturn(1);
        assertThrows(SocialException.class, () -> relationService.follow("sid", "peer"));
        verify(relationMapper, times(1)).insertFollow(1L, 2L);
    }
    @Test void ownIdentityCannotBeFollowedOrBlocked() {
        when(identityService.requireActiveByPublicId("self")).thenReturn(me);
        assertThrows(SocialException.class, () -> relationService.follow("sid", "self"));
        assertThrows(SocialException.class, () -> relationService.block("sid", "self"));
        verifyNoInteractions(relationMapper, realtimeHub);
    }
    @Test void blockClearsMutualFollowsAndUnblockDoesNotRestoreThem() {
        peer(); assertEquals(true, relationService.block("sid", "peer").get("blocked"));
        verify(relationMapper).insertBlock(1L, 2L); verify(relationMapper).deleteMutualFollows(1L, 2L);
        assertEquals(false, relationService.unblock("sid", "peer").get("blocked"));
        verify(relationMapper).deleteBlock(1L, 2L); verify(relationMapper, never()).insertFollow(anyLong(), anyLong());
        verify(realtimeHub, times(2)).pushToUserAfterCommit(eq(1L), anyMap());
    }
    @Test void unfollowRechecksTargetAfterLockAndRejectsClosedIdentity() {
        var target = peer(); when(identityService.findById(2L)).thenReturn(target);
        relationService.unfollow("sid", "peer"); verify(relationMapper).deleteFollow(1L, 2L);
        target.setStatus("CLOSED"); assertThrows(SocialException.class, () -> relationService.unfollow("sid", "peer"));
        when(identityService.findById(2L)).thenReturn(null); assertThrows(SocialException.class, () -> relationService.unfollow("sid", "peer"));
        verify(relationMapper, times(1)).deleteFollow(1L, 2L);
    }
    @Test void privacyAcceptsOnlyNamedPoliciesForAuthenticatedOwner() {
        when(identityService.requireActiveById(1L)).thenReturn(me);
        when(identityService.normalizeDmPolicy(null)).thenReturn("ALL");
        assertEquals("ALL", relationService.getPrivacy("sid").get("dmPolicy"));
        assertEquals("MUTUAL", relationService.updatePrivacy("sid", " mutual ").get("dmPolicy"));
        verify(privacyMapper).updateDmPolicy("MUTUAL", "alice");
        assertThrows(SocialException.class, () -> relationService.updatePrivacy("sid", null));
        assertThrows(SocialException.class, () -> relationService.updatePrivacy("sid", "public"));
        verify(realtimeHub, times(1)).pushToUserAfterCommit(eq(1L), anyMap());
    }
    @Test void relationshipPaginationKeepsCursorEvenWhenClosedRowsAreFiltered() {
        var target = peer(); when(identityService.findById(2L)).thenReturn(target);
        var closed = new CampusAccountView(); closed.setId(3L); closed.setStatus("CLOSED");
        when(identityService.findById(3L)).thenReturn(closed);
        var rows = List.of(java.util.Map.<String,Object>of("userId", 2L, "relationAt", new Date(1000)),
                java.util.Map.<String,Object>of("userId", 3L, "relationAt", new Date(900)),
                java.util.Map.<String,Object>of("userId", 4L, "relationAt", new Date(800)));
        when(relationMapper.listFollowing(2L, 1L, null, null, 3)).thenReturn(rows);
        var page = relationService.listRelationships("sid", "peer", " following ", null, 2);
        assertEquals(1, page.getItems().size()); assertTrue(page.isHasMore());
        assertEquals("OTAwOjM", page.getNextCursor());
        when(relationMapper.listFollowing(2L, 1L, new Date(900), 3L, 3)).thenReturn(List.of());
        assertFalse(relationService.listRelationships("sid", "peer", "following", page.getNextCursor(), 2).isHasMore());
        when(relationMapper.listFollowers(2L, 1L, null, null, 21)).thenReturn(List.of());
        when(relationMapper.listFriends(2L, 1L, null, null, 51)).thenReturn(List.of());
        assertTrue(relationService.listRelationships("sid", "peer", "followers", null, 0).getItems().isEmpty());
        assertTrue(relationService.listRelationships("sid", "peer", "friends", null, 99).getItems().isEmpty());
        when(relationMapper.listBlocks(1L, null, null, 21)).thenReturn(rows);
        relationService.listBlocks("sid", null, null); verify(identityService).buildClosedPeer(closed);
        assertThrows(SocialException.class, () -> relationService.listRelationships("sid", "peer", "unknown", null, 2));
        assertThrows(SocialException.class, () -> relationService.listBlocks("sid", "invalid!", 2));
    }
    @Test void blockedProfileIsHiddenButSelfProfileRemainsAvailable() {
        peer(); when(relationMapper.countAnyBlock(1L, 2L)).thenReturn(1);
        assertEquals("USER_NOT_FOUND", assertThrows(SocialException.class, () -> relationService.getUser("sid", "peer")).getErrorCode());
        when(identityService.requireActiveByPublicId("self")).thenReturn(me);
        var dto = new SocialUserDTO(); when(identityService.buildSocialUser(me, me)).thenReturn(dto);
        assertSame(dto, relationService.getUser("sid", "self")); assertSame(dto, relationService.getMe("sid"));
    }
}
