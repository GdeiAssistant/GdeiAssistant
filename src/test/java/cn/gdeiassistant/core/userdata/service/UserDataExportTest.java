package cn.gdeiassistant.core.userdata.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.common.redis.exportdata.ExportDataDao;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.cetquery.pojo.entity.CetNumberEntity;
import cn.gdeiassistant.core.data.mapper.AppDataMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.*;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.logdata.mapper.LogDataMapper;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.secret.pojo.entity.*;
import cn.gdeiassistant.core.secret.service.SecretService;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;

import com.alibaba.fastjson2.JSON;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.*;

@ExtendWith(MockitoExtension.class)
class UserDataExportTest {
    @Mock UserCertificateService certificates;
    @Mock UserMapper users;
    @Mock PhoneMapper phones;
    @Mock ProfileMapper profiles;
    @Mock PrivacyMapper privacy;
    @Mock AppDataMapper app;
    @Mock LogDataMapper logs;
    @Mock ExportDataDao exports;
    @Mock R2StorageService storage;
    @Mock SecretService secrets;
    @InjectMocks UserDataService service;

    User user() {
        User u = new User("owner");
        u.setPassword("synthetic-private-password");
        return u;
    }

    @BeforeEach
    void setup() {
        lenient().when(certificates.getUserLoginCertificate("session")).thenReturn(user());
    }

    @Test
    void richExportContainsOwnedMediaAndLabelsButNoCredentialOrCommenterIdentity()
            throws Exception {
        PhoneEntity phone = new PhoneEntity();
        phone.setPhone("13800000000");
        when(phones.selectPhone("owner")).thenReturn(phone);
        ProfileEntity profile = new ProfileEntity();
        profile.setUsername("owner");
        profile.setNickname("Synthetic");
        profile.setDegree(1);
        profile.setProfession(1);
        profile.setFaculty(1);
        when(app.selectUserProfile("owner")).thenReturn(profile);
        UserProfileService dictionaries = new UserProfileService();
        dictionaries.setDegreeMap(Map.of(1, "Degree"));
        dictionaries.setFacultyMap(Map.of(1, "Faculty"));
        dictionaries.setProfessionMap(Map.of(1, "Profession"));
        Introduction bio = new Introduction();
        bio.setIntroductionContent("Synthetic biography");
        when(app.selectUserIntroduction("owner")).thenReturn(bio);
        when(app.selectUserPrivacy("owner")).thenReturn(new PrivacyEntity());
        CetNumberEntity cet = new CetNumberEntity();
        cet.setNumber(100000000000000L);
        when(app.selectUserCetNumber("owner")).thenReturn(cet);
        DeliveryOrderEntity order = new DeliveryOrderEntity();
        order.setState(0);
        when(app.selectUserDeliveryOrderList("owner")).thenReturn(List.of(order));
        DeliveryTradeEntity trade = new DeliveryTradeEntity();
        trade.setState(0);
        when(app.selectUserDeliveryTradeList("owner")).thenReturn(List.of(trade));
        MarketplaceItemEntity item = new MarketplaceItemEntity();
        item.setId(1);
        item.setType(0);
        item.setState(0);
        when(app.selectUserErshouItemList("owner")).thenReturn(List.of(item));
        LostAndFoundItemEntity lost = new LostAndFoundItemEntity();
        lost.setId(2);
        lost.setItemType(0);
        lost.setLostType(0);
        lost.setState(0);
        when(app.selectUserLostAndFoundItemList("owner")).thenReturn(List.of(lost));
        SecretCommentEntity comment = new SecretCommentEntity();
        comment.setUsername("other-person");
        SecretContentEntity voice = new SecretContentEntity();
        voice.setId(3);
        voice.setType(1);
        voice.setState(0);
        voice.setSecretCommentList(List.of(comment));
        SecretContentEntity text = new SecretContentEntity();
        text.setId(4);
        text.setType(0);
        when(app.selectUserSecretItemList("owner")).thenReturn(List.of(voice, text));
        when(secrets.findSecretVoiceObjectKey(3)).thenReturn("secret/3.m4a");
        PhotographEntity photograph = new PhotographEntity();
        photograph.setId(5);
        photograph.setCount(2);
        photograph.setType(0);
        when(app.selectUserPhotographItemList("owner")).thenReturn(List.of(photograph));
        ExpressEntity express = new ExpressEntity();
        express.setSelfGender(0);
        express.setPersonGender(1);
        when(app.selectUserExpressItemList("owner")).thenReturn(List.of(express));
        when(logs.selectChargeLogList("owner")).thenReturn(List.of(new ChargeLog()));
        when(storage.downloadObject(eq("gdeiassistant-userdata"), anyString()))
                .thenAnswer(
                        i -> {
                            String k = i.getArgument(1);
                            return k.endsWith("_1.jpg") && !k.startsWith("photograph")
                                    ? null
                                    : new ByteArrayInputStream(
                                            ("media:" + k).getBytes(StandardCharsets.UTF_8));
                        });
        Map<String, byte[]> zip = new HashMap<>();
        doAnswer(
                        i -> {
                            try (ZipInputStream in = new ZipInputStream(i.getArgument(2))) {
                                ZipEntry e;
                                while ((e = in.getNextEntry()) != null)
                                    zip.put(e.getName(), in.readAllBytes());
                            }
                            return null;
                        })
                .when(storage)
                .uploadObject(
                        eq("gdeiassistant-userdata"),
                        startsWith("export/"),
                        any(InputStream.class));
        service.exportUserData("session");
        assertTrue(zip.containsKey("avatar.jpg"));
        assertTrue(zip.containsKey("avatar_hd.jpg"));
        assertTrue(zip.containsKey("secret_voice_3.m4a"));
        assertTrue(zip.containsKey("photograph_5_2.jpg"));
        assertFalse(zip.containsKey("ershou_1_1.jpg"));
        String json = new String(zip.get("data.json"), StandardCharsets.UTF_8);
        assertFalse(json.contains("synthetic-private-password"));
        assertFalse(json.contains("other-person"));
        assertFalse(json.contains("13800000000"));
        assertTrue(json.contains("138********"));
        var data = JSON.parseObject(json);
        assertEquals("owner", data.getString("username"));
        assertEquals("Degree", data.getJSONObject("profile").getString("degree"));
        assertEquals(
                1, data.getJSONArray("secretItems").getJSONObject(0).getInteger("commentCount"));
        verify(exports).saveExportDataToken(eq("owner"), anyString());
        verify(exports).removeExportingDataToken("owner");
    }

