package cn.gdeiassistant.common.migration;

import static org.junit.jupiter.api.Assertions.*;

import cn.gdeiassistant.common.config.CacheIndexConfig;
import cn.gdeiassistant.common.pojo.document.*;
import cn.gdeiassistant.common.pojo.entity.Grade;
import cn.gdeiassistant.core.grade.repository.GradeDaoImpl;
import cn.gdeiassistant.core.schedule.repository.ScheduleDaoImpl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.*;

@EnabledIfEnvironmentVariable(named = "GDEI_MONGO_TEST_PORT", matches = "\\d+")
class ArchitectureMongoIntegrationTest {
    MongoClient admin;
    MongoTemplate mongo;
    String db;

    String uri(String user, String password, String auth) {
        return "mongodb://"
                + user
                + ":"
                + password
                + "@127.0.0.1:"
                + System.getenv("GDEI_MONGO_TEST_PORT")
                + "/?authSource="
                + auth;
    }

    @BeforeEach
    void setup() {
        admin = MongoClients.create(uri("synthetic_admin", "synthetic_ci_only", "admin"));
        db = "gdei_test_" + UUID.randomUUID().toString().replace("-", "");
        mongo = new MongoTemplate(admin, db);
    }

    @AfterEach
    void cleanup() {
        mongo.getDb().drop();
        admin.close();
    }

    @Test
    void metadataMigrationPreservesPayloadAndIsRepeatable() {
        Document legacy =
                new Document("username", "owner")
                        .append("_class", "cn.gdeiassistant.common.pojo.Document.GradeDocument")
                        .append(
                                "gradeLists",
                                List.of(
                                        List.of(
                                                new Document(
                                                                "_class",
                                                                "cn.gdeiassistant.common.pojo.Entity.Grade")
                                                        .append("gradeName", "Synthetic course")
                                                        .append("gradeScore", "90"))));
        mongo.getCollection("grade").insertOne(legacy);
        mongo.getCollection("schedule")
                .insertOne(
                        new Document("username", "owner")
                                .append(
                                        "_class",
                                        "cn.gdeiassistant.common.pojo.Document.ScheduleDocument"));
        ArchitectureCacheUpgrade.migrate(mongo);
        Document migrated = mongo.getCollection("grade").find().first();
        assertEquals(
                "cn.gdeiassistant.common.pojo.document.GradeDocument",
                migrated.getString("_class"));
        Document nested =
                (Document) ((List<?>) ((List<?>) migrated.get("gradeLists")).get(0)).get(0);
        assertEquals("cn.gdeiassistant.common.pojo.entity.Grade", nested.getString("_class"));
        assertEquals("90", nested.getString("gradeScore"));
        GradeDocument restored =
                mongo.findOne(
                        new org.springframework.data.mongodb.core.query.Query(
                                org.springframework.data.mongodb.core.query.Criteria.where(
                                                "username")
                                        .is("owner")),
                        GradeDocument.class,
                        "grade");
        assertNotNull(restored);
        assertEquals("Synthetic course", restored.getGradeList().get(0).get(0).getGradeName());
        assertEquals("90", restored.getGradeList().get(0).get(0).getGradeScore());
        ArchitectureCacheUpgrade.migrate(mongo);
        assertEquals(migrated, mongo.getCollection("grade").find().first());
        assertEquals(1, mongo.getCollection("schedule").countDocuments());
    }

    @Test
    void duplicateOwnersAbortBeforeAnyMetadataMutation() {
        mongo.getCollection("grade")
                .insertOne(
                        new Document("username", "owner")
                                .append(
                                        "_class",
                                        "cn.gdeiassistant.common.pojo.Document.GradeDocument"));
        mongo.getCollection("schedule")
                .insertMany(
                        List.of(
                                new Document("username", "duplicate"),
                                new Document("username", "duplicate")));
        assertThrows(IllegalStateException.class, () -> ArchitectureCacheUpgrade.migrate(mongo));
        assertEquals(
                "cn.gdeiassistant.common.pojo.Document.GradeDocument",
                mongo.getCollection("grade").find().first().getString("_class"));
        assertEquals(2, mongo.getCollection("schedule").countDocuments());
    }

