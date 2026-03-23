package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.common.pojo.Entity.CetNumber;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.cet.mapper.CetMapper;
import cn.gdeiassistant.core.close.mapper.CloseMapper;
import cn.gdeiassistant.core.dating.mapper.DatingMapper;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.message.mapper.InteractionNotificationMapper;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountDeletionServiceTest {

    @InjectMocks
    private AccountDeletionService accountDeletionService;

    @Mock private UserProfileService userProfileService;
    @Mock private UserCertificateService userCertificateService;
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
    @Mock private CloseMapper closeMapper;
    @Mock private DatingMapper datingMapper;
    @Mock private TopicMapper topicMapper;
    @Mock private SecretMapper secretMapper;
    @Mock private PhotographMapper photographMapper;
    @Mock private InteractionNotificationMapper interactionNotificationMapper;
    @Mock private ExpressMapper expressMapper;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";
    private static final String SESSION_ID = "session-123";

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(USERNAME, PASSWORD);
    }

    // =========================================================================
    // deleteAccount — happy path
    // =========================================================================
    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccount {

        @BeforeEach
        void setUpDeleteAccount() {
            when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
            // Default stubs for queries that may or may not return data
            lenient().when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
            lenient().when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
            lenient().when(userMapper.selectDeletedUserCount(anyString())).thenReturn(0);
        }

        @Test
        @DisplayName("happy path: calls all anonymize mappers and closes user")
        void happyPath_callsAllAnonymizeMappersAndClosesUser() throws Exception {
            accountDeletionService.deleteAccount(SESSION_ID);

            // Verify CET and phone cleanup were checked
            verify(cetMapper).selectNumber(USERNAME);
            verify(phoneMapper).selectPhone(USERNAME);

            // Verify education cache removal
            verify(gradeDao).removeGrade(USERNAME);
            verify(scheduleDao).removeSchedule(USERNAME);

            // Verify dating profiles hidden
            verify(datingMapper).hideDatingProfilesByUsername(USERNAME);

            // Verify profile reset
            verify(profileMapper).resetUserProfile(USERNAME, "已注销");
            verify(profileMapper).resetUserIntroduction(USERNAME);

            // Verify privacy reset
            verify(privacyMapper).resetPrivacy(USERNAME);

            // Verify avatar deletion
            verify(userProfileService).deleteAvatar(USERNAME);

            // Verify anonymization of community content
            verify(topicMapper).anonymizeUsername(eq(USERNAME), anyString());
            verify(secretMapper).anonymizeUsername(eq(USERNAME), anyString());
            verify(photographMapper).anonymizeUsername(eq(USERNAME), anyString());
            verify(interactionNotificationMapper).anonymizeActorUsername(eq(USERNAME), anyString(), eq("已注销用户"));
            verify(expressMapper).anonymizeByUsername(eq(USERNAME), anyString());
            verify(expressMapper).anonymizeCommentsByUsername(eq(USERNAME), anyString());

            // Verify user account closed
            verify(userMapper).closeUser(anyString(), eq(USERNAME));

            // Verify close log saved
            verify(closeMapper).insertCloseLog(argThat(log ->
                    log.getUsername().equals(USERNAME)));
        }

        @Test
        @DisplayName("happy path: deletedUsername is derived from sha1 hash with count suffix")
        void happyPath_deletedUsernameFormat() throws Exception {
            when(userMapper.selectDeletedUserCount(anyString())).thenReturn(2);

            accountDeletionService.deleteAccount(SESSION_ID);

            // The deletedUsername should end with _2 (the count)
            verify(userMapper).closeUser(argThat(resetName ->
                    resetName.startsWith("del_") && resetName.endsWith("_2")), eq(USERNAME));
        }

        @Test
        @DisplayName("happy path: null count from selectDeletedUserCount defaults to 0")
        void happyPath_nullCountDefaultsToZero() throws Exception {
            when(userMapper.selectDeletedUserCount(anyString())).thenReturn(null);

            accountDeletionService.deleteAccount(SESSION_ID);

            verify(userMapper).closeUser(argThat(resetName ->
                    resetName.endsWith("_0")), eq(USERNAME));
        }

        @Test
        @DisplayName("deletes CET number when one exists")
        void deletesCetNumberWhenPresent() throws Exception {
            CetNumber cetNumber = new CetNumber();
            when(cetMapper.selectNumber(USERNAME)).thenReturn(cetNumber);

            accountDeletionService.deleteAccount(SESSION_ID);

            verify(cetMapper).updateNumber(USERNAME, null);
        }

        @Test
        @DisplayName("skips CET deletion when no number exists")
        void skipsCetDeletionWhenAbsent() throws Exception {
            when(cetMapper.selectNumber(USERNAME)).thenReturn(null);

            accountDeletionService.deleteAccount(SESSION_ID);

            verify(cetMapper, never()).updateNumber(anyString(), any());
        }

        @Test
        @DisplayName("deletes phone when one exists")
        void deletesPhoneWhenPresent() throws Exception {
            PhoneEntity phone = new PhoneEntity();
            when(phoneMapper.selectPhone(USERNAME)).thenReturn(phone);

            accountDeletionService.deleteAccount(SESSION_ID);

            verify(phoneMapper).deletePhone(USERNAME);
        }

        @Test
        @DisplayName("skips phone deletion when no phone exists")
        void skipsPhoneDeletionWhenAbsent() throws Exception {
            when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);

            accountDeletionService.deleteAccount(SESSION_ID);

            verify(phoneMapper, never()).deletePhone(anyString());
        }

        @Test
        @DisplayName("anonymization uses consistent deletedUsername across all mappers")
        void anonymizationUsesConsistentDeletedUsername() throws Exception {
            accountDeletionService.deleteAccount(SESSION_ID);

            // Capture the deletedUsername from any one mapper call and verify all others use the same
            var topicCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
            verify(topicMapper).anonymizeUsername(eq(USERNAME), topicCaptor.capture());
            String deletedUsername = topicCaptor.getValue();

            verify(secretMapper).anonymizeUsername(USERNAME, deletedUsername);
            verify(photographMapper).anonymizeUsername(USERNAME, deletedUsername);
            verify(interactionNotificationMapper).anonymizeActorUsername(USERNAME, deletedUsername, "已注销用户");
            verify(expressMapper).anonymizeByUsername(USERNAME, deletedUsername);
            verify(expressMapper).anonymizeCommentsByUsername(USERNAME, deletedUsername);
            verify(userMapper).closeUser(deletedUsername, USERNAME);
        }
    }

    // =========================================================================
    // deleteAccount — optional mapper null safety
    // =========================================================================
    @Nested
    @DisplayName("deleteAccount - optional mapper null safety")
    class DeleteAccountOptionalMappers {

        /**
         * When optional mappers (topic, secret, photograph, notification, express,
         * dating) are null, the deletion flow must still succeed. We simulate this
         * by creating a fresh service instance with those fields left null.
         */
        @Test
        @DisplayName("deletion succeeds when all optional mappers are null")
        void deletionSucceedsWithNullOptionalMappers() throws Exception {
            // Build a service with optional mappers explicitly set to null via reflection
            AccountDeletionService service = new AccountDeletionService();
            setField(service, "userProfileService", userProfileService);
            setField(service, "userCertificateService", userCertificateService);
            setField(service, "gradeDao", gradeDao);
            setField(service, "scheduleDao", scheduleDao);
            setField(service, "userMapper", userMapper);
            setField(service, "marketplaceMapper", marketplaceMapper);
            setField(service, "lostAndFoundMapper", lostAndFoundMapper);
            setField(service, "deliveryMapper", deliveryMapper);
            setField(service, "cetMapper", cetMapper);
            setField(service, "phoneMapper", phoneMapper);
            setField(service, "profileMapper", profileMapper);
            setField(service, "privacyMapper", privacyMapper);
            setField(service, "closeMapper", closeMapper);
            // Intentionally NOT setting: datingMapper, topicMapper, secretMapper,
            // photographMapper, interactionNotificationMapper, expressMapper

            when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
            when(cetMapper.selectNumber(USERNAME)).thenReturn(null);
            when(phoneMapper.selectPhone(USERNAME)).thenReturn(null);
            when(userMapper.selectDeletedUserCount(anyString())).thenReturn(0);

            // Should not throw
            assertDoesNotThrow(() -> service.deleteAccount(SESSION_ID));

            // Core non-optional operations should still be called
            verify(profileMapper).resetUserProfile(USERNAME, "已注销");
            verify(userMapper).closeUser(anyString(), eq(USERNAME));
            verify(closeMapper).insertCloseLog(any());
        }

        private void setField(Object target, String fieldName, Object value) throws Exception {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        }
    }

    // =========================================================================
    // CloseSocialDataState
    // =========================================================================
    @Nested
    @DisplayName("CloseSocialDataState")
    class CloseSocialDataStateTests {

        @Test
        @DisplayName("closes active marketplace items (state=1 -> state=3)")
        void closesActiveMarketplaceItems() throws Exception {
            MarketplaceItemEntity activeItem = new MarketplaceItemEntity();
            activeItem.setId(10);
            activeItem.setState(1);

            MarketplaceItemEntity completedItem = new MarketplaceItemEntity();
            completedItem.setId(20);
            completedItem.setState(3);

            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(List.of(activeItem, completedItem));
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(marketplaceMapper).updateItemState(10, 3);
            verify(marketplaceMapper, never()).updateItemState(eq(20), anyInt());
        }

        @Test
        @DisplayName("closes active lost-and-found items (state=0 -> state=2)")
        void closesActiveLostAndFoundItems() throws Exception {
            LostAndFoundItemEntity activeItem = new LostAndFoundItemEntity();
            activeItem.setId(5);
            activeItem.setState(0);

            LostAndFoundItemEntity resolvedItem = new LostAndFoundItemEntity();
            resolvedItem.setId(6);
            resolvedItem.setState(1);

            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(List.of(activeItem, resolvedItem));
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(lostAndFoundMapper).updateItemState(5, 2);
            verify(lostAndFoundMapper, never()).updateItemState(eq(6), anyInt());
        }

        @Test
        @DisplayName("closes delivery orders: state=0 -> state=3")
        void closesOpenDeliveryOrders() throws Exception {
            DeliveryOrderEntity openOrder = new DeliveryOrderEntity();
            openOrder.setOrderId(100);
            openOrder.setState(0);

            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(List.of(openOrder));

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(deliveryMapper).updateOrderState(100, 3);
        }

        @Test
        @DisplayName("closes delivery trades: state=1 order with state=0 trade -> trade state=2")
        void closesDeliveryTradesForAcceptedOrders() throws Exception {
            DeliveryOrderEntity acceptedOrder = new DeliveryOrderEntity();
            acceptedOrder.setOrderId(200);
            acceptedOrder.setState(1);

            DeliveryTradeEntity pendingTrade = new DeliveryTradeEntity();
            pendingTrade.setTradeId(300);
            pendingTrade.setState(0);

            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(List.of(acceptedOrder));
            when(deliveryMapper.selectDeliveryTradeByOrderId(200))
                    .thenReturn(pendingTrade);

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(deliveryMapper).updateTradeState(300, 0, 2);
        }

        @Test
        @DisplayName("skips trade update when trade is null for accepted order")
        void skipsTradeUpdateWhenTradeIsNull() throws Exception {
            DeliveryOrderEntity acceptedOrder = new DeliveryOrderEntity();
            acceptedOrder.setOrderId(200);
            acceptedOrder.setState(1);

            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(List.of(acceptedOrder));
            when(deliveryMapper.selectDeliveryTradeByOrderId(200))
                    .thenReturn(null);

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(deliveryMapper, never()).updateTradeState(anyInt(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("skips trade update when trade state is not 0")
        void skipsTradeUpdateWhenTradeAlreadyCompleted() throws Exception {
            DeliveryOrderEntity acceptedOrder = new DeliveryOrderEntity();
            acceptedOrder.setOrderId(200);
            acceptedOrder.setState(1);

            DeliveryTradeEntity completedTrade = new DeliveryTradeEntity();
            completedTrade.setTradeId(300);
            completedTrade.setState(1); // already completed

            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(List.of(acceptedOrder));
            when(deliveryMapper.selectDeliveryTradeByOrderId(200))
                    .thenReturn(completedTrade);

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(deliveryMapper, never()).updateTradeState(anyInt(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("hides dating profiles via datingMapper")
        void hidesDatingProfiles() throws Exception {
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(datingMapper).hideDatingProfilesByUsername(USERNAME);
        }

        @Test
        @DisplayName("handles empty lists gracefully with no mapper updates")
        void handlesEmptyListsGracefully() throws Exception {
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());

            accountDeletionService.CloseSocialDataState(USERNAME);

            verify(marketplaceMapper, never()).updateItemState(anyInt(), anyInt());
            verify(lostAndFoundMapper, never()).updateItemState(anyInt(), anyInt());
            verify(deliveryMapper, never()).updateOrderState(anyInt(), anyInt());
            verify(deliveryMapper, never()).updateTradeState(anyInt(), anyInt(), anyInt());
        }
    }

    // =========================================================================
    // checkAccountDeletability
    // =========================================================================
    @Nested
    @DisplayName("checkAccountDeletability")
    class CheckAccountDeletability {

        @BeforeEach
        void setUp() {
            when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(mockUser);
        }

        @Test
        @DisplayName("throws UserNotExistException when user not found")
        void throwsUserNotExistExceptionWhenUserNotFound() {
            when(userMapper.selectUser(USERNAME)).thenReturn(null);

            assertThrows(UserNotExistException.class, () ->
                    accountDeletionService.checkAccountDeletability(SESSION_ID, PASSWORD));
        }

        @Test
        @DisplayName("throws PasswordIncorrectException when password does not match")
        void throwsPasswordIncorrectExceptionOnWrongPassword() {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(USERNAME);
            userEntity.setPassword("correct-password");

            when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);

            assertThrows(PasswordIncorrectException.class, () ->
                    accountDeletionService.checkAccountDeletability(SESSION_ID, "wrong-password"));
        }

        @Test
        @DisplayName("returns success when no blocking conditions exist")
        void returnsSuccessWhenNoPendingItems() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(USERNAME);
            userEntity.setPassword(PASSWORD);

            when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());

            DataJsonResult<Map<String, String>> result =
                    accountDeletionService.checkAccountDeletability(SESSION_ID, PASSWORD);

            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("returns failure with marketplace blocking condition")
        void returnsFailureWithActiveMarketplaceItem() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(USERNAME);
            userEntity.setPassword(PASSWORD);

            MarketplaceItemEntity activeItem = new MarketplaceItemEntity();
            activeItem.setState(1);

            when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(List.of(activeItem));
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());

            DataJsonResult<Map<String, String>> result =
                    accountDeletionService.checkAccountDeletability(SESSION_ID, PASSWORD);

            assertFalse(result.isSuccess());
            assertNotNull(result.getData());
            assertTrue(result.getData().containsKey("二手交易平台存在未交易完成的物品"));
        }

        @Test
        @DisplayName("returns failure with lost-and-found blocking condition")
        void returnsFailureWithActiveLostAndFoundItem() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(USERNAME);
            userEntity.setPassword(PASSWORD);

            LostAndFoundItemEntity activeItem = new LostAndFoundItemEntity();
            activeItem.setState(0);

            when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(List.of(activeItem));
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());

            DataJsonResult<Map<String, String>> result =
                    accountDeletionService.checkAccountDeletability(SESSION_ID, PASSWORD);

            assertFalse(result.isSuccess());
            assertNotNull(result.getData());
            assertTrue(result.getData().containsKey("失物招领平台存在未确认状态的物品"));
        }

        @Test
        @DisplayName("returns failure with open delivery order blocking condition")
        void returnsFailureWithOpenDeliveryOrder() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(USERNAME);
            userEntity.setPassword(PASSWORD);

            DeliveryOrderEntity openOrder = new DeliveryOrderEntity();
            openOrder.setOrderId(1);
            openOrder.setState(0);

            when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(List.of(openOrder));

            DataJsonResult<Map<String, String>> result =
                    accountDeletionService.checkAccountDeletability(SESSION_ID, PASSWORD);

            assertFalse(result.isSuccess());
            assertNotNull(result.getData());
            assertTrue(result.getData().containsKey("全民快递平台存在未删除的订单信息"));
        }

        @Test
        @DisplayName("returns failure with pending delivery trade blocking condition")
        void returnsFailureWithPendingDeliveryTrade() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(USERNAME);
            userEntity.setPassword(PASSWORD);

            DeliveryOrderEntity acceptedOrder = new DeliveryOrderEntity();
            acceptedOrder.setOrderId(1);
            acceptedOrder.setState(1);

            DeliveryTradeEntity pendingTrade = new DeliveryTradeEntity();
            pendingTrade.setTradeId(2);
            pendingTrade.setState(0);

            when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(Collections.emptyList());
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(List.of(acceptedOrder));
            when(deliveryMapper.selectDeliveryTradeByOrderId(1))
                    .thenReturn(pendingTrade);

            DataJsonResult<Map<String, String>> result =
                    accountDeletionService.checkAccountDeletability(SESSION_ID, PASSWORD);

            assertFalse(result.isSuccess());
            assertNotNull(result.getData());
            assertTrue(result.getData().containsKey("全民快递平台存在未确认交易的订单信息"));
        }

        @Test
        @DisplayName("returns all blocking conditions when multiple issues exist")
        void returnsAllBlockingConditions() throws Exception {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(USERNAME);
            userEntity.setPassword(PASSWORD);

            MarketplaceItemEntity marketplaceItem = new MarketplaceItemEntity();
            marketplaceItem.setState(1);

            LostAndFoundItemEntity lostItem = new LostAndFoundItemEntity();
            lostItem.setState(0);

            DeliveryOrderEntity openOrder = new DeliveryOrderEntity();
            openOrder.setOrderId(1);
            openOrder.setState(0);

            when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
            when(marketplaceMapper.selectItemsByUsername(USERNAME))
                    .thenReturn(List.of(marketplaceItem));
            when(lostAndFoundMapper.selectItemByUsername(USERNAME))
                    .thenReturn(List.of(lostItem));
            when(deliveryMapper.selectDeliveryOrderByUsername(USERNAME))
                    .thenReturn(List.of(openOrder));

            DataJsonResult<Map<String, String>> result =
                    accountDeletionService.checkAccountDeletability(SESSION_ID, PASSWORD);

            assertFalse(result.isSuccess());
            assertEquals(3, result.getData().size());
        }
    }
}
