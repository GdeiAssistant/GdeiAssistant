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
}
