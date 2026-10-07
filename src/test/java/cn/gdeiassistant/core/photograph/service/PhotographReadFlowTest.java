package cn.gdeiassistant.core.photograph.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.photograph.converter.PhotographCommentConverter;
import cn.gdeiassistant.core.photograph.converter.PhotographConverter;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import cn.gdeiassistant.core.photograph.pojo.dto.PhotographPublishDTO;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.objectstorage.service.StoredAssetService;
import cn.gdeiassistant.core.objectstorage.service.UploadService;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhotographReadFlowTest {
    private final PhotographMapper mapper = mock(PhotographMapper.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final PublicAuthorResolver authors = mock(PublicAuthorResolver.class);
    private final StoredAssetService assets = mock(StoredAssetService.class);
    private final UploadService uploads = mock(UploadService.class);
    private final PhotographService service = new PhotographService();

    @BeforeEach void setup() {
        var converter = Mappers.getMapper(PhotographConverter.class);
        var comments = Mappers.getMapper(PhotographCommentConverter.class);
        ReflectionTestUtils.setField(converter, "photographCommentConverter", comments);
        ReflectionTestUtils.setField(service, "photographMapper", mapper);
        ReflectionTestUtils.setField(service, "photographConverter", converter);
        ReflectionTestUtils.setField(service, "photographCommentConverter", comments);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        ReflectionTestUtils.setField(service, "publicAuthorResolver", authors);
        ReflectionTestUtils.setField(service, "storedAssets", assets);
        ReflectionTestUtils.setField(service, "uploads", uploads);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("viewer"));
        when(authors.resolve("author")).thenReturn(new PublicAuthorResolver.AuthorPublic("public-id", "公开昵称"));
        when(assets.generatePresignedUrl(anyString(), eq(30L), eq(TimeUnit.MINUTES))).thenAnswer(a -> "signed:" + a.getArgument(0));
    }

    private PhotographEntity item() {
        var item = new PhotographEntity(); item.setId(7); item.setUsername("author");
        item.setTitle("合成标题"); item.setContent("合成内容"); item.setCount(2); item.setType(1);
        return item;
    }

    @Test void publicResponseMustNotMutateTheStoredAuthorIdentity() {
        var source = item(); when(mapper.selectPhotograph(0, 10, 1, "viewer")).thenReturn(List.of(source));
        var result = service.queryPhotographList(0, 10, 1, "session").get(0);
        assertEquals("公开昵称", result.getUsername()); assertEquals("public-id", result.getAuthorId());
        assertEquals("signed:photograph/7_1.jpg", result.getFirstImageUrl());
        assertEquals("author", source.getUsername());
        // Repeated conversion of the same mapper result must still resolve the original identity.
        assertEquals("public-id", service.queryPhotographList(0, 10, 1, "session").get(0).getAuthorId());
    }

    @Test void ownedListAndDetailKeepAllPicturesWithFiniteUrlsAndSkipUnidentifiedRows() throws Exception {
        when(mapper.selectPhotographByUsername(0, 10, "viewer")).thenReturn(List.of(new PhotographEntity(), item()));
        var list = service.queryMyPhotographList("session", 0, 10);
        assertEquals(1, list.size()); assertEquals("signed:photograph/7_1.jpg", list.get(0).getFirstImageUrl());
        when(mapper.selectPhotographByIdAndUsername(7, "viewer")).thenReturn(List.of(item()));
        var detail = service.getPhotographById(7, "session");
        assertEquals(List.of("signed:photograph/7_1.jpg", "signed:photograph/7_2.jpg"), detail.getImageUrls());
        assertEquals("合成标题", detail.getTitle()); assertEquals("合成内容", detail.getContent()); assertEquals(1, detail.getType());
        assertTrue(service.queryPhotographList(20, 10, 1, "session").isEmpty());
        assertTrue(service.queryMyPhotographList("session", 20, 10).isEmpty());
    }

    @Test void missingStatisticsAreZeroAndPositiveCountsStayDistinct() {
        assertEquals(0, service.queryPhotoStatisticalData()); assertEquals(0, service.queryCommentStatisticalData());
        assertEquals(0, service.queryLikeStatisticalData());
        when(mapper.selectPhotographImageCount()).thenReturn(12); when(mapper.selectPhotographCommentCount()).thenReturn(4);
        when(mapper.selectPhotographLikeCount()).thenReturn(8);
        assertEquals(12, service.queryPhotoStatisticalData()); assertEquals(4, service.queryCommentStatisticalData());
        assertEquals(8, service.queryLikeStatisticalData());
    }

    @Test void publicationUsesGeneratedIdAndSessionBoundTicketsOrNonemptyFiles() throws Exception {
        var dto = new PhotographPublishDTO(); dto.setTitle("合成标题"); dto.setContent("合成内容"); dto.setType(1); dto.setCount(2);
        doAnswer(a -> { ((PhotographEntity) a.getArgument(0)).setId(7); return null; }).when(mapper).insertPhotograph(any());
        service.publishPhotograph(dto, "session", null, new String[]{"one", "two"});
        verify(uploads).moveUpload("session", "one", "photograph/7_1.jpg");
        verify(uploads).moveUpload("session", "two", "photograph/7_2.jpg");
        var image = new MockMultipartFile("image", "image.jpg", "image/jpeg", new byte[]{1, 2});
        service.publishPhotograph(dto, "session", new MockMultipartFile[]{null, new MockMultipartFile("image", new byte[0]), image}, null);
        verify(assets).uploadObject(eq("photograph/7_1.jpg"), any());
        doThrow(new IllegalStateException("synthetic delete outage")).when(assets).deleteObject("photograph/7_1.jpg");
        service.deletePhotographImages(7, 2); verify(assets).deleteObject("photograph/7_2.jpg");
        service.deletePhotograph(7); verify(mapper).deletePhotograph(7);
    }
}
