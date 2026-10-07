package cn.gdeiassistant.core.topic.service;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.topic.converter.TopicConverter;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.pojo.dto.TopicPublishDTO;
import cn.gdeiassistant.core.topic.pojo.entity.*;
import cn.gdeiassistant.core.objectstorage.service.StoredAssetService;
import cn.gdeiassistant.core.objectstorage.service.UploadService;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopicServiceTest {
    private final TopicMapper mapper = mock(TopicMapper.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final PublicAuthorResolver authors = mock(PublicAuthorResolver.class);
    private final StoredAssetService assets = mock(StoredAssetService.class);
    private final UploadService uploads = mock(UploadService.class);
    private final InteractionNotificationService notifications = mock(InteractionNotificationService.class);
    private final TopicService service = new TopicService();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service,"topicMapper",mapper); ReflectionTestUtils.setField(service,"topicConverter",Mappers.getMapper(TopicConverter.class));
        ReflectionTestUtils.setField(service,"userCertificateService",certificates); ReflectionTestUtils.setField(service,"publicAuthors",authors);
        ReflectionTestUtils.setField(service,"storedAssets",assets); ReflectionTestUtils.setField(service,"uploads",uploads);
        ReflectionTestUtils.setField(service,"interactionNotificationService",notifications);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("viewer"));
    }
    private TopicEntity item() { var item=new TopicEntity();item.setId(1);item.setUsername("author");item.setTopic("合成话题");item.setContent("合成内容");item.setCount(2);return item; }
    @Test void paginatedListsResolvePublicAuthorsInBatchAndDetailsIncludeAllPictures() throws Exception {
        var item=item();var author=new PublicAuthorResolver.AuthorPublic("public-id","昵称");
        when(authors.resolveAll(List.of("author"))).thenReturn(Map.of("author",author)); when(authors.resolve("author")).thenReturn(author);
        when(mapper.selectTopicPage(0,10,"viewer")).thenReturn(List.of(item));
        when(mapper.selectTopicPageByKeyword(0,10,"viewer","keyword")).thenReturn(List.of(item));
        when(mapper.selectTopicByUsername(0,10,"viewer","viewer")).thenReturn(List.of(item));
        when(assets.generatePresignedUrl(anyString(),eq(90L),eq(TimeUnit.MINUTES))).thenAnswer(a -> "signed:"+a.getArgument(0));
        for(var list:List.of(service.queryTopic("session",0,10),service.queryTopicByKeyword("session",0,10,"keyword"),service.queryMyTopicList("session",0,10))) {
            assertEquals("昵称",list.get(0).getUsername());assertEquals("public-id",list.get(0).getAuthorId());assertEquals("signed:topic/1_1.jpg",list.get(0).getFirstImageUrl());
        }
        when(mapper.selectTopicById(1,"viewer")).thenReturn(item);
        assertEquals(List.of("signed:topic/1_1.jpg","signed:topic/1_2.jpg"),service.queryTopicById(1,"session").getImageUrls());
        assertThrows(DataNotExistException.class,()->service.queryTopicById(99,"session"));
        assertTrue(service.queryTopic("session",20,10).isEmpty());
    }
    @Test void duplicateLikesNeverProduceDuplicateNotification() throws Exception {
        var item=item();when(mapper.selectTopicById(1,"viewer")).thenReturn(item);
        when(mapper.insertTopicLike(1,"viewer")).thenReturn(1,0);
        service.likeTopic(1,"session");service.likeTopic(1,"session");
        when(mapper.selectTopicLike(1,"viewer")).thenReturn(new TopicLikeEntity());service.likeTopic(1,"session");
        verify(notifications,times(1)).createInteractionNotification(eq("topic"),eq("like"),eq("author"),eq("viewer"),eq("1"),isNull(),eq("like"),anyString(),eq("用户 点赞了你的话题"));
        assertThrows(DataNotExistException.class,()->service.likeTopic(99,"session"));
    }
    @Test void publicationUsesGeneratedIdAndSessionOwnershipForImages() throws Exception {
        var dto=new TopicPublishDTO();dto.setTopic("合成话题");dto.setContent("合成内容");dto.setCount(2);
        doAnswer(a->{((TopicEntity)a.getArgument(0)).setId(7);return null;}).when(mapper).insertTopic(any());
        when(authors.resolve("viewer")).thenReturn(new PublicAuthorResolver.AuthorPublic("public-id","昵称"));
        var view=service.addTopic(dto,"session");assertEquals("合成话题",view.getTopic());assertEquals("合成内容",view.getContent());assertEquals(2,view.getCount());
        service.publishTopic(dto,"session",null,new String[]{"one","two"});
        verify(uploads).moveUpload("session","one","topic/7_1.jpg");verify(uploads).moveUpload("session","two","topic/7_2.jpg");
        doThrow(new IllegalStateException("synthetic outage")).when(assets).deleteObject("topic/7_1.jpg");
        service.deleteTopicImages(7,2);verify(assets).deleteObject("topic/7_2.jpg");service.deleteTopic(7);verify(mapper).deleteTopic(7);
    }
}