    @AfterEach
    void clearDictionaries() {
        UserProfileService d = new UserProfileService();
        d.setDegreeMap(null);
        d.setFacultyMap(null);
        d.setProfessionMap(null);
    }

    @Test
    void emptyExportStillContainsIdentityAndCompletesTask() throws Exception {
        final byte[][] archive = {null};
        doAnswer(
                        i -> {
                            archive[0] = ((InputStream) i.getArgument(2)).readAllBytes();
                            return null;
                        })
                .when(storage)
                .uploadObject(anyString(), anyString(), any(InputStream.class));
        service.exportUserData("session");
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive[0]))) {
            assertEquals("data.json", zip.getNextEntry().getName());
            assertEquals(
                    "owner",
                    JSON.parseObject(new String(zip.readAllBytes(), StandardCharsets.UTF_8))
                            .getString("username"));
        }
        verify(exports).saveExportDataToken(eq("owner"), anyString());
    }

    @Test
    void failedUploadClearsTaskWithoutAdvertisingDownload() throws Exception {
        doThrow(new IllegalStateException("synthetic storage failure"))
                .when(storage)
                .uploadObject(anyString(), anyString(), any(InputStream.class));
        service.exportUserData("session");
        verify(exports).removeExportingDataToken("owner");
        verify(exports, never()).saveExportDataToken(anyString(), anyString());
    }

    @Test
    void exportStatusAndDownloadAreScopedToOwnerAndBounded() {
        when(exports.queryExportDataToken("owner")).thenReturn("synthetic-export");
        when(exports.queryExportingDataToken("owner")).thenReturn("running");
        when(storage.generatePresignedUrl(
                        "gdeiassistant-userdata",
                        "export/synthetic-export.zip",
                        90,
                        TimeUnit.MINUTES))
                .thenReturn("https://synthetic.invalid/export");
        assertTrue(service.checkAlreadyExportUserData("session"));
        assertTrue(service.checkExportingUserData("session"));
        assertEquals("https://synthetic.invalid/export", service.downloadUserData("session"));
        when(exports.queryExportDataToken("owner")).thenReturn(" ");
        assertNull(service.downloadUserData("session"));
    }

    @Test
    void firstLoginCanInitializeWithoutPersistingPassword() throws Exception {
        ArgumentCaptor<CampusAccountView> c = ArgumentCaptor.forClass(CampusAccountView.class);
        service.syncUserData(user(), false);
        verify(users).insertAppUser(c.capture());
        assertEquals("ACTIVE", c.getValue().getStatus());
        assertNotNull(c.getValue().getPublicId());
        assertNull(c.getValue().getPassword());
        verify(users).insertCampusCredential(c.getValue());
        verify(profiles).initUserProfile(eq("owner"), anyString());
        verify(profiles).initUserIntroduction("owner");
        verify(privacy).initPrivacy("owner");
    }

    @Test
    void existingActiveLoginCanRefreshCredentialButClosedUserIsRejected() throws Exception {
        CampusAccountView account = new CampusAccountView();
        account.setStatus("ACTIVE");
        when(users.selectUser("owner")).thenReturn(account);
        when(profiles.selectUserProfile("owner")).thenReturn(new ProfileEntity());
        when(profiles.selectUserIntroduction("owner")).thenReturn(new Introduction());
        when(privacy.selectPrivacy("owner")).thenReturn(new PrivacyEntity());
        service.syncUserData(user());
        ArgumentCaptor<CampusAccountView> c = ArgumentCaptor.forClass(CampusAccountView.class);
        verify(users).updateUser(c.capture());
        assertEquals("synthetic-private-password", c.getValue().getPassword());
        service.syncUserData(user(), false);
        verify(users, times(1)).updateUser(any());
        account.setStatus("CLOSED");
        assertThrows(IllegalStateException.class, () -> service.syncUserData(user()));
        verify(users, never()).insertAppUser(any());
    }
}
