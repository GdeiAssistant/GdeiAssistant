package cn.gdeiassistant.common.tools.Utils;

public class PageUtils {

    public static final int MAX_PAGE_SIZE = 50;

    private PageUtils() {
    }

    public static int normalizePageSize(int start, int size) {
        requireNonNegativeStart(start);
        if (size <= 0) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    public static int normalizePageSize(Integer start, Integer size) {
        if (start == null || size == null) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return normalizePageSize(start.intValue(), size.intValue());
    }

    public static int requireNonNegativeStart(int start) {
        if (start < 0) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return start;
    }

    public static int requireNonNegativeStart(Integer start) {
        if (start == null) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return requireNonNegativeStart(start.intValue());
    }
}
