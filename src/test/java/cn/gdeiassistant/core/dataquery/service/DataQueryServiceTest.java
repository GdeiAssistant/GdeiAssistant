package cn.gdeiassistant.core.dataquery.service;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.pojo.entity.YellowPage;
import cn.gdeiassistant.core.dataquery.pojo.YellowPageType;
import cn.gdeiassistant.core.electricfees.mapper.ElectricFeesMapper;
import cn.gdeiassistant.core.yellowpage.pojo.entity.YellowPageCategory;
import cn.gdeiassistant.core.yellowpage.service.YellowPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataQueryServiceTest {

    @Mock
    private ElectricFeesMapper electricFeesMapper;

    @Mock
    private YellowPageService yellowPageService;

    private DataQueryService dataQueryService;

    @BeforeEach
    void setUp() {
        dataQueryService = new DataQueryService(electricFeesMapper, yellowPageService);
    }

    @Test
    void queryYellowPageTypeMapsCategoriesToYellowPageType() throws DataNotExistException {
        YellowPageCategory first = new YellowPageCategory();
        first.setTypeCode(1);
        first.setTypeName("教务部门");
        YellowPageCategory second = new YellowPageCategory();
        second.setTypeCode(2);
        second.setTypeName("后勤服务");
        when(yellowPageService.selectYellowPageCategories()).thenReturn(List.of(first, second));

        List<YellowPageType> result = dataQueryService.queryYellowPageType();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getTypeCode());
        assertEquals("教务部门", result.get(0).getTypeName());
        assertEquals(2, result.get(1).getTypeCode());
        assertEquals("后勤服务", result.get(1).getTypeName());
    }

    @Test
    void queryYellowPageTypeThrowsWhenEmpty() {
        when(yellowPageService.selectYellowPageCategories()).thenReturn(List.of());

        assertThrows(DataNotExistException.class, () -> dataQueryService.queryYellowPageType());
    }

    @Test
    void queryAndInsertYellowPageDelegateToYellowPageService() throws DataNotExistException {
        YellowPage item = new YellowPage();
        item.setId(10);
        when(yellowPageService.selectAllYellowPageList()).thenReturn(List.of(item));

        List<YellowPage> list = dataQueryService.queryYellowPageList();
        assertEquals(1, list.size());
        assertSame(item, list.get(0));

        dataQueryService.insertYellowPageBatch(List.of(item));
        verify(yellowPageService).insertYellowPageBatch(List.of(item));
    }
}
