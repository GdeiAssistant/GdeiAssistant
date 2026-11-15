package cn.gdeiassistant.core.information.service.announcement;

import cn.gdeiassistant.common.pojo.entity.Announcement;
import cn.gdeiassistant.common.pojo.entity.AnnouncementEntity;
import cn.gdeiassistant.common.spi.AnnouncementStore;
import cn.gdeiassistant.core.information.converter.AnnouncementConverter;
import cn.gdeiassistant.core.information.pojo.vo.AnnouncementVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementStore announcementStore;

    @Mock
    private AnnouncementConverter announcementConverter;

    private AnnouncementService announcementService;

    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(announcementStore, announcementConverter);
    }

    @Test
    void saveAnnouncementMapsTitleContentAndTimestamp() {
        Announcement request = new Announcement();
        request.setTitle("系统通知");
        request.setContent("服务升级公告");

        announcementService.saveAnnouncement(request);

        ArgumentCaptor<AnnouncementEntity> captor = ArgumentCaptor.forClass(AnnouncementEntity.class);
        verify(announcementStore).insertAnnouncement(captor.capture());
        assertEquals("系统通知", captor.getValue().getTitle());
        assertEquals("服务升级公告", captor.getValue().getContent());
        assertNotNull(captor.getValue().getPublishTime());
    }

    @Test
    void queryMethodsHandleMissingAndInvalidInputs() {
        assertNull(announcementService.queryAnnouncementDetail(null));
        assertTrue(announcementService.queryAnnouncementPage(-1, 10).isEmpty());
        assertTrue(announcementService.queryAnnouncementPage(0, 0).isEmpty());
        verifyNoInteractions(announcementStore);

        AnnouncementEntity entity = new AnnouncementEntity();
        entity.setId("1");
        AnnouncementVO vo = new AnnouncementVO();
        vo.setId("1");
        when(announcementStore.queryLatestAnnouncement()).thenReturn(entity);
        when(announcementConverter.toVO(entity)).thenReturn(vo);
        when(announcementStore.queryAnnouncementPage(0, 5)).thenReturn(List.of(entity));
        when(announcementConverter.toVOList(List.of(entity))).thenReturn(List.of(vo));

        assertSame(vo, announcementService.queryLatestAnnouncement());
        assertEquals(List.of(vo), announcementService.queryAnnouncementPage(0, 5));
    }
}
