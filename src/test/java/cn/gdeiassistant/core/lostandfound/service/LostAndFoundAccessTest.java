package cn.gdeiassistant.core.lostandfound.service;

import cn.gdeiassistant.common.exception.databaseexception.*;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.lostandfound.converter.LostAndFoundItemConverter;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.dto.LostAndFoundPublishDTO;
import cn.gdeiassistant.core.lostandfound.pojo.entity.*;
import cn.gdeiassistant.core.objectstorage.service.StoredAssetService;
import cn.gdeiassistant.core.objectstorage.service.UploadService;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LostAndFoundAccessTest {
    private final LostAndFoundMapper mapper = mock(LostAndFoundMapper.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final PublicAuthorResolver authors = mock(PublicAuthorResolver.class);
    private final StoredAssetService assets = mock(StoredAssetService.class);
    private final UploadService uploads = mock(UploadService.class);
    private final LostAndFoundService service = new LostAndFoundService();
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "lostAndFoundMapper", mapper);
        ReflectionTestUtils.setField(service, "lostAndFoundItemConverter", Mappers.getMapper(LostAndFoundItemConverter.class));
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        ReflectionTestUtils.setField(service, "publicAuthorResolver", authors);
        ReflectionTestUtils.setField(service, "storedAssets", assets); ReflectionTestUtils.setField(service, "uploads", uploads);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("owner"));
    }
    private LostAndFoundDetailEntity detail(String owner, int state) {
        var item = new LostAndFoundItemEntity(); item.setId(1); item.setUsername(owner); item.setState(state); item.setName("合成物品");
        var detail = new LostAndFoundDetailEntity(); detail.setItem(item); return detail;
    }
    private LostAndFoundPublishDTO dto() {
        var dto = new LostAndFoundPublishDTO(); dto.setName("合成物品"); dto.setDescription("描述"); dto.setLocation("合成地点");
        dto.setItemType(2); dto.setLostType(0); dto.setQq("synthetic-qq"); dto.setWechat("synthetic-wechat"); dto.setPhone("13800000000"); return dto;
    }
    @Test void missingOtherOwnerAndConfirmedItemsCannotBeEdited() throws Exception {
        assertThrows(DataNotExistException.class, () -> service.updateLostAndFoundItem(dto(), 1, "session"));
        when(mapper.selectInfoByID(1)).thenReturn(detail("another", 0));
        assertThrows(NoAccessException.class, () -> service.updateLostAndFoundItem(dto(), 1, "session"));
        when(mapper.selectInfoByID(1)).thenReturn(detail("owner", 1));
        assertThrows(ConfirmedStateException.class, () -> service.updateLostAndFoundItem(dto(), 1, "session"));
        assertThrows(ConfirmedStateException.class, () -> service.updateLostAndFoundItemState(1, 0));
        verify(mapper, never()).updateItemItem(any()); verify(mapper, never()).updateItemState(anyInt(), anyInt());
    }
    @Test void allowedUpdatePersistsFieldsAndStateOnTheSameId() throws Exception {
        when(mapper.selectInfoByID(1)).thenReturn(detail("owner", 0));
        service.updateLostAndFoundItem(dto(), 1, "session"); service.updateLostAndFoundItemState(1, 1);
        var saved = ArgumentCaptor.forClass(LostAndFoundItemEntity.class); verify(mapper).updateItemItem(saved.capture());
        assertEquals(1, saved.getValue().getId()); assertEquals("合成物品", saved.getValue().getName());
        assertEquals("描述", saved.getValue().getDescription()); assertEquals("合成地点", saved.getValue().getLocation());
        assertEquals(2, saved.getValue().getItemType()); assertEquals("synthetic-wechat", saved.getValue().getWechat());
        verify(mapper).updateItemState(1, 1);
    }
    @Test void publicListsReplaceCampusIdentityAndPersonalListsUseFiniteUrls() throws Exception {
        var item = detail("owner", 0).getItem();
        when(authors.resolve("owner")).thenReturn(new PublicAuthorResolver.AuthorPublic("public-id", "昵称"));
        when(mapper.selectAvailableItem(anyInt(), anyInt(), eq(10))).thenReturn(List.of(item));
        when(mapper.selectItemWithKeyword(anyInt(), eq("keyword"), anyInt(), eq(10))).thenReturn(List.of(item));
        when(mapper.selectItemByItemType(anyInt(), eq(2), anyInt(), eq(10))).thenReturn(List.of(item));
        for (var list : List.of(service.queryLostItems(0), service.queryFoundItems(0), service.queryLostItemsWithKeyword("keyword", 0),
                service.queryFoundItemsWithKeyword("keyword", 0), service.queryLostItemsByType(2, 0), service.queryFoundItemsByType(2, 0))) {
            assertEquals("public-id", list.get(0).getAuthorId()); assertEquals("昵称", list.get(0).getUsername());
        }
        when(mapper.selectItemByUsername("owner", 0, 25)).thenReturn(List.of(item));
        when(assets.generatePresignedUrl("lostandfound/1_1.jpg", 30, TimeUnit.MINUTES)).thenReturn("synthetic-url");
        assertEquals(List.of("synthetic-url"), service.queryPersonalLostAndFoundItems("session", 0, 25).get(0).getPictureURL());
        assertEquals(List.of("synthetic-url"), service.getLostAndFoundItemPictureURL(1));
        assertTrue(service.queryPersonalLostAndFoundItems("session", 25, 25).isEmpty());
    }
    @Test void publicationMapsOwnerAndMovesOnlyOwnedTicketsOrNonemptyFiles() throws Exception {
        doAnswer(a -> { ((LostAndFoundItemEntity)a.getArgument(0)).setId(7); return null; }).when(mapper).insertItem(any());
        var saved = service.addLostAndFoundItem(dto(), "session"); assertEquals("owner", saved.getUsername());
        assertEquals(0, saved.getState()); assertNotNull(saved.getPublishTime());
        service.publishItem(dto(), "session", null, new String[]{"ticket-one", "ticket-two"});
        verify(uploads).moveUpload("session", "ticket-one", "lostandfound/7_1.jpg");
        verify(uploads).moveUpload("session", "ticket-two", "lostandfound/7_2.jpg");
        service.publishItem(dto(), "session", new MockMultipartFile[]{null, new MockMultipartFile("image", new byte[0]),
                new MockMultipartFile("image", new byte[]{1})}, null);
        verify(assets).uploadObject(eq("lostandfound/7_1.jpg"), any());
    }
}
