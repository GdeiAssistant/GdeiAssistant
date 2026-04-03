package cn.gdeiassistant.core.data.mapper;

import org.apache.ibatis.annotations.ResultMap;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class AppDataMapperTest {

    @Test
    void selectUserErshouItemListShouldReferenceMarketplaceResultMap() throws NoSuchMethodException {
        Method method = AppDataMapper.class.getMethod("selectUserErshouItemList", String.class);
        ResultMap resultMap = method.getAnnotation(ResultMap.class);

        assertArrayEquals(
                new String[]{"cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper.MarketplaceItemEntity"},
                resultMap.value()
        );
    }
}
