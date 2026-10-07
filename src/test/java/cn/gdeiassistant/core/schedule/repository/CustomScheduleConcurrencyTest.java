package cn.gdeiassistant.core.schedule.repository;

import cn.gdeiassistant.common.pojo.entity.CustomSchedule;
import cn.gdeiassistant.common.exception.customscheduleexception.CountOverLimitException;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="GDEI_MONGO_TEST_PORT",matches="\\d+")
class CustomScheduleConcurrencyTest {
    com.mongodb.client.MongoClient client; MongoTemplate mongo; ScheduleDaoImpl a,b;
    @BeforeEach void setup() {
        client=MongoClients.create("mongodb://synthetic_admin:synthetic_ci_only@127.0.0.1:"+System.getenv("GDEI_MONGO_TEST_PORT")+"/?authSource=admin");
        mongo=new MongoTemplate(client,"synthetic_audit_"+java.util.UUID.randomUUID().toString().replace("-",""));
        a=new ScheduleDaoImpl();b=new ScheduleDaoImpl();ReflectionTestUtils.setField(a,"mongoTemplate",mongo);ReflectionTestUtils.setField(b,"mongoTemplate",mongo);
    }
    @AfterEach void close(){try{mongo.getDb().drop();}finally{client.close();}}
    CustomSchedule course(String name){var c=new CustomSchedule();c.setPosition(0);c.setScheduleName(name);c.setScheduleLocation("synthetic");c.setScheduleLength(1);c.setMinScheduleWeek(1);c.setMaxScheduleWeek(20);return c;}
    @Test void twoInstancesDoNotLoseConcurrentAddsOrDuplicateFirstInsert() throws Exception {
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try{
            var one=pool.submit(()->{start.await();a.addCustomSchedule("synthetic",course("one"));return null;});
            var two=pool.submit(()->{start.await();b.addCustomSchedule("synthetic",course("two"));return null;});
            start.countDown();one.get(10,TimeUnit.SECONDS);two.get(10,TimeUnit.SECONDS);
            assertEquals(2,a.queryCustomSchedule("synthetic").getScheduleMap().size());
            assertEquals(1,mongo.getCollection("customSchedule").countDocuments());
        }finally{pool.shutdownNow();}
    }
    @Test void sixthCourseIsRejectedAndDeletionTargetsOneExactId() throws Exception {
        for(int i=0;i<5;i++)a.addCustomSchedule("synthetic",course("course"+i));
        assertThrows(CountOverLimitException.class,()->b.addCustomSchedule("synthetic",course("sixth")));
        assertThrows(IllegalArgumentException.class,()->a.deleteCustomSchedule("synthetic",0));
        String id=a.queryCustomSchedule("synthetic").getScheduleMap().keySet().iterator().next();
        assertFalse(a.deleteCustomSchedule("other",0,id));assertTrue(a.deleteCustomSchedule("synthetic",0,id));
        assertEquals(4,a.queryCustomSchedule("synthetic").getScheduleMap().size());
        assertFalse(a.deleteCustomSchedule("synthetic",0,id));
    }
    @Test void unversionedLegacyDocumentKeepsIdentityWhenCourseIsAdded() throws Exception {
        var original=new org.bson.Document("username","synthetic").append("scheduleMap",new org.bson.Document());
        mongo.getCollection("customSchedule").insertOne(original);
        a.addCustomSchedule("synthetic",course("legacy-added"));
        var current=mongo.getCollection("customSchedule").find().first();
        assertEquals(original.get("_id"),current.get("_id"));assertEquals(1L,((Number)current.get("version")).longValue());
        assertEquals(1,a.queryCustomSchedule("synthetic").getScheduleMap().size());assertEquals(1,mongo.getCollection("customSchedule").countDocuments());
    }
    @Test void duplicateLegacyOwnersAreRejectedWithoutChangingEitherDocument() {
        var first=new org.bson.Document("username","synthetic").append("scheduleMap",new org.bson.Document());
        var second=new org.bson.Document("username","synthetic").append("scheduleMap",new org.bson.Document());
        mongo.getCollection("customSchedule").insertMany(java.util.List.of(first,second));
        assertThrows(IllegalStateException.class,()->a.addCustomSchedule("synthetic",course("new")));
        assertThrows(IllegalStateException.class,()->a.deleteCustomSchedule("synthetic",0));
        assertEquals(java.util.List.of(first,second),mongo.getCollection("customSchedule").find().into(new java.util.ArrayList<>()));
    }

    @Test void renamedNewsClassReadsHistoricalCollectionAndPayloadWithoutRewritingIt() {
        var original=new org.bson.Document("_id","synthetic-news").append("type",1)
                .append("title","Synthetic news").append("publishDate",new java.util.Date(0))
                .append("_class","cn.gdeiassistant.common.pojo.entity.NewInfo");
        mongo.getCollection("new").insertOne(original);
        var repository=new cn.gdeiassistant.core.news.repository.NewsRepositoryImpl();
        ReflectionTestUtils.setField(repository,"mongoTemplate",mongo);
        assertEquals("Synthetic news",repository.queryNewsItem("synthetic-news").getTitle());
        assertEquals(1,repository.queryNewsItems(1,0,10).size());
        assertEquals(original,mongo.getCollection("new").find().first());
    }

}
