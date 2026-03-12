package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;

import java.util.Comparator;
import java.util.Date;

public class InteractionMessageRecord {

    public static final Comparator<InteractionMessageRecord> DEFAULT_COMPARATOR = Comparator
            .comparing(InteractionMessageRecord::getCreateTime, Comparator.nullsLast(Date::compareTo))
            .reversed()
            .thenComparing(InteractionMessageRecord::getSortId, Comparator.reverseOrder())
            .thenComparing(record -> record.getMessage().getModule(), Comparator.nullsLast(String::compareTo));

    private final InteractionMessageVO message;
    private final Date createTime;
    private final Long sortId;

    public InteractionMessageRecord(InteractionMessageVO message, Date createTime, Long sortId) {
        this.message = message;
        this.createTime = createTime;
        this.sortId = sortId;
    }

    public InteractionMessageVO getMessage() {
        return message;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Long getSortId() {
        return sortId;
    }
}
