package cn.gdeiassistant.common.tools.Utils;

public class PageUtils {

    public static final int MAX_PAGE_SIZE = 50;

    private PageUtils() {
    }

    public static int normalizePageSize(int start, int size) {
        if (start < 0 || size <= 0) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
