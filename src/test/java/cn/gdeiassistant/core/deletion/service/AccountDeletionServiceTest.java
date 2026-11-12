package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.databaseexception.UserNotExistException;
import cn.gdeiassistant.common.pojo.entity.CetNumber;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import cn.gdeiassistant.core.announcement.service.AnnouncementAccountContributor;
import cn.gdeiassistant.core.cet.mapper.CetMapper;
import cn.gdeiassistant.core.cet.service.CetAccountContributor;
import cn.gdeiassistant.core.dating.mapper.DatingMapper;
import cn.gdeiassistant.core.dating.service.DatingAccountContributor;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.delivery.service.DeliveryAccountContributor;
import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.express.service.ExpressAccountContributor;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.grade.service.GradeAccountContributor;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.lostandfound.service.LostAndFoundAccountContributor;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.service.MarketplaceAccountContributor;
import cn.gdeiassistant.core.message.mapper.InteractionNotificationMapper;
import cn.gdeiassistant.core.message.service.MessageAccountContributor;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.phone.service.PhoneAccountContributor;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import cn.gdeiassistant.core.photograph.service.PhotographAccountContributor;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.service.PrivacyAccountContributor;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.service.ProfileAccountContributor;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.schedule.service.ScheduleAccountContributor;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.service.SecretAccountContributor;
import cn.gdeiassistant.core.social.mapper.SocialRelationMapper;
import cn.gdeiassistant.core.social.service.SocialAccountContributor;
import cn.gdeiassistant.core.social.websocket.SocialRealtimeHub;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.service.TopicAccountContributor;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Behaviour test for account-closure orchestration: asserts observable results only
 * (blockers, anonymized name, per-domain erasure) and no internal field layout.
 */
