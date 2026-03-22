package cn.gdeiassistant.core.marketplace.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock
    private MarketplaceMapper marketplaceMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private MarketplaceService marketplaceService;

    @Test
    void queryItemsReturnsListFromMapper() throws Exception {
        MarketplaceItemEntity item = new MarketplaceItemEntity();
        item.setId(1);
        item.setName("Textbook");
        when(marketplaceMapper.selectAvailableItems(0, 10)).thenReturn(List.of(item));

        List<MarketplaceItemEntity> result = marketplaceService.queryItems(0);

        assertEquals(1, result.size());
        assertEquals("Textbook", result.get(0).getName());
        verify(marketplaceMapper).selectAvailableItems(0, 10);
    }

    @Test
    void queryItemsReturnsEmptyListWhenMapperReturnsNull() throws Exception {
        when(marketplaceMapper.selectAvailableItems(0, 10)).thenReturn(null);

        List<MarketplaceItemEntity> result = marketplaceService.queryItems(0);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void queryDetailByIdThrowsDataNotExistExceptionWhenItemNotFound() {
        when(marketplaceMapper.selectInfoByID(999)).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> marketplaceService.queryDetailById(999));
    }

    @Test
    void queryDetailByIdReturnsVOWithProfileAndPictureURLs() throws Exception {
        MarketplaceItemEntity item = new MarketplaceItemEntity();
        item.setId(1);
        item.setUsername("testuser");
        MarketplaceItemVO vo = new MarketplaceItemVO();
        vo.setSecondhandItem(item);
        vo.setProfile(new ProfileVO());
        when(marketplaceMapper.selectInfoByID(1)).thenReturn(vo);
        when(r2StorageService.generatePresignedUrl(eq("gdeiassistant-userdata"),
                eq("ershou/1_1.jpg"), eq(30L), eq(TimeUnit.MINUTES))).thenReturn("https://pic1.jpg");
        when(r2StorageService.generatePresignedUrl(eq("gdeiassistant-userdata"),
                eq("ershou/1_2.jpg"), eq(30L), eq(TimeUnit.MINUTES))).thenReturn("");
        when(userProfileService.getOtherUserAvatar("testuser")).thenReturn("https://avatar.jpg");

        MarketplaceItemVO result = marketplaceService.queryDetailById(1);

        assertEquals("testuser", result.getSecondhandItem().getUsername());
        assertEquals(1, result.getSecondhandItem().getPictureURL().size());
        assertEquals("https://pic1.jpg", result.getSecondhandItem().getPictureURL().get(0));
        assertEquals("https://avatar.jpg", result.getProfile().getAvatarURL());
    }

    @Test
    void updateItemStateThrowsDataNotExistExceptionWhenItemNotFound() {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("owner"));
        when(marketplaceMapper.selectInfoByID(999)).thenReturn(null);

        assertThrows(DataNotExistException.class,
                () -> marketplaceService.updateItemState("session1", 999, 1));
    }

    @Test
    void updateItemStateThrowsNoAccessExceptionWhenUserDoesNotOwnItem() {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("other"));
        MarketplaceItemEntity item = new MarketplaceItemEntity();
        item.setUsername("owner");
        item.setState(1);
        MarketplaceItemVO vo = new MarketplaceItemVO();
        vo.setSecondhandItem(item);
        when(marketplaceMapper.selectInfoByID(1)).thenReturn(vo);

        assertThrows(NoAccessException.class,
                () -> marketplaceService.updateItemState("session1", 1, 0));
    }

    @Test
    void uploadItemPictureThrowsOnR2Failure() {
        doThrow(new RuntimeException("R2 down"))
                .when(r2StorageService).uploadObject(eq("gdeiassistant-userdata"), anyString(), any(InputStream.class));

        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        assertThrows(RuntimeException.class, () -> marketplaceService.uploadItemPicture(1, 1, stream));
    }

    @Test
    void deleteItem_callsMapperDelete() {
        marketplaceService.deleteItem(99);
        verify(marketplaceMapper).deleteItem(99);
    }

    @Test
    void deleteItemImages_callsR2DeleteForEachIndex() {
        marketplaceService.deleteItemImages(99, 3);
        verify(r2StorageService).deleteObject(eq("gdeiassistant-userdata"), eq("ershou/99_1.jpg"));
        verify(r2StorageService).deleteObject(eq("gdeiassistant-userdata"), eq("ershou/99_2.jpg"));
        verify(r2StorageService).deleteObject(eq("gdeiassistant-userdata"), eq("ershou/99_3.jpg"));
    }
}
