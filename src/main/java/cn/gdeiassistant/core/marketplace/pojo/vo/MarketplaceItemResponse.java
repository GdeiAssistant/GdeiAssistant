package cn.gdeiassistant.core.marketplace.pojo.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/** Public API projection; persistence usernames never cross this boundary. */
public record MarketplaceItemResponse(Integer id, String authorId, String displayName,
        String name, String description, BigDecimal price, String location, Integer type,
        String qq, String phone, Integer state, Date publishTime, List<String> pictureURL) {}
