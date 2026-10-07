package cn.gdeiassistant.common.tools.utils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PaginationOverflowTest {
    @Test void nextPageNeverWrapsToANegativeOffset() {
        assertEquals(25,PageUtils.nextStart(0,25));
        assertEquals(Integer.MAX_VALUE,PageUtils.nextStart(Integer.MAX_VALUE-25,25));
        assertThrows(IllegalArgumentException.class,()->PageUtils.nextStart(Integer.MAX_VALUE,25));
        assertThrows(IllegalArgumentException.class,()->PageUtils.nextStart(-1,25));
        assertThrows(IllegalArgumentException.class,()->PageUtils.nextStart(0,0));
    }
}
