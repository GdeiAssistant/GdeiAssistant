package cn.gdeiassistant.common.account;

import cn.gdeiassistant.core.delivery.service.DeliveryAccountContributor;
import cn.gdeiassistant.core.express.service.ExpressAccountContributor;
import cn.gdeiassistant.core.lostandfound.service.LostAndFoundAccountContributor;
import cn.gdeiassistant.core.marketplace.service.MarketplaceAccountContributor;
import cn.gdeiassistant.core.photograph.service.PhotographAccountContributor;
import cn.gdeiassistant.core.profile.service.ProfileAccountContributor;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.secret.service.SecretAccountContributor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Each domain must interpret its own exported status codes, not the export orchestrator. */
class AccountDataExportDecorationTest {

    private static final List<AccountLifecycleContributor> CONTRIBUTORS = List.of(
            new DeliveryAccountContributor(null),
            new MarketplaceAccountContributor(null),
            new LostAndFoundAccountContributor(null),
            new SecretAccountContributor(null),
            new PhotographAccountContributor(null),
            new ExpressAccountContributor(null),
            new ProfileAccountContributor(null, null));

    @AfterEach
    void clearDictionaries() {
        UserProfileService dictionaries = new UserProfileService();
        dictionaries.setDegreeMap(null);
        dictionaries.setFacultyMap(null);
        dictionaries.setProfessionMap(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstRow(Map<String, Object> data, String section) {
        return (Map<String, Object>) ((List<?>) data.get(section)).get(0);
    }

    @Test
    void everyDomainLabelsItsOwnSection() {
        Map<String, Object> data = new HashMap<>();
        data.put("deliveryOrders", List.of(new HashMap<>(Map.of("state", 0))));
        data.put("deliveryTrades", List.of(new HashMap<>(Map.of("state", 1))));
        data.put("ershouItems", List.of(new HashMap<>(Map.of("type", 1, "state", 2))));
        data.put("lostandfoundItems", List.of(new HashMap<>(Map.of("itemType", 0, "lostType", 1, "state", 0))));
        data.put("secretItems", List.of(new HashMap<>(Map.of("state", 0, "type", 1))));
        data.put("photographItems", List.of(new HashMap<>(Map.of("type", 1))));
        data.put("expressItems", List.of(new HashMap<>(Map.of("selfGender", 0, "personGender", 1))));
        data.put("profile", new HashMap<>(Map.of("degree", 0)));

        UserProfileService dictionaries = new UserProfileService();
        dictionaries.setDegreeMap(Map.of(0, "Undergraduate"));
        dictionaries.setFacultyMap(Map.of());
        dictionaries.setProfessionMap(Map.of());

        AccountDataExport export = new AccountDataExport(data);
        CONTRIBUTORS.forEach(contributor -> contributor.decorateExport(export));

        assertEquals("未被接单", firstRow(data, "deliveryOrders").get("state"));
        assertEquals("已完成", firstRow(data, "deliveryTrades").get("state"));
        assertEquals("手机", firstRow(data, "ershouItems").get("type"));
        assertEquals("已出售", firstRow(data, "ershouItems").get("state"));
        assertEquals("手机", firstRow(data, "lostandfoundItems").get("itemType"));
        assertEquals("寻物", firstRow(data, "lostandfoundItems").get("lostType"));
        assertEquals("寻主/寻物中", firstRow(data, "lostandfoundItems").get("state"));
        assertEquals("语音", firstRow(data, "secretItems").get("type"));
        assertEquals("校园照", firstRow(data, "photographItems").get("type"));
        assertEquals("男", firstRow(data, "expressItems").get("selfGender"));
        assertEquals("女", firstRow(data, "expressItems").get("personGender"));
        assertEquals("Undergraduate", ((Map<?, ?>) data.get("profile")).get("degree"));
    }
}
