package cn.gdeiassistant.common.config.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LegacyJacksonConfigTest {

    @Test
    void providesLegacyObjectMapperBean() throws Exception {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(LegacyJacksonConfig.class)) {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);
            assertNotNull(objectMapper);
            assertEquals("{\"hello\":\"world\"}", objectMapper.writeValueAsString(Map.of("hello", "world")));
        }
    }
}
