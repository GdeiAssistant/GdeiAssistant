package cn.gdeiassistant.common.tools.utils;
import cn.gdeiassistant.common.i18n.Messages;

public class PageUtils {

    public static final int MAX_PAGE_SIZE = 50;

    private PageUtils() {
    }

    public static int normalizePageSize(int start, int size) {
        requireNonNegativeStart(start);
        if (size <= 0) {
            throw new IllegalArgumentException(Messages.get("request.invalid"));
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    public static int normalizePageSize(Integer start, Integer size) {
        if (start == null || size == null) {
            throw new IllegalArgumentException(Messages.get("request.invalid"));
        }
        return normalizePageSize(start.intValue(), size.intValue());
    }

    public static int nextStart(int start, int size) {
        requireNonNegativeStart(start);
        if (size <= 0) throw new IllegalArgumentException(Messages.get("request.invalid"));
        try { return Math.addExact(start, size); }
        catch (ArithmeticException overflow) { throw new IllegalArgumentException(Messages.get("request.invalid"), overflow); }
    }

    public static int requireNonNegativeStart(int start) {
        if (start < 0) {
            throw new IllegalArgumentException(Messages.get("request.invalid"));
        }
        return start;
    }

    public static int requireNonNegativeStart(Integer start) {
        if (start == null) {
            throw new IllegalArgumentException(Messages.get("request.invalid"));
        }
        return requireNonNegativeStart(start.intValue());
    }
}
