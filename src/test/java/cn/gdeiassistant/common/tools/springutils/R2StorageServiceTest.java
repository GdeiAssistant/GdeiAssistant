package cn.gdeiassistant.common.tools.springutils;

import cn.gdeiassistant.common.pojo.config.R2Config;
import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class R2StorageServiceTest {
    R2StorageService storage;S3Client client;S3Presigner signer;R2Config config;
    @BeforeEach void setup(){storage=new R2StorageService();client=mock(S3Client.class);signer=mock(S3Presigner.class);config=mock(R2Config.class);
        when(config.isEnabled()).thenReturn(true);when(config.getBucketName()).thenReturn("synthetic-bucket");
        ReflectionTestUtils.setField(storage,"r2Config",config);ReflectionTestUtils.setField(storage,"s3Client",client);ReflectionTestUtils.setField(storage,"s3Presigner",signer);}
    @Test void deletionDoesNotHidePermissionOrNetworkFailure(){when(client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(new IllegalStateException("synthetic outage"));assertThrows(IllegalStateException.class,()->storage.deleteObject("avatar/synthetic.jpg"));}
    @Test void missingObjectIsAnIdempotentSuccessfulDeletion(){when(client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(NoSuchKeyException.builder().message("missing").build());assertDoesNotThrow(()->storage.deleteObject("avatar/synthetic.jpg"));}
    @Test void disabledStorageDoesNotPretendCleanupSucceeded(){when(config.isEnabled()).thenReturn(false);assertThrows(FeatureNotEnabledException.class,()->storage.deleteObject("avatar/synthetic.jpg"));}
    @Test void confirmedObjectSigningNeverChecksRemoteExistenceOrRewritesSignedHost() throws Exception {var signed=mock(PresignedGetObjectRequest.class);when(signed.url()).thenReturn(new URL("https://signed.example.test/object?signature=synthetic"));when(signer.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(signed);
        assertEquals("https://signed.example.test/object?signature=synthetic",storage.generateKnownObjectUrl(null,"topic/1_1.jpg",10,TimeUnit.MINUTES));verifyNoInteractions(client);}
    @Test void conditionalCopyPinsValidatedObjectVersion(){storage.moveVerifiedObject("upload/synthetic/object.png","topic/1_1.jpg","synthetic-etag");var request=org.mockito.ArgumentCaptor.forClass(CopyObjectRequest.class);verify(client).copyObject(request.capture());assertEquals("synthetic-etag",request.getValue().copySourceIfMatch());verify(client).deleteObject(any(DeleteObjectRequest.class));}
    @Test void permanentSourcesAreRejectedBeforeRemoteAccess(){assertThrows(IllegalArgumentException.class,()->storage.moveVerifiedObject("avatar/other.jpg","topic/1.jpg","etag"));verifyNoInteractions(client);}
    @Test void strictUploadUsesConfiguredBucketAndActualLengthAndDoesNotAcceptMissingData() throws Exception {
        byte[] bytes={1,2,3};storage.uploadBytesStrict(null,"object",bytes,"image/png");
        var request=org.mockito.ArgumentCaptor.forClass(PutObjectRequest.class);
        var body=org.mockito.ArgumentCaptor.forClass(software.amazon.awssdk.core.sync.RequestBody.class);
        verify(client).putObject(request.capture(),body.capture());
        assertEquals("synthetic-bucket",request.getValue().bucket());assertEquals(3L,request.getValue().contentLength());
        assertEquals("image/png",request.getValue().contentType());assertArrayEquals(bytes,body.getValue().contentStreamProvider().newStream().readAllBytes());
        assertThrows(IllegalArgumentException.class,()->storage.uploadBytesStrict(null,"object",null,null));
        assertThrows(IllegalArgumentException.class,()->storage.uploadBytesStrict(null," ",bytes,null));
        assertThrows(IllegalArgumentException.class,()->storage.deleteObjectStrict(null," "));
    }
    @Test void uploadStreamPreservesReadFailureAndMimeAndExplicitBucket() throws Exception {
        storage.uploadObject("explicit","export.zip",new java.io.ByteArrayInputStream(new byte[]{1,2}));
        var request=org.mockito.ArgumentCaptor.forClass(PutObjectRequest.class);verify(client).putObject(request.capture(),any(software.amazon.awssdk.core.sync.RequestBody.class));
        assertEquals("explicit",request.getValue().bucket());assertEquals("application/zip",request.getValue().contentType());
        assertThrows(RuntimeException.class,()->storage.uploadObject("x",new java.io.InputStream(){public int read() throws java.io.IOException{throw new java.io.IOException("synthetic");}}));
        storage.uploadObject("x",null);
        when(config.isEnabled()).thenReturn(false);
        assertThrows(FeatureNotEnabledException.class,()->storage.uploadObject("x",new java.io.ByteArrayInputStream(new byte[]{1})));
    }
    @Test void boundedDownloadRejectsOversizeAndClosesStreamAndPreservesMissingVsFailure() throws Exception {
        var stream=spy(new java.io.ByteArrayInputStream(new byte[]{1,2,3}));
        var response=new software.amazon.awssdk.core.ResponseInputStream<>(GetObjectResponse.builder().build(),stream);
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(response);
        assertThrows(java.io.IOException.class,()->storage.downloadBytesBounded(null,"x",2));verify(stream).close();
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(new software.amazon.awssdk.core.ResponseInputStream<>(GetObjectResponse.builder().build(),new java.io.ByteArrayInputStream(new byte[]{1,2})));
        assertArrayEquals(new byte[]{1,2},storage.downloadBytesBounded(null,"x",2));
        when(client.getObject(any(GetObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertNull(storage.downloadBytesBounded(null,"x",2));assertNull(storage.downloadObject("x"));
        when(client.getObject(any(GetObjectRequest.class))).thenThrow(new IllegalStateException("outage"));
        assertThrows(IllegalStateException.class,()->storage.downloadObject("x"));
        assertThrows(IllegalArgumentException.class,()->storage.downloadBytesBounded(null,"x",0));assertNull(storage.downloadBytesBounded(null," ",2));
        when(config.isEnabled()).thenReturn(false);assertNull(storage.downloadObject("x"));assertNull(storage.downloadBytesBounded(null,"x",2));
    }
    @Test void missingHeadDoesNotSignButExistingObjectCanBeSigned() throws Exception {
        when(client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.builder().build());
        assertNull(storage.headObjectMetadata("x"));assertEquals("",storage.generatePresignedUrl("x",1,TimeUnit.MINUTES));verifyNoInteractions(signer);
        when(client.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().contentLength(3L).contentType("image/png").eTag("etag").build());
        var signed=mock(PresignedGetObjectRequest.class);when(signed.url()).thenReturn(new URL("https://signed.example.test/x"));when(signer.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(signed);
        assertEquals(3L,storage.headObjectMetadata("x").contentLength());assertEquals("https://signed.example.test/x",storage.generatePresignedUrl("x",1,TimeUnit.MINUTES));
        when(client.headObject(any(HeadObjectRequest.class))).thenThrow(new IllegalStateException("outage"));assertThrows(IllegalStateException.class,()->storage.headObjectMetadata("x"));
        assertEquals("",storage.generatePresignedUrl("x",1,TimeUnit.MINUTES));
    }
    @Test void uploadSigningHasFiniteDurationAndPinsMimeOnOriginalHost() throws Exception {
        var signed=mock(PresignedPutObjectRequest.class);when(signed.url()).thenReturn(new URL("http://signed.example.test/x"));when(signer.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(signed);
        assertEquals("https://signed.example.test/x",storage.generatePutPresignedUrl("x","image/png",15,TimeUnit.MINUTES));
        var request=org.mockito.ArgumentCaptor.forClass(PutObjectPresignRequest.class);verify(signer).presignPutObject(request.capture());
        assertEquals(java.time.Duration.ofMinutes(15),request.getValue().signatureDuration());assertEquals("image/png",request.getValue().putObjectRequest().contentType());
        when(config.getEffectivePresignEndpoint()).thenReturn("http://127.0.0.1:1234");assertEquals("http://signed.example.test/x",storage.generatePutPresignedUrl("x","image/png",1,TimeUnit.MINUTES));
        when(config.isEnabled()).thenReturn(false);assertThrows(FeatureNotEnabledException.class,()->storage.generatePutPresignedUrl("x","image/png",1,TimeUnit.MINUTES));
        assertThrows(FeatureNotEnabledException.class,()->storage.headObjectMetadata("x"));assertEquals("",storage.generateKnownObjectUrl(null,"x",1,TimeUnit.MINUTES));assertEquals("",storage.generatePresignedUrl("x",1,TimeUnit.MINUTES));
    }
    @Test void copyCannotDeleteSourceOnFailureAndRequiresVersionAndValidSource() {
        for(String key:new String[]{null,"","upload/../x","other/x"})assertThrows(IllegalArgumentException.class,()->storage.moveVerifiedObject(key,"target","etag"));
        assertThrows(IllegalArgumentException.class,()->storage.moveVerifiedObject("upload/x","target"," "));
        when(client.copyObject(any(CopyObjectRequest.class))).thenThrow(new IllegalStateException("synthetic copy failure"));
        assertThrows(IllegalStateException.class,()->storage.moveVerifiedObject("upload/x","target","etag"));verify(client,never()).deleteObject(any(DeleteObjectRequest.class));
        when(config.isEnabled()).thenReturn(false);assertThrows(FeatureNotEnabledException.class,()->storage.moveVerifiedObject("upload/x","target","etag"));
        assertThrows(FeatureNotEnabledException.class,()->storage.uploadBytesStrict(null,"x",new byte[]{1},null));
    }

}
