package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class BaseInteractionMessageProvider implements InteractionMessageProvider {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    protected InteractionMessageRecord buildRecord(InteractionMessageVO message, Date createTime, Integer sortId) {
        long actualSortId = sortId == null ? Long.MIN_VALUE : sortId.longValue();
        return new InteractionMessageRecord(message, createTime, actualSortId);
    }

    protected String formatDate(Date value) {
        return value == null ? "" : value.toInstant().atZone(ZONE_ID).toLocalDateTime().format(DATETIME_FORMATTER);
    }

    protected String toStringValue(Integer value) {
        return value == null ? null : String.valueOf(value);
    }

    protected String prefixedId(String prefix, Integer value) {
        return value == null ? null : prefix + value;
    }

    protected List<InteractionMessageRecord> limitRecords(List<InteractionMessageRecord> records, int limit) {
        if (records == null || records.isEmpty() || limit <= 0) {
            return new ArrayList<>();
        }
        records.sort(InteractionMessageRecord.DEFAULT_COMPARATOR);
        if (records.size() <= limit) {
            return records;
        }
        return new ArrayList<>(records.subList(0, limit));
    }
}