@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    private static final String SESSION_ID = "session-123";

    @Mock private UserCertificateService userCertificateService;
    @Mock private UserProfileService userProfileService;
    @Mock private GradeDao gradeDao;
    @Mock private ScheduleDao scheduleDao;
    @Mock private UserMapper userMapper;
    @Mock private MarketplaceMapper marketplaceMapper;
    @Mock private LostAndFoundMapper lostAndFoundMapper;
    @Mock private DeliveryMapper deliveryMapper;
    @Mock private CetMapper cetMapper;
    @Mock private PhoneMapper phoneMapper;
    @Mock private ProfileMapper profileMapper;
    @Mock private PrivacyMapper privacyMapper;
    @Mock private DatingMapper datingMapper;
    @Mock private TopicMapper topicMapper;
    @Mock private SecretMapper secretMapper;
    @Mock private PhotographMapper photographMapper;
    @Mock private InteractionNotificationMapper interactionNotificationMapper;
    @Mock private ExpressMapper expressMapper;
    @Mock private SocialRelationMapper socialRelationMapper;
    @Mock private SocialRealtimeHub realtimeHub;
    @Mock private AnnouncementMapper announcementMapper;
    @Mock private DeletionCleanupMapper cleanupMapper;
    @Mock private ApplicationEventPublisher events;

    private User mockUser;

    /**
     * Real contributors wired to mocked mappers: assertions then describe what happened to a
     * domain's data instead of which method the orchestrator happened to call.
     */
    private AccountDeletionService deletionService() {
        List<AccountLifecycleContributor> contributors = List.of(
                new MarketplaceAccountContributor(marketplaceMapper),
                new LostAndFoundAccountContributor(lostAndFoundMapper),
                new DeliveryAccountContributor(deliveryMapper),
                new CetAccountContributor(cetMapper),
                new PhoneAccountContributor(phoneMapper),
                new DatingAccountContributor(datingMapper),
                new ProfileAccountContributor(profileMapper, userProfileService),
                new PrivacyAccountContributor(privacyMapper),
                new TopicAccountContributor(topicMapper),
                new SecretAccountContributor(secretMapper),
                new PhotographAccountContributor(photographMapper),
                new MessageAccountContributor(interactionNotificationMapper),
                new ExpressAccountContributor(expressMapper),
                new SocialAccountContributor(socialRelationMapper, realtimeHub),
                new GradeAccountContributor(gradeDao),
                new ScheduleAccountContributor(scheduleDao),
                new AnnouncementAccountContributor(announcementMapper));
        return new AccountDeletionService(contributors, userCertificateService, userMapper, cleanupMapper, events);
    }

    @BeforeEach
    void setUp() {
        mockUser = new User(USERNAME, PASSWORD);
    }

    private CampusAccountView campusAccount() {
        CampusAccountView account = new CampusAccountView();
        account.setUsername(USERNAME);
        account.setPassword(PASSWORD);
        return account;
    }

    // =========================================================================
    // deleteAccount
    // =========================================================================

    @Test
    @DisplayName("deleteAccount erases every domain and closes the account")
    void deleteAccount_purgesEveryDomainAndClosesAccount() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(cetMapper.selectNumber(USERNAME)).thenReturn(new CetNumber());
        when(phoneMapper.selectPhone(USERNAME)).thenReturn(new PhoneEntity());
        when(userMapper.selectDeletedUserCount(anyString())).thenReturn(0);

        deletionService().deleteAccount(SESSION_ID);

        verify(cetMapper).updateNumber(USERNAME, null);
        verify(phoneMapper).deletePhone(USERNAME);
        verify(datingMapper).hideDatingProfilesByUsername(USERNAME);
        verify(profileMapper).resetUserProfile(USERNAME, "已注销");
        verify(profileMapper).resetUserIntroduction(USERNAME);
        verify(privacyMapper).resetPrivacy(USERNAME);
        verify(topicMapper).anonymizeUsername(eq(USERNAME), anyString());
        verify(secretMapper).anonymizeUsername(eq(USERNAME), anyString());
        verify(photographMapper).anonymizeUsername(eq(USERNAME), anyString());
        verify(interactionNotificationMapper).anonymizeActorUsername(eq(USERNAME), anyString(), eq("已注销用户"));
        verify(expressMapper).anonymizeByUsername(eq(USERNAME), anyString());
        verify(expressMapper).anonymizeCommentsByUsername(eq(USERNAME), anyString());
        verify(userMapper).closeUser(anyString(), eq(USERNAME));
        verify(cleanupMapper).enqueue(anyString(), eq(USERNAME), startsWith("del_"), isNull());
    }

    @Test
    @DisplayName("deleteAccount skips an absent CET number and phone")
    void deleteAccount_skipsAbsentCetNumberAndPhone() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
        when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
        when(userMapper.selectDeletedUserCount(anyString())).thenReturn(0);

        deletionService().deleteAccount(SESSION_ID);

        verify(cetMapper, never()).updateNumber(anyString(), any());
        verify(phoneMapper, never()).deletePhone(anyString());
    }

    @Test
    @DisplayName("deleteAccount builds the anonymized name from a hash prefix and the reuse count")
    void deleteAccount_anonymizedNameFormat() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
        when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
        when(userMapper.selectDeletedUserCount(anyString())).thenReturn(2);

        deletionService().deleteAccount(SESSION_ID);

        verify(userMapper).closeUser(argThat(name -> name.startsWith("del_") && name.endsWith("_2")), eq(USERNAME));
    }

    @Test
    @DisplayName("deleteAccount treats a null reuse count as zero")
    void deleteAccount_nullCountDefaultsToZero() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
        when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
        when(userMapper.selectDeletedUserCount(anyString())).thenReturn(null);

        deletionService().deleteAccount(SESSION_ID);

        verify(userMapper).closeUser(argThat(name -> name.endsWith("_0")), eq(USERNAME));
    }

    @Test
    @DisplayName("deleteAccount shares one anonymized name across domains")
    void deleteAccount_sharesAnonymizedNameAcrossDomains() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
        when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
        when(userMapper.selectDeletedUserCount(anyString())).thenReturn(0);

        deletionService().deleteAccount(SESSION_ID);

        ArgumentCaptor<String> anonymized = ArgumentCaptor.forClass(String.class);
        verify(topicMapper).anonymizeUsername(eq(USERNAME), anonymized.capture());
        String name = anonymized.getValue();
        verify(secretMapper).anonymizeUsername(USERNAME, name);
        verify(photographMapper).anonymizeUsername(USERNAME, name);
        verify(interactionNotificationMapper).anonymizeActorUsername(USERNAME, name, "已注销用户");
        verify(expressMapper).anonymizeByUsername(USERNAME, name);
        verify(expressMapper).anonymizeCommentsByUsername(USERNAME, name);
        verify(userMapper).closeUser(name, USERNAME);
    }

    @Test
    @DisplayName("deleteAccount leaves cross-store cleanup to the worker")
    void deleteAccount_defersCleanupToWorker() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
        when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
        when(userMapper.selectDeletedUserCount(anyString())).thenReturn(0);

        deletionService().deleteAccount(SESSION_ID);

        verifyNoInteractions(gradeDao, scheduleDao, announcementMapper, userProfileService, realtimeHub);
    }

    @Test
    @DisplayName("deleteAccount clears follow and block relations for a known user")
    void deleteAccount_clearsSocialRelationsForKnownUser() throws Exception {
        CampusAccountView account = campusAccount();
        account.setId(42L);
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(userMapper.selectUser(USERNAME)).thenReturn(account);
        when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
        when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
        when(userMapper.selectDeletedUserCount(anyString())).thenReturn(0);

        deletionService().deleteAccount(SESSION_ID);

        verify(userMapper).selectUserByIdForUpdate(42L);
        verify(socialRelationMapper).deleteAllFollowsForUser(42L);
        verify(socialRelationMapper).deleteAllBlocksForUser(42L);
        verify(userMapper).closeAppUser(42L);
    }

    // =========================================================================
    // checkAccountDeletability
    // =========================================================================

    @Test
    @DisplayName("rejects a missing user with UserNotExistException")
    void checkAccountDeletability_userMissing() {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(userMapper.selectUser(USERNAME)).thenReturn(null);

        assertThrows(UserNotExistException.class,
                () -> deletionService().checkAccountDeletability(SESSION_ID, PASSWORD));
    }

    @Test
    @DisplayName("asks for the campus password when it is blank")
    void checkAccountDeletability_blankPassword() {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(userMapper.selectUser(USERNAME)).thenReturn(campusAccount());

        assertThrows(PasswordIncorrectException.class,
                () -> deletionService().checkAccountDeletability(SESSION_ID, " "));
    }

    @Test
    @DisplayName("rejects a wrong campus password")
    void checkAccountDeletability_wrongPassword() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(userMapper.selectUser(USERNAME)).thenReturn(campusAccount());
        doThrow(new PasswordIncorrectException("密码错误"))
                .when(userCertificateService).verifyCurrentPassword(USERNAME, "wrong-password");

        assertThrows(PasswordIncorrectException.class,
                () -> deletionService().checkAccountDeletability(SESSION_ID, "wrong-password"));
    }

    @Test
    @DisplayName("allows closure when no domain reports unfinished content")
    void checkAccountDeletability_noBlockers() throws Exception {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(userMapper.selectUser(USERNAME)).thenReturn(campusAccount());
        when(marketplaceMapper.selectOwnerItemStates(USERNAME)).thenReturn(Collections.emptyList());
        when(lostAndFoundMapper.selectOwnerItemStates(USERNAME)).thenReturn(Collections.emptyList());
        when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME)).thenReturn(Collections.emptyList());

        DataJsonResult<Map<String, String>> result =
                deletionService().checkAccountDeletability(SESSION_ID, PASSWORD);

        assertTrue(result.isSuccess());
        verify(userCertificateService).verifyCurrentPassword(USERNAME, PASSWORD);
    }

    @Test
    @DisplayName("blocks closure while a marketplace item is still on sale")
    void checkAccountDeletability_marketplaceBlocker() throws Exception {
        MarketplaceItemEntity onSale = new MarketplaceItemEntity();
        onSale.setId(10);
        onSale.setState(1);
        MarketplaceItemEntity removed = new MarketplaceItemEntity();
        removed.setId(20);
        removed.setState(3);
        stubAllBlockers(List.of(onSale, removed), Collections.emptyList(), Collections.emptyList());

        DataJsonResult<Map<String, String>> result =
                deletionService().checkAccountDeletability(SESSION_ID, PASSWORD);

        assertFalse(result.isSuccess());
        assertTrue(result.getData().containsKey("二手交易平台存在未交易完成的物品"));
    }

    @Test
    @DisplayName("blocks closure while a lost-and-found item is still open")
    void checkAccountDeletability_lostAndFoundBlocker() throws Exception {
        LostAndFoundItemEntity open = new LostAndFoundItemEntity();
        open.setId(5);
        open.setState(0);
        LostAndFoundItemEntity resolved = new LostAndFoundItemEntity();
        resolved.setId(6);
        resolved.setState(1);
        stubAllBlockers(Collections.emptyList(), List.of(open, resolved), Collections.emptyList());

        DataJsonResult<Map<String, String>> result =
                deletionService().checkAccountDeletability(SESSION_ID, PASSWORD);

        assertFalse(result.isSuccess());
        assertTrue(result.getData().containsKey("失物招领平台存在未确认状态的物品"));
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("blocks closure while a delivery order has no taker")
    void checkAccountDeletability_openDeliveryOrder() throws Exception {
        DeliveryOrderEntity openOrder = new DeliveryOrderEntity();
        openOrder.setOrderId(100);
        openOrder.setState(0);
        stubAllBlockers(Collections.emptyList(), Collections.emptyList(), List.of(openOrder));

        DataJsonResult<Map<String, String>> result =
                deletionService().checkAccountDeletability(SESSION_ID, PASSWORD);

        assertFalse(result.isSuccess());
        assertTrue(result.getData().containsKey("全民快递平台存在未删除的订单信息"));
    }

    @Test
    @DisplayName("blocks closure while an accepted delivery trade is pending")
    void checkAccountDeletability_pendingDeliveryTrade() throws Exception {
        DeliveryOrderEntity accepted = new DeliveryOrderEntity();
        accepted.setOrderId(200);
        accepted.setState(1);
        DeliveryTradeEntity pendingTrade = new DeliveryTradeEntity();
        pendingTrade.setTradeId(300);
        pendingTrade.setState(0);
        when(deliveryMapper.selectDeliveryTradeByOrderId(200)).thenReturn(pendingTrade);
        stubAllBlockers(Collections.emptyList(), Collections.emptyList(), List.of(accepted));

        DataJsonResult<Map<String, String>> result =
                deletionService().checkAccountDeletability(SESSION_ID, PASSWORD);

        assertFalse(result.isSuccess());
        assertTrue(result.getData().containsKey("全民快递平台存在未确认交易的订单信息"));
    }

    @Test
    @DisplayName("does not block when an accepted order has no trade record")
    void checkAccountDeletability_missingTradeDoesNotBlock() throws Exception {
        DeliveryOrderEntity accepted = new DeliveryOrderEntity();
        accepted.setOrderId(200);
        accepted.setState(1);
        when(deliveryMapper.selectDeliveryTradeByOrderId(200)).thenReturn(null);
        stubAllBlockers(Collections.emptyList(), Collections.emptyList(), List.of(accepted));

        DataJsonResult<Map<String, String>> result =
                deletionService().checkAccountDeletability(SESSION_ID, PASSWORD);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("returns every blocking reason")
    void checkAccountDeletability_returnsAllBlockers() throws Exception {
        MarketplaceItemEntity onSale = new MarketplaceItemEntity();
        onSale.setState(1);
        LostAndFoundItemEntity open = new LostAndFoundItemEntity();
        open.setState(0);
        DeliveryOrderEntity openOrder = new DeliveryOrderEntity();
        openOrder.setOrderId(1);
        openOrder.setState(0);
        stubAllBlockers(List.of(onSale), List.of(open), List.of(openOrder));

        DataJsonResult<Map<String, String>> result =
                deletionService().checkAccountDeletability(SESSION_ID, PASSWORD);

        assertFalse(result.isSuccess());
        assertEquals(3, result.getData().size());
        List<String> reasons = new ArrayList<>(result.getData().keySet());
        assertTrue(reasons.contains("二手交易平台存在未交易完成的物品"));
        assertTrue(reasons.contains("失物招领平台存在未确认状态的物品"));
        assertTrue(reasons.contains("全民快递平台存在未删除的订单信息"));
    }

    private void stubAllBlockers(List<MarketplaceItemEntity> marketplace,
                                 List<LostAndFoundItemEntity> lostAndFound,
                                 List<DeliveryOrderEntity> delivery) {
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        when(userMapper.selectUser(USERNAME)).thenReturn(campusAccount());
        when(marketplaceMapper.selectOwnerItemStates(USERNAME)).thenReturn(marketplace);
        when(lostAndFoundMapper.selectOwnerItemStates(USERNAME)).thenReturn(lostAndFound);
        when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME)).thenReturn(delivery);
    }
}