    @Test
    void concurrentUpsertsKeepOneDocumentPerOwnerAndPreserveOtherOwners() throws Exception {
        new CacheIndexConfig().createCacheIndexes(mongo);
        GradeDaoImpl grades = new GradeDaoImpl();
        ScheduleDaoImpl schedules = new ScheduleDaoImpl();
        ReflectionTestUtils.setField(grades, "mongoTemplate", mongo);
        ReflectionTestUtils.setField(schedules, "mongoTemplate", mongo);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Void>> calls = new ArrayList<>();
            for (int i = 0; i < 24; i++)
                calls.add(
                        () -> {
                            GradeDocument g = new GradeDocument();
                            g.setUsername("owner");
                            Grade course = new Grade();
                            course.setGradeName("Synthetic course");
                            g.setGradeList(List.of(List.of(course)));
                            grades.saveGrade(g);
                            ScheduleDocument s = new ScheduleDocument();
                            s.setUsername("owner");
                            s.setScheduleList(List.of());
                            schedules.saveSchedule(s);
                            return null;
                        });
            for (Future<Void> f : pool.invokeAll(calls)) f.get();
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, mongo.getCollection("grade").countDocuments());
        assertEquals(1, mongo.getCollection("schedule").countDocuments());
        assertEquals(
                "Synthetic course",
                grades.queryGrade("owner").getGradeList().get(0).get(0).getGradeName());
        assertTrue(schedules.querySchedule("owner").getScheduleList().isEmpty());
        GradeDocument other = new GradeDocument();
        other.setUsername("other");
        grades.saveGrade(other);
        grades.removeGrade("owner");
        schedules.removeSchedule("owner");
        assertNull(grades.queryGrade("owner"));
        assertNull(schedules.querySchedule("owner"));
        assertNotNull(grades.queryGrade("other"));
        assertThrows(
                RuntimeException.class,
                () ->
                        mongo.getCollection("grade")
                                .insertMany(
                                        List.of(
                                                new Document("username", "same"),
                                                new Document("username", "same"))));
    }

    @Test
    void restrictedApplicationRoleNeedsOnlyTwoCollectionIndexPermissions() {
        mongo.createCollection("grade");
        mongo.createCollection("schedule");
        List<Document> privileges = new ArrayList<>();
        for (String c : List.of("grade", "schedule"))
            privileges.add(
                    new Document("resource", new Document("db", db).append("collection", c))
                            .append("actions", List.of("find", "insert", "update", "remove")));
        mongo.getDb()
                .runCommand(
                        new Document("createRole", "runtime")
                                .append("privileges", privileges)
                                .append("roles", List.of()));
        mongo.getDb()
                .runCommand(
                        new Document("createUser", "synthetic_runtime")
                                .append("pwd", "synthetic_runtime_only")
                                .append("roles", List.of("runtime")));
        try (MongoClient client =
                MongoClients.create(uri("synthetic_runtime", "synthetic_runtime_only", db))) {
            MongoTemplate runtime = new MongoTemplate(client, db);
            assertThrows(
                    RuntimeException.class,
                    () -> new CacheIndexConfig().createCacheIndexes(runtime));
            for (Document privilege : privileges)
                privilege.put(
                        "actions", List.of("find", "insert", "update", "remove", "createIndex"));
            mongo.getDb()
                    .runCommand(
                            new Document("updateRole", "runtime").append("privileges", privileges));
            new CacheIndexConfig().createCacheIndexes(runtime);
            new CacheIndexConfig().createCacheIndexes(runtime);
            for (String c : List.of("grade", "schedule"))
                assertTrue(
                        mongo.getCollection(c).listIndexes().into(new ArrayList<>()).stream()
                                .anyMatch(
                                        i ->
                                                "username_1".equals(i.getString("name"))
                                                        && Boolean.TRUE.equals(
                                                                i.getBoolean("unique"))));
            assertThrows(
                    RuntimeException.class,
                    () ->
                            runtime.getDb()
                                    .runCommand(
                                            new Document("createIndexes", "unrelated")
                                                    .append(
                                                            "indexes",
                                                            List.of(
                                                                    new Document(
                                                                                    "key",
                                                                                    new Document(
                                                                                            "username",
                                                                                            1))
                                                                            .append(
                                                                                    "name",
                                                                                    "username_1")))));
        }
    }
}
