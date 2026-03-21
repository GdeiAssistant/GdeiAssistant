package cn.gdeiassistant.core.userdata.service;

import cn.gdeiassistant.common.constant.ItemConstantUtils;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.redis.ExportData.ExportDataDao;
import cn.gdeiassistant.core.data.mapper.AppDataMapper;
import cn.gdeiassistant.core.userData.service.UserDataService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import cn.gdeiassistant.core.secret.service.SecretService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDataServiceTest {

    @Mock
    private AppDataMapper appDataMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private ExportDataDao exportDataDao;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private SecretService secretService;

    @InjectMocks
    private UserDataService userDataService;

    @Test
    void replaceWithLabel_returnsLabelForValidIndex() throws Exception {
        Method method = UserDataService.class.getDeclaredMethod(
                "replaceWithLabel", Map.class, String.class, String[].class);
        method.setAccessible(true);

        Map<String, Object> object = new HashMap<>();
        object.put("type", 1);

        method.invoke(null, object, "type", ItemConstantUtils.PHOTOGRAPH_ITEM_TYPE);

        assertEquals("校园照", object.get("type"));
    }

    @Test
    void replaceWithLabel_doesNothingForOutOfBoundsIndex() throws Exception {
        Method method = UserDataService.class.getDeclaredMethod(
                "replaceWithLabel", Map.class, String.class, String[].class);
        method.setAccessible(true);

        Map<String, Object> object = new HashMap<>();
        object.put("type", 999);

        method.invoke(null, object, "type", ItemConstantUtils.PHOTOGRAPH_ITEM_TYPE);

        assertEquals(999, object.get("type"));
    }

    @Test
    void resolveLocation_handlesNullRegionCodeGracefully() throws Exception {
        Method method = UserDataService.class.getDeclaredMethod(
                "resolveLocation", Map.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Map<String, Object> object = new HashMap<>();
        object.put("locationRegion", null);
        object.put("locationState", "01");
        object.put("locationCity", "001");

        assertDoesNotThrow(() ->
                method.invoke(null, object, "locationRegion", "locationState", "locationCity"));
    }

    @Test
    void checkExportingUserData_returnsFalseWhenNoTokenExists() {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(exportDataDao.QueryExportingDataToken("testuser")).thenReturn(null);

        assertFalse(userDataService.CheckExportingUserData("session1"));
    }

    @Test
    void checkAlreadyExportUserData_returnsFalseWhenNoTokenExists() {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(exportDataDao.QueryExportDataToken("testuser")).thenReturn(null);

        assertFalse(userDataService.CheckAlreadyExportUserData("session1"));
    }
}
