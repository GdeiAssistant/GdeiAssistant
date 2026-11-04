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
    void legacyIdPathsAreNotMapped() {
        List<String> legacy = new ArrayList<>();
        for (Class<?> controller : CONTROLLERS) {
            for (Method method : controller.getDeclaredMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (mapping == null) continue;
                for (String path : mapping.value()) {
                    if (path.contains("/id/{")) {
                        legacy.add(controller.getSimpleName() + "#" + method.getName() + " -> " + path);
                    }
                }
            }
        }
        assertTrue(legacy.isEmpty(), "Legacy id paths must not be mapped: " + legacy);
    }
}
