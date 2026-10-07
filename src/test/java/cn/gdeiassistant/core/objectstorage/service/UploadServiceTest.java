package cn.gdeiassistant.core.objectstorage.service;

import cn.gdeiassistant.common.tools.springutils.*;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class UploadServiceTest {
    UploadService service; R2StorageService storage; RedisDaoUtils redis; StoredAssetService assets;
    String owner=StringEncryptUtils.sha256HexString("synthetic-owner"),key="upload/"+owner+"/sample.png";
    byte[] image;
    @BeforeEach void setup() throws Exception {
        service=new UploadService();storage=mock(R2StorageService.class);redis=mock(RedisDaoUtils.class);assets=mock(StoredAssetService.class);
        var certificates=mock(UserCertificateService.class);when(certificates.getUserLoginCertificate("session")).thenReturn(new User("synthetic-owner"));
        ReflectionTestUtils.setField(service,"r2StorageService",storage);ReflectionTestUtils.setField(service,"redis",redis);ReflectionTestUtils.setField(service,"assets",assets);ReflectionTestUtils.setField(service,"certificates",certificates);
        var bytes=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",bytes);image=bytes.toByteArray();
    }
    void ticket() {
        when(redis.get("upload-ticket:"+key)).thenReturn("image/png");
        when(storage.headObjectMetadata(key)).thenReturn(HeadObjectResponse.builder().contentType("image/png").contentLength((long)image.length).eTag("version-one").build());
    }
    @Test void ownershipIsEnforcedBeforeReadingAnotherUsersTicketOrObject() {
        assertThrows(SecurityException.class,()->service.moveUpload("session","upload/another-owner/sample.png","topic/1_1.jpg"));verifyNoInteractions(storage,redis,assets);
        assertThrows(SecurityException.class,()->service.moveUpload("session",key+"/../other","topic/1_1.jpg"));verifyNoInteractions(storage,redis,assets);
    }
    @Test void mimeAndContentMismatchDoNotConsumeTicket() throws Exception {
        ticket();when(storage.downloadBytesBounded(isNull(),eq(key),anyInt())).thenReturn(new byte[20]);
        assertThrows(IllegalArgumentException.class,()->service.moveUpload("session",key,"topic/1_1.jpg"));verify(redis,never()).compareAndDelete(anyString(),anyString());verifyNoInteractions(assets);
        assertThrows(IllegalArgumentException.class,()->service.moveUpload("session",key,"secret/voice/1.mp3"));
    }
    @Test void publicationConsumesTicketOnceAndConditionsCopyOnVerifiedVersion() throws Exception {
        ticket();when(storage.downloadBytesBounded(isNull(),eq(key),anyInt())).thenReturn(image);
        when(redis.compareAndDelete("upload-ticket:"+key,"image/png")).thenReturn(true,false);
        service.moveUpload("session",key,"topic/1_1.jpg");
        verify(assets).pending("topic/1_1.jpg",owner,"image/png",image.length);verify(storage).moveVerifiedObject(key,"topic/1_1.jpg","version-one");verify(assets).ready("topic/1_1.jpg");
        assertThrows(IllegalArgumentException.class,()->service.moveUpload("session",key,"topic/2_1.jpg"));verify(storage,never()).moveVerifiedObject(eq(key),eq("topic/2_1.jpg"),anyString());
    }
    @Test void presignBindsOwnerAndPersistsFiniteTicketAndCleanupMetadata() {
        when(storage.generatePutPresignedUrl(anyString(),eq("image/png"),eq(15L),eq(TimeUnit.MINUTES))).thenReturn("https://synthetic.invalid/put");
        var result=service.createPresignedUpload("session","../../picture.PNG","image/png");
        assertTrue(result.get("objectKey").startsWith("upload/"+owner+"/"));assertFalse(result.get("objectKey").contains(".."));assertTrue(result.get("objectKey").endsWith(".png"));
        verify(redis).set("upload-ticket:"+result.get("objectKey"),"image/png",20,TimeUnit.MINUTES);verify(assets).pending(result.get("objectKey"),owner,"image/png",0);
    }
    @Test void webmRequiresItsContainerAndAudioCodecMarkers() {
        byte[] bytes=new byte[40];bytes[0]=0x1a;bytes[1]=0x45;bytes[2]=(byte)0xdf;bytes[3]=(byte)0xa3;
        System.arraycopy("webm".getBytes(java.nio.charset.StandardCharsets.US_ASCII),0,bytes,8,4);
        assertNull(UploadService.validateMedia(bytes));
        System.arraycopy("A_OPUS".getBytes(java.nio.charset.StandardCharsets.US_ASCII),0,bytes,24,6);
        assertEquals("audio/webm",UploadService.validateMedia(bytes));
        bytes[0]=0;assertNull(UploadService.validateMedia(bytes));
    }

}
