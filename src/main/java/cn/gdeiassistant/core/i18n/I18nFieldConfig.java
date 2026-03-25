package cn.gdeiassistant.core.i18n;

import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class I18nFieldConfig {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final Map<String, List<String>> PATH_FIELD_MAP = new LinkedHashMap<>() {{
        put("/api/information/overview", List.of(
                "data.notice.title", "data.notice.content",
                "data.notices[].title", "data.notices[].content",
                "data.festival.name", "data.festival.description[]"
        ));
        put("/api/information/news/**", List.of("data.title", "data.content", "data[].title", "data[].content"));
        put("/api/information/announcement/**", List.of("data.title", "data.content", "data[].title", "data[].content"));
        put("/api/schedule", List.of(
                "data.scheduleList[].scheduleName",
                "data.scheduleList[].scheduleTeacher",
                "data.scheduleList[].scheduleLocation"
        ));
        put("/api/card/query", List.of(
                "data.cardInfo.cardLostState",
                "data.cardInfo.cardFreezeState",
                "data.cardList[].merchantName",
                "data.cardList[].tradeName"
        ));
        put("/api/card/info", List.of(
                "data.cardLostState",
                "data.cardFreezeState"
        ));
        put("/api/data/electricfees", List.of(
                "data.buildingNumber",
                "data.department",
                "data.name"
        ));
        put("/api/data/yellowpage", List.of(
                "data.type[].typeName",
                "data.data[].typeName",
                "data.data[].section",
                "data.data[].campus",
                "data.data[].address"
        ));
        put("/api/ershou/**", List.of("data.name", "data.description", "data[].name", "data[].description"));
        put("/api/lostandfound/**", List.of("data.name", "data.description", "data[].name", "data[].description"));
        put("/api/express/**", List.of("data.nickname", "data.name", "data.content", "data[].nickname", "data[].name", "data[].content"));
        put("/api/secret/**", List.of("data.content", "data[].content"));
        put("/api/topic/**", List.of("data.topic", "data.content", "data[].topic", "data[].content"));
        put("/api/photograph/**", List.of("data.title", "data.content", "data[].title", "data[].content"));
        put("/api/delivery/**", List.of("data.remarks", "data[].remarks"));
        put("/api/dating/**", List.of("data.nickname", "data.content", "data[].nickname", "data[].content"));
    }};

    public static List<String> getFieldsForPath(String requestPath) {
        for (Map.Entry<String, List<String>> entry : PATH_FIELD_MAP.entrySet()) {
            if (MATCHER.match(entry.getKey(), requestPath)) {
                return entry.getValue();
            }
        }
        return List.of();
    }

    public static boolean isTranslatablePath(String requestPath) {
        return PATH_FIELD_MAP.keySet().stream().anyMatch(p -> MATCHER.match(p, requestPath));
    }
}
