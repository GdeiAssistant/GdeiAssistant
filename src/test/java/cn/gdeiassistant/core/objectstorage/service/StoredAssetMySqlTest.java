package cn.gdeiassistant.core.objectstorage.service;

import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.objectstorage.mapper.StoredAssetMapper;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.service.TopicService;
import cn.gdeiassistant.core.topic.pojo.dto.TopicPublishDTO;
import cn.gdeiassistant.core.topic.converter.TopicConverter;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.pojo.entity.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.interceptor.*;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@EnabledIfEnvironmentVariable(named="GDEI_UPGRADE_TEST_JDBC_BASE",matches=".+")
class StoredAssetMySqlTest {
    String schema; JdbcTemplate server,sql; StoredAssetMapper mapper; StoredAssetService assets;
    R2StorageService storage; DataSourceTransactionManager transactions; SqlSessionTemplate sessions;
    @BeforeEach void setup() throws Exception {
        String base=System.getenv("GDEI_UPGRADE_TEST_JDBC_BASE"), password=System.getenv("GDEI_UPGRADE_TEST_PASSWORD");
        server=new JdbcTemplate(new DriverManagerDataSource(base+"mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true","root",password));
        schema="synthetic_assets_"+UUID.randomUUID().toString().replace("-","");server.execute("CREATE DATABASE "+schema);
        var source=new DriverManagerDataSource(base+schema+"?sslMode=DISABLED&allowPublicKeyRetrieval=true","root",password);
        sql=new JdbcTemplate(source);transactions=new DataSourceTransactionManager(source);
        sql.execute("CREATE TABLE stored_asset(bucket varchar(128) NOT NULL,object_key varchar(256) NOT NULL,owner_id varchar(64),status varchar(16) NOT NULL,content_type varchar(100),byte_length bigint NOT NULL DEFAULT 0,updated_at datetime NOT NULL,PRIMARY KEY(bucket,object_key),KEY idx_stored_asset_cleanup(status,updated_at)) ENGINE=InnoDB");
        sql.execute("CREATE TABLE topic(id int auto_increment primary key,username varchar(24),topic varchar(15),content varchar(250),count tinyint,publish_time datetime) ENGINE=InnoDB");
        var factory=new SqlSessionFactoryBean();factory.setDataSource(source);var sessionFactory=factory.getObject();
        sessionFactory.getConfiguration().addMapper(StoredAssetMapper.class);sessionFactory.getConfiguration().addMapper(TopicMapper.class);
        sessions=new SqlSessionTemplate(sessionFactory);mapper=sessions.getMapper(StoredAssetMapper.class);
        storage=mock(R2StorageService.class);when(storage.getBucketName()).thenReturn("synthetic");when(storage.isEnabled()).thenReturn(true);
        assets=new StoredAssetService(storage,mapper,transactions);
    }
    @AfterEach void close() { if(server!=null&&schema!=null)server.execute("DROP DATABASE "+schema); }
    byte[] png() throws Exception {
        var bytes=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2,2,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",bytes);return bytes.toByteArray();
    }
    @Test void messagePublicIdentityProjectionHasNoCredentialTableDependency() {
        sql.execute("CREATE TABLE app_user(id bigint primary key,public_id varchar(36),status varchar(16)) ENGINE=InnoDB");
        sql.execute("INSERT INTO app_user VALUES(1,'synthetic-active','ACTIVE'),(2,'synthetic-closed','CLOSED')");
        sessions.getConfiguration().addMapper(cn.gdeiassistant.core.social.mapper.SocialUserSummaryMapper.class);
        var rows=sessions.getMapper(cn.gdeiassistant.core.social.mapper.SocialUserSummaryMapper.class).selectPublicIds(List.of(1L,2L,999L));
        assertEquals(2,rows.size());
        for(var row:rows) assertEquals(Set.of("userId","publicId"),row.keySet());
        assertEquals(Set.of("synthetic-active","synthetic-closed"),rows.stream().map(row->row.get("publicId")).collect(java.util.stream.Collectors.toSet()));
    }

