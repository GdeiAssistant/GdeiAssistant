package cn.gdeiassistant.contract;

import cn.gdeiassistant.core.announcement.controller.AnnouncementController;
import cn.gdeiassistant.core.announcement.controller.AnnouncementReadController;
import cn.gdeiassistant.core.dating.controller.DatingController;
import cn.gdeiassistant.core.delivery.controller.DeliveryController;
import cn.gdeiassistant.core.express.controller.ExpressController;
import cn.gdeiassistant.core.lostandfound.controller.LostAndFoundController;
import cn.gdeiassistant.core.marketplace.controller.MarketplaceController;
import cn.gdeiassistant.core.message.controller.MessageController;
import cn.gdeiassistant.core.photograph.controller.PhotographController;
import cn.gdeiassistant.core.schoolnews.controller.SchoolNewsController;
import cn.gdeiassistant.core.secret.controller.SecretController;
import cn.gdeiassistant.core.topic.controller.TopicController;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiPathNormalizationContractTest {

    private static final Class<?>[] CONTROLLERS = {
            AnnouncementController.class,
            AnnouncementReadController.class,
            DatingController.class,
            DeliveryController.class,
            ExpressController.class,
            LostAndFoundController.class,
            MarketplaceController.class,
            MessageController.class,
            PhotographController.class,
            SchoolNewsController.class,
            SecretController.class,
            TopicController.class
    };

    @Test
    void legacyIdPathsKeepACanonicalAlias() {
        List<String> missing = new ArrayList<>();
        for (Class<?> controller : CONTROLLERS) {
            for (Method method : controller.getDeclaredMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null) continue;
                List<String> paths = List.of(mapping.value());
                for (String path : paths) {
                    if (!path.contains("/id/{")) continue;
                    String canonical = path.replace("/id/{", "/{");
                    if (!paths.contains(canonical)) {
                        missing.add(controller.getSimpleName() + "#" + method.getName() + " -> " + path);
                    }
                }
            }
        }
        assertTrue(missing.isEmpty(), "Legacy id paths without canonical alias: " + missing);
    }
}
