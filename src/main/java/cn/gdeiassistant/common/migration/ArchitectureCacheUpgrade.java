package cn.gdeiassistant.common.migration;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.util.*;

/** Preserve cache contents while migrating Java type metadata after package normalization. */
public final class ArchitectureCacheUpgrade {
    private ArchitectureCacheUpgrade() {}

    public static void migrate(MongoTemplate mongo) {
        for (String name : List.of("grade", "schedule")) {
            var duplicates = mongo.getCollection(name).aggregate(List.of(
                    new Document("$group", new Document("_id", "$username").append("count", new Document("$sum", 1))),
                    new Document("$match", new Document("count", new Document("$gt", 1))),
                    new Document("$limit", 1))).first();
            if (duplicates != null) throw new IllegalStateException("Resolve duplicate cache owners before migration: " + name);
        }
        for (String name : List.of("grade", "schedule")) {
            try (var cursor = mongo.getCollection(name).find().iterator()) {
                while (cursor.hasNext()) {
                    Document item = cursor.next();
                    Document updates = metadataUpdates(item);
                    if (!updates.isEmpty()) mongo.getCollection(name).updateOne(
                            new Document("_id", item.get("_id")), new Document("$set", updates));
                }
            }
        }
    }

    static Document metadataUpdates(Document item) {
        Document updates = new Document();
        collect(item, "", updates);
        return updates;
    }

    private static void collect(Object value, String path, Document updates) {
        if (value instanceof Map<?, ?> map) {
            for (var entry : map.entrySet()) {
                String next = path.isEmpty() ? String.valueOf(entry.getKey()) : path + "." + entry.getKey();
                Object field = entry.getValue();
                if ("_class".equals(entry.getKey()) && field instanceof String name && name.startsWith("cn.gdeiassistant.")) {
                    int split = name.lastIndexOf('.');
                    String normalized = name.substring(0, split).toLowerCase(Locale.ROOT) + name.substring(split);
                    if (!name.equals(normalized)) updates.put(next, normalized);
                } else collect(field, next, updates);
            }
        } else if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) collect(list.get(i), path + "." + i, updates);
        }
    }
}
