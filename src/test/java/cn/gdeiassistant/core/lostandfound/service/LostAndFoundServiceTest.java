package cn.gdeiassistant.core.lostandfound.service;

import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LostAndFoundServiceTest {

    @Mock
    private LostAndFoundMapper lostAndFoundMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private cn.gdeiassistant.core.objectstorage.service.StoredAssetService storedAssets;

    @InjectMocks
    private LostAndFoundService lostAndFoundService;

    @Test
    void uploadPicture_throwsOnR2Failure() {
        doThrow(new RuntimeException("R2 down"))
                .when(storedAssets).uploadObject(eq((String) null), anyString(), any(InputStream.class));

        InputStream stream = new ByteArrayInputStream(new byte[]{1});
        assertThrows(RuntimeException.class,
                () -> lostAndFoundService.uploadLostAndFoundItemPicture(1, 1, stream));
    }

    @Test
    void deleteLostAndFoundItem_callsMapperDelete() {
        lostAndFoundService.deleteLostAndFoundItem(77);
        verify(lostAndFoundMapper).deleteItem(77);
    }

    @Test
    void deleteLostAndFoundItemImages_callsR2DeleteForEachIndex() {
        lostAndFoundService.deleteLostAndFoundItemImages(77, 2);
        verify(storedAssets).deleteObject(eq((String) null), eq("lostandfound/77_1.jpg"));
        verify(storedAssets).deleteObject(eq((String) null), eq("lostandfound/77_2.jpg"));
    }
}
