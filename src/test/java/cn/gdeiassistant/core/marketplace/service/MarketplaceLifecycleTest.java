package cn.gdeiassistant.core.marketplace.service;
import cn.gdeiassistant.common.exception.databaseexception.*;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.dto.MarketplacePublishDTO;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.objectstorage.service.*;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MarketplaceLifecycleTest {
    private final MarketplaceService service=new MarketplaceService();
    private final MarketplaceMapper mapper=mock(MarketplaceMapper.class);
    private final UserCertificateService certificates=mock(UserCertificateService.class);
    private final UploadService uploads=mock(UploadService.class);
    private final StoredAssetService assets=mock(StoredAssetService.class);
    private final PublicAuthorResolver authors=mock(PublicAuthorResolver.class);
    @BeforeEach void setup(){ReflectionTestUtils.setField(service,"marketplaceMapper",mapper);ReflectionTestUtils.setField(service,"userCertificateService",certificates);ReflectionTestUtils.setField(service,"uploads",uploads);ReflectionTestUtils.setField(service,"storedAssets",assets);ReflectionTestUtils.setField(service,"publicAuthorResolver",authors);when(certificates.getUserLoginCertificate("session")).thenReturn(new User("synthetic-owner"));}
    private MarketplaceItemEntity item(int id,String owner,int state){var item=new MarketplaceItemEntity();item.setId(id);item.setUsername(owner);item.setState(state);return item;}
    private void detail(MarketplaceItemEntity item){var detail=new MarketplaceItemVO();detail.setMarketplaceItem(item);when(mapper.selectInfoByID(item.getId())).thenReturn(detail);}
    private MarketplacePublishDTO publish(){var dto=new MarketplacePublishDTO();dto.setName("合成商品");dto.setDescription("合成说明");dto.setPrice(new BigDecimal("12.30"));dto.setLocation("合成地点");dto.setType(2);dto.setQq("synthetic-contact");return dto;}
    @Test void publishUsesSessionOwnerAndDecimalPriceAndMovesOnlySpecifiedImages() throws Exception {
        doAnswer(call->{((MarketplaceItemEntity)call.getArgument(0)).setId(20);return null;}).when(mapper).insertItem(any());
        var created=service.publishItem(publish(),"session");assertEquals(20,created.getId());assertEquals("synthetic-owner",created.getUsername());assertEquals(new BigDecimal("12.30"),created.getPrice());assertNotNull(created.getPublishTime());assertEquals("合成商品",created.getName());
        service.publishItem(publish(),"session",null,new String[]{"synthetic/temp-a","synthetic/temp-b"});verify(uploads).moveUpload("session","synthetic/temp-a","ershou/20_1.jpg");verify(uploads).moveUpload("session","synthetic/temp-b","ershou/20_2.jpg");
        service.publishItem(publish(),"session",new org.springframework.web.multipart.MultipartFile[]{null,new MockMultipartFile("empty",new byte[0]),new MockMultipartFile("image",new byte[]{1})},null);
        verify(assets).uploadObject(eq("ershou/20_1.jpg"),any());
    }
    @Test void ownerMayEditActiveItemButSoldForeignAndAbsentItemsNeverWrite() throws Exception {
        var active=item(20,"synthetic-owner",1);detail(active);assertTrue(service.ownedByCurrentUser("session",active));assertFalse(service.ownedByCurrentUser(null,active));assertFalse(service.ownedByCurrentUser("absent",active));
        service.verifyEditAccess("session",20);service.updateItem("session",publish(),20);
        verify(mapper).updateItem(argThat(value->value.getId()==20&&"合成商品".equals(value.getName())&&new BigDecimal("12.30").equals(value.getPrice())));
        active.setState(2);assertThrows(ConfirmedStateException.class,()->service.verifyEditAccess("session",20));assertThrows(ConfirmedStateException.class,()->service.updateItem("session",publish(),20));
        active.setState(1);active.setUsername("synthetic-other");assertThrows(NoAccessException.class,()->service.verifyEditAccess("session",20));assertThrows(NoAccessException.class,()->service.updateItem("session",publish(),20));
        assertFalse(service.ownedByCurrentUser("session",active));assertThrows(DataNotExistException.class,()->service.verifyEditAccess("session",404));assertThrows(DataNotExistException.class,()->service.updateItem("session",publish(),404));
        verify(mapper,times(1)).updateItem(any());
    }
    @Test void ownerPagesKeepOwnerIdentityWhilePublicPagesBatchAuthors() throws Exception {
        when(authors.resolveAll(anyCollection())).thenReturn(Map.of("synthetic-owner",new PublicAuthorResolver.AuthorPublic("synthetic-public-id","合成昵称")));
        when(assets.generatePresignedUrl("ershou/20_1.jpg",30,TimeUnit.MINUTES)).thenReturn("https://synthetic.invalid/image");
        when(mapper.selectItemsByUsername("synthetic-owner",5,10)).thenReturn(List.of(item(20,"synthetic-owner",1)));
        var personal=service.queryPersonalItems("session",5,10);assertEquals("synthetic-owner",personal.get(0).getUsername());assertEquals(List.of("https://synthetic.invalid/image"),personal.get(0).getPictureURL());assertEquals("synthetic-public-id",personal.get(0).getAuthorId());
        when(mapper.selectItemsWithKeyword(0,10,"keyword")).thenReturn(List.of(item(21,"synthetic-owner",1)));assertEquals("合成昵称",service.queryItemsWithKeyword("keyword",0).get(0).getDisplayName());
        when(mapper.selectItemsByType(0,10,2)).thenReturn(List.of(item(22,"synthetic-owner",1)));assertEquals("synthetic-public-id",service.queryItemsByType(2,0).get(0).getAuthorId());
        assertTrue(service.queryPersonalItems("session",100,10).isEmpty());assertTrue(service.queryItemsByType(3,100).isEmpty());assertTrue(service.queryItemsWithKeyword("absent",100).isEmpty());
    }
}