    @Test void failedPublicationRollsBackBusinessRowButLeavesDurableCleanup() throws Exception {
        var target=new TopicService();var certificates=mock(UserCertificateService.class);when(certificates.getUserLoginCertificate("session")).thenReturn(new User("synthetic"));
        var authors=mock(PublicAuthorResolver.class);when(authors.resolve("synthetic")).thenReturn(new PublicAuthorResolver.AuthorPublic(null,"用户"));
        ReflectionTestUtils.setField(target,"userCertificateService",certificates);ReflectionTestUtils.setField(target,"topicMapper",sessions.getMapper(TopicMapper.class));
        ReflectionTestUtils.setField(target,"topicConverter",org.mapstruct.factory.Mappers.getMapper(TopicConverter.class));ReflectionTestUtils.setField(target,"publicAuthors",authors);ReflectionTestUtils.setField(target,"storedAssets",assets);
        var source=new AnnotationTransactionAttributeSource();var proxy=new ProxyFactory(target);proxy.addAdvice(new TransactionInterceptor(transactions,source));
        TopicService service=(TopicService)proxy.getProxy();var dto=new TopicPublishDTO();dto.setTopic("synthetic");dto.setContent("synthetic");dto.setCount(1);
        doThrow(new IllegalStateException("synthetic storage outage")).when(storage).uploadBytesStrict(isNull(),anyString(),any(byte[].class),eq("image/png"));
        assertThrows(RuntimeException.class,()->service.publishTopic(dto,"session",new MockMultipartFile[]{new MockMultipartFile("image","sample.png","image/png",png())},null));
        assertEquals(0,sql.queryForObject("SELECT COUNT(*) FROM topic",Integer.class));
        assertEquals("PENDING",sql.queryForObject("SELECT status FROM stored_asset",String.class));
        sql.update("UPDATE stored_asset SET updated_at=DATE_SUB(NOW(),INTERVAL 21 MINUTE)");assets.cleanAbandonedUploads();
        verify(storage).deleteObjectStrict("synthetic","topic/1_1.jpg");assertEquals(0,sql.queryForObject("SELECT COUNT(*) FROM stored_asset",Integer.class));
    }
    @Test void sqlFailureAfterObjectUploadKeepsPendingAndDoesNotAdvertiseUrl() throws Exception {
        var business=new TransactionTemplate(transactions);
        assertThrows(IllegalStateException.class,()->business.executeWithoutResult(status->{assets.uploadObject("topic/2_1.jpg",new java.io.ByteArrayInputStream(uncheckedPng()));throw new IllegalStateException("synthetic SQL failure");}));
        assertEquals("PENDING",mapper.find("synthetic","topic/2_1.jpg").get("status"));
        assertEquals("",assets.generatePresignedUrl("topic/2_1.jpg",30,TimeUnit.MINUTES));
        verify(storage,never()).generateKnownObjectUrl(any(),any(),anyLong(),any());
    }
    byte[] uncheckedPng(){try{return png();}catch(Exception e){throw new RuntimeException(e);}}
    @Test void failedDeleteRetainsRetryAndSuccessfulRetryRemovesMetadata() {
        assets.pending("old",null,"image/png",50);assets.ready("old");
        doThrow(new IllegalStateException("synthetic outage")).doNothing().when(storage).deleteObjectStrict(null,"old");
        assertThrows(IllegalStateException.class,()->assets.deleteObject("old"));assertEquals("DELETING",mapper.find("synthetic","old").get("status"));
        assets.deleteObject("old");assertNull(mapper.find("synthetic","old"));verify(storage,times(2)).deleteObjectStrict(null,"old");
    }
    @Test void readyUrlsHaveNoHeadAndCleanupRechecksConcurrentRenewal() {
        assets.pending("ready",null,"image/png",40);assets.ready("ready");
        when(storage.generateKnownObjectUrl(null,"ready",30,TimeUnit.MINUTES)).thenReturn("https://synthetic.invalid/signed");
        assertEquals("https://synthetic.invalid/signed",assets.generatePresignedUrl("ready",30,TimeUnit.MINUTES));verify(storage,never()).headObjectMetadata(any());
        assets.pending("renewed",null,"image/png",50);sql.update("UPDATE stored_asset SET updated_at=DATE_SUB(NOW(),INTERVAL 21 MINUTE) WHERE object_key='renewed'");
        assertEquals(1,mapper.cleanupCandidates().size());assets.pending("renewed",null,"image/png",60);assets.cleanAbandonedUploads();
        verify(storage,never()).deleteObjectStrict(any(),any());assertEquals(60L,((Number)mapper.find("synthetic","renewed").get("byte_length")).longValue());
    }
    @Test void readyAudioLookupUsesMetadataAndDoesNotProbeOtherSuffixes() {
        assets.pending("secret/voice/1.webm",null,"audio/webm",100);assets.ready("secret/voice/1.webm");
        assets.pending("secret/voice/1.mp3",null,"audio/mpeg",100);
        assertEquals("secret/voice/1.webm",assets.firstReadyKey(new String[]{"secret/voice/1.mp3","secret/voice/1.webm"}));
        verify(storage,never()).headObjectMetadata(any());
    }
    @Test void invalidImageCannotPublishAndMissingObjectsAreNegativeCached() {
        byte[] fake=new byte[20];fake[0]=(byte)137;fake[1]='P';fake[2]='N';fake[3]='G';
        assertThrows(IllegalArgumentException.class,()->assets.uploadObject("topic/invalid.jpg",new java.io.ByteArrayInputStream(fake)));
        assertEquals(0,sql.queryForObject("SELECT COUNT(*) FROM stored_asset",Integer.class));
        assertEquals("",assets.generatePresignedUrl("absent",30,TimeUnit.MINUTES));assertEquals("",assets.generatePresignedUrl("absent",30,TimeUnit.MINUTES));
        verify(storage,times(1)).headObjectMetadata("absent");
    }
    @Test void socialBatchProjectionPreservesPrivacyBlocksAndClosedAccountSemantics() {
        sql.execute("CREATE TABLE app_user(id bigint primary key,public_id varchar(36),status varchar(16))");
        sql.execute("CREATE TABLE campus_credential(user_id bigint primary key,campus_username varchar(24))");
        sql.execute("CREATE TABLE profile(username varchar(24) primary key,nickname varchar(30))");
        sql.execute("CREATE TABLE privacy(username varchar(24) primary key,is_introduction_open boolean,dm_policy varchar(16))");
        sql.execute("CREATE TABLE introduction(username varchar(24) primary key,introduction varchar(80))");
        sql.execute("CREATE TABLE user_follow(follower_id bigint,followee_id bigint,PRIMARY KEY(follower_id,followee_id))");
        sql.execute("CREATE TABLE user_block(blocker_id bigint,blocked_id bigint,PRIMARY KEY(blocker_id,blocked_id))");
        sql.update("INSERT INTO app_user VALUES (1,'viewer','ACTIVE'),(2,'peer-a','ACTIVE'),(3,'peer-b','ACTIVE'),(4,'closed','CLOSED')");
        sql.update("INSERT INTO campus_credential VALUES(2,'synthetic-a'),(3,'synthetic-b')");
        sql.update("INSERT INTO profile VALUES('synthetic-a','A'),('synthetic-b','B')");
        sql.update("INSERT INTO privacy VALUES('synthetic-a',true,' all '),('synthetic-b',false,'NONE')");
        sql.update("INSERT INTO introduction VALUES('synthetic-a','visible'),('synthetic-b','private')");
        sql.update("INSERT INTO user_follow VALUES(1,2),(2,1),(4,2),(2,4)");sql.update("INSERT INTO user_block VALUES(1,3)");
        sessions.getConfiguration().addMapper(cn.gdeiassistant.core.social.mapper.SocialUserSummaryMapper.class);
        var identity=new cn.gdeiassistant.core.social.service.SocialIdentityService();
        ReflectionTestUtils.setField(identity,"summaries",sessions.getMapper(cn.gdeiassistant.core.social.mapper.SocialUserSummaryMapper.class));
        var viewer=new cn.gdeiassistant.core.user.pojo.entity.CampusAccountView();viewer.setId(1L);viewer.setStatus("ACTIVE");
        var peers=identity.buildSocialUsers(viewer,List.of(2L,3L,4L));
        assertTrue(peers.get(2L).isCanMessage());assertEquals("visible",peers.get(2L).getIntroduction());
        assertEquals(1,peers.get(2L).getFollowingCount());assertEquals(1,peers.get(2L).getFollowerCount());assertEquals(1,peers.get(2L).getFriendCount());
        assertFalse(peers.get(3L).isCanMessage());assertTrue(peers.get(3L).isBlockedByMe());assertNull(peers.get(3L).getIntroduction());
        assertEquals("CONTACT_UNAVAILABLE",peers.get(3L).getMessagePermissionReason());
        assertEquals("已注销",peers.get(4L).getNickname());assertFalse(peers.get(4L).isCanMessage());
        sql.update("INSERT INTO user_block VALUES(2,1)");assertFalse(identity.buildSocialUsers(viewer,List.of(2L)).get(2L).isCanMessage());
    }
}
