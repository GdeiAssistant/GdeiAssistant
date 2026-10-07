package cn.gdeiassistant.core.dating.service;

import cn.gdeiassistant.common.exception.databaseexception.NoAccessException;
import cn.gdeiassistant.common.exception.datingexception.RepeatPickException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.dating.mapper.DatingMapper;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPickSubmitDTO;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPublishDTO;
import cn.gdeiassistant.core.dating.pojo.entity.DatingPickEntity;
import cn.gdeiassistant.core.dating.pojo.entity.DatingProfileEntity;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.objectstorage.service.StoredAssetService;
import cn.gdeiassistant.core.objectstorage.service.UploadService;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatingPrivacyFlowTest {
    private final DatingMapper mapper = mock(DatingMapper.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final PublicAuthorResolver authors = mock(PublicAuthorResolver.class);
    private final StoredAssetService assets = mock(StoredAssetService.class);
    private final UploadService uploads = mock(UploadService.class);
    private final InteractionNotificationService notifications = mock(InteractionNotificationService.class);
    private final DatingService service = new DatingService();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "datingMapper", mapper);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        ReflectionTestUtils.setField(service, "publicAuthorResolver", authors);
        ReflectionTestUtils.setField(service, "storedAssets", assets);
        ReflectionTestUtils.setField(service, "uploads", uploads);
        ReflectionTestUtils.setField(service, "interactionNotificationService", notifications);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("viewer"));
        when(authors.resolve("author")).thenReturn(new PublicAuthorResolver.AuthorPublic("public-id", "公开昵称"));
        when(assets.generatePresignedUrl("dating/7.jpg", 30, TimeUnit.MINUTES)).thenReturn("signed:picture");
    }

    private DatingProfileEntity profile() {
        var profile = new DatingProfileEntity();
        profile.setProfileId(7); profile.setUsername("author"); profile.setNickname("室友昵称");
        profile.setGrade(2); profile.setFaculty("合成院系"); profile.setHometown("合成家乡");
        profile.setContent("合成简介"); profile.setQq("synthetic-qq"); profile.setWechat("synthetic-wechat");
        profile.setArea(1); profile.setState(1);
        return profile;
    }

    private DatingPickEntity pick(int state) {
        var pick = new DatingPickEntity();
        pick.setPickId(9); pick.setUsername("viewer"); pick.setRoommateProfile(profile());
        pick.setContent("合成请求"); pick.setState(state);
        return pick;
    }

    @Test void pendingAndRejectedSentRequestsNeverExposeContactDetails() {
        var pending = pick(0);
        when(mapper.selectDatingPickListByUsername("viewer", 50)).thenReturn(List.of(pending, pick(-1), pick(1)));
        var views = service.queryMySentPicks("session");
        for (int index = 0; index < 2; index++) {
            var target = views.get(index).getRoommateProfile();
            assertNull(target.getQq()); assertNull(target.getWechat());
            assertEquals("public-id", target.getAuthorId()); assertEquals("公开昵称", target.getUsername());
            assertEquals("signed:picture", target.getPictureURL());
        }
        assertEquals("synthetic-qq", views.get(2).getRoommateProfile().getQq());
        assertEquals("synthetic-wechat", views.get(2).getRoommateProfile().getWechat());
        assertEquals("合成请求", views.get(2).getContent());
        // Mapping must not erase the stored contacts while preparing a restricted response.
        assertEquals("synthetic-qq", pending.getRoommateProfile().getQq());
    }

    @Test void publicListsMaskContactsAndOwnerListsExcludeHiddenProfiles() throws Exception {
        when(mapper.selectDatingProfilePage(0, 10, 1)).thenReturn(List.of(profile()));
        var list = service.queryDatingProfile(0, 10, 1);
        assertEquals("请进入详情页查看", list.get(0).getQq());
        assertEquals("请进入详情页查看", list.get(0).getWechat());
        assertEquals("公开昵称", list.get(0).getUsername());
        var hidden = profile(); hidden.setState(0);
        when(mapper.selectDatingProfileByUsername("viewer")).thenReturn(List.of(profile(), hidden));
        assertEquals(1, service.queryMyRoommateProfiles("session").size());
        when(mapper.selectDatingProfileById(7)).thenReturn(profile());
        var detail = service.queryDatingProfile(7);
        assertEquals("室友昵称", detail.getNickname()); assertEquals(2, detail.getGrade());
        assertEquals("合成院系", detail.getFaculty()); assertEquals("合成家乡", detail.getHometown());
        assertEquals("合成简介", detail.getContent()); assertEquals(1, detail.getArea());
        assertTrue(service.queryDatingProfile(20, 10, 1).isEmpty());
    }

    @Test void receivedRequestDetailsRequireTheProfileOwnerAndCannotBeHandledTwice() throws Exception {
        var pick = pick(0);
        when(mapper.selectDatingPickById(9)).thenReturn(pick);
        assertThrows(NoAccessException.class, () -> service.verifyRoommatePickViewAccess("session", 9));
        assertTrue(service.checkIsPickPageHidden("session", 9));
        when(certificates.getUserLoginCertificate("owner-session")).thenReturn(new User("author"));
        assertDoesNotThrow(() -> service.verifyRoommatePickViewAccess("owner-session", 9));
        assertFalse(service.checkIsPickPageHidden("owner-session", 9));
        service.updateRoommatePickState(9, 1);
        verify(notifications).createInteractionNotification("dating", "pick_accepted", "viewer", "author", "9", "7", "sent", "撩一下已通过", "室友昵称 通过了你的请求");
        pick.setState(1);
        assertThrows(NoAccessException.class, () -> service.updateRoommatePickState(9, -1));
        verify(mapper, times(1)).updateRoommatePickState(anyInt(), anyInt());
        assertTrue(service.checkIsPickPageHidden("owner-session", 9));
    }

    @Test void rejectedRequestsCanBeResubmittedAndNotificationsKeepTheGeneratedIdentity() throws Exception {
        when(mapper.selectDatingProfileById(7)).thenReturn(profile());
        when(mapper.selectDatingPick(7, "viewer")).thenReturn(pick(0));
        assertThrows(RepeatPickException.class, () -> service.verifyRoommatePickRequestAccess("session", 7));
        when(mapper.selectDatingPick(7, "viewer")).thenReturn(pick(-1));
        assertDoesNotThrow(() -> service.verifyRoommatePickRequestAccess("session", 7));
        var dto = new DatingPickSubmitDTO(); dto.setProfileId(7); dto.setContent("合成请求");
        doAnswer(a -> { ((DatingPickEntity) a.getArgument(0)).setPickId(11); return null; }).when(mapper).insertRoommatePick(any());
        service.addRoommatePick("session", dto);
        verify(notifications).createInteractionNotification("dating", "pick_received", "author", "viewer", "11", "7", "received", "收到新的撩一下", "viewer 向你发起了撩一下：合成请求");
        assertEquals(-1, service.queryRoommatePick(7, "session").getState());
        assertNull(service.queryRoommatePick(99, "session"));
    }

    @Test void rejectionUsesSafeFallbackAndReceivedListsUseFinitePictureUrls() throws Exception {
        var pick = pick(0); pick.getRoommateProfile().setNickname("");
        when(mapper.selectDatingPickById(9)).thenReturn(pick);
        service.updateRoommatePickState(9, -1);
        verify(notifications).createInteractionNotification("dating", "pick_rejected", "viewer", "author", "9", "7", "sent", "撩一下未通过", "对方 拒绝了你的请求");
        when(mapper.selectReceivedRoommatePickListByProfileOwner("viewer", 50)).thenReturn(List.of(pick));
        assertEquals("signed:picture", service.queryMyReceivedPicks("session").get(0).getRoommateProfile().getPictureURL());
        assertEquals(9, service.getRoommatePickDetailVo(9).getPickId());
        assertNull(service.getRoommatePickDetailVo(99));
    }

    @Test void publicationUsesOnlyTheAuthenticatedOwnersUploadTicket() throws Exception {
        var dto = new DatingPublishDTO(); dto.setNickname("昵称"); dto.setQq("synthetic-qq"); dto.setWechat("synthetic-wechat");
        doAnswer(a -> { ((DatingProfileEntity) a.getArgument(0)).setProfileId(7); return null; }).when(mapper).insertRoommateProfile(any());
        service.publishProfile("session", dto, null, "ticket");
        verify(uploads).moveUpload("session", "ticket", "dating/7.jpg");
        service.publishProfile("session", dto, new MockMultipartFile("picture", "image.jpg", "image/jpeg", new byte[]{1, 2}), null);
        verify(assets).uploadObject(eq("dating/7.jpg"), any());
        var profile = profile(); when(mapper.selectDatingProfileById(7)).thenReturn(profile);
        service.updateRoommateProfile(dto, 7);
        assertEquals("昵称", profile.getNickname()); verify(mapper).updateRoommateProfile(profile);
        service.updateRoommateProfile(dto, 99);
    }
}
