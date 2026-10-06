package cn.gdeiassistant.common.migration;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ArchitectureCacheUpgradeTest {
    @Test void migratesNestedTypeMetadataWithoutChangingPayloadOrClassNames() {
        var item = new Document("_class", "cn.gdeiassistant.common.pojo.Document.GradeDocument")
                .append("username", "synthetic")
                .append("gradeList", List.of(new Document("_class", "cn.gdeiassistant.common.pojo.Entity.Grade")
                        .append("gradeName", "Synthetic course")))
                .append("outside", new Document("_class", "java.lang.String"));
        var original = item.toJson();
        assertEquals(new Document("_class", "cn.gdeiassistant.common.pojo.document.GradeDocument")
                .append("gradeList.0._class", "cn.gdeiassistant.common.pojo.entity.Grade"),
                ArchitectureCacheUpgrade.metadataUpdates(item));
        assertEquals(original, item.toJson());
        assertTrue(ArchitectureCacheUpgrade.metadataUpdates(
                new Document("_class", "cn.gdeiassistant.common.pojo.document.GradeDocument")).isEmpty());
    }
}
