package cn.gdeiassistant.core.marketplace.mapper;

import cn.gdeiassistant.core.marketplace.service.MarketplaceService;
import cn.gdeiassistant.core.user.mapper.PublicAuthorMapper;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.*;
import java.sql.Connection;
import java.util.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnabledIfEnvironmentVariable(named="GDEI_UPGRADE_TEST_JDBC_BASE", matches="jdbc:mysql://127\\.0\\.0\\.1:.+")
class MarketplaceMySqlQueryTest {
    private static final String SCHEMA="gdei_marketplace_query_test";
    private static DriverManagerDataSource data;
    private static SqlSessionFactory factory;
    private static final QueryCounter count=new QueryCounter();
    private static DriverManagerDataSource source(String schema) {return new DriverManagerDataSource(System.getenv("GDEI_UPGRADE_TEST_JDBC_BASE")+schema+"?sslMode=DISABLED&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true","root",System.getenv("GDEI_UPGRADE_TEST_PASSWORD"));}
    @BeforeAll static void setup() throws Exception {
        new JdbcTemplate(source("mysql")).execute("CREATE DATABASE IF NOT EXISTS "+SCHEMA);data=source(SCHEMA);var jdbc=new JdbcTemplate(data);
        String initialization=Files.readString(Path.of("db-init/mysql/init.sql"));
        var matcher=Pattern.compile("CREATE TABLE `ershou` \\(.*?\\) ENGINE=.*?;",Pattern.DOTALL).matcher(initialization);assertTrue(matcher.find());
        jdbc.execute(matcher.group());jdbc.execute("CREATE TABLE profile(username VARCHAR(24) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci PRIMARY KEY,nickname VARCHAR(30))");
        jdbc.update("INSERT INTO profile VALUES('synthetic-owner','合成昵称')");
        jdbc.batchUpdate("INSERT INTO ershou(username,name,description,price,location,type,state,publish_time) VALUES(?,?,?,?,?,?,?,?)",new org.springframework.jdbc.core.BatchPreparedStatementSetter(){
            public int getBatchSize(){return 10000;}
            public void setValues(java.sql.PreparedStatement statement,int i)throws java.sql.SQLException {
                statement.setString(1,i%8==0?"synthetic-owner":"synthetic-other");statement.setString(2,"合成商品"+i);statement.setString(3,i%200==0?"稀有合成关键词":"合成说明");
                statement.setBigDecimal(4,new java.math.BigDecimal("12.34"));statement.setString(5,"合成地点");statement.setInt(6,i%5);statement.setInt(7,i%4==0?1:2);statement.setTimestamp(8,new java.sql.Timestamp(1767225600000L));
            }});
        jdbc.execute("ANALYZE TABLE ershou");
        var config=new Configuration(new Environment("synthetic",new JdbcTransactionFactory(),data));config.addInterceptor(count);config.addMapper(MarketplaceMapper.class);factory=new SqlSessionFactoryBuilder().build(config);
    }
    @AfterAll static void cleanup(){new JdbcTemplate(source("mysql")).execute("DROP DATABASE IF EXISTS "+SCHEMA);}
    private Map<String,Object> explain(String method,Map<String,Object> params) throws Exception {
        var mapped=factory.getConfiguration().getMappedStatement(MarketplaceMapper.class.getName()+"."+method);var bound=mapped.getBoundSql(params);
        try(var connection=data.getConnection();var statement=connection.prepareStatement("EXPLAIN "+bound.getSql())) {
            new DefaultParameterHandler(mapped,params,bound).setParameters(statement);
            try(var rows=statement.executeQuery()){assertTrue(rows.next());var result=new LinkedHashMap<String,Object>();result.put("key",rows.getString("key"));result.put("type",rows.getString("type"));result.put("rows",rows.getLong("rows"));result.put("extra",rows.getString("Extra"));return result;}
        }
    }
    @Test void actualMapperSqlUsesIndexesForPublicTypedAndOwnerPages() throws Exception {
        for(String method:List.of("selectAvailableItems","selectItemsByType","selectItemsByUsername")) {
            var plan=explain(method,Map.of("start",0,"size",10,"type",2,"username","synthetic-owner"));
            System.out.println("SYNTHETIC_EXPLAIN " + method + " " + plan);
            assertNotNull(plan.get("key"),method+" "+plan);assertNotEquals("ALL",plan.get("type"),method+" "+plan);
            assertFalse(String.valueOf(plan.get("extra")).contains("filesort"),method+" "+plan);
        }
        var keyword=explain("selectItemsWithKeyword",Map.of("start",0,"size",10,"keyword","稀有合成关键词"));
        System.out.println("SYNTHETIC_EXPLAIN keyword " + keyword);
        assertNotNull(keyword.get("key")); // LIKE with leading wildcard still requires scanning; no claim of index-only search.
    }
    @Test void sameTimestampPagesHaveNoOverlapAndFilterBeforePagination() {
        try(var sql=factory.openSession()) {
            var mapper=sql.getMapper(MarketplaceMapper.class);var first=mapper.selectAvailableItems(0,10);var second=mapper.selectAvailableItems(10,10);
            assertEquals(10,first.size());assertEquals(10,second.size());var ids=new HashSet<Integer>();first.forEach(item->assertTrue(ids.add(item.getId())));second.forEach(item->assertTrue(ids.add(item.getId())));
            assertTrue(first.get(9).getId()>second.get(0).getId());assertTrue(first.stream().allMatch(item->item.getState()==1));
            var typed=mapper.selectItemsByType(0,10,2);assertEquals(10,typed.size());assertTrue(typed.stream().allMatch(item->item.getType()==2&&item.getState()==1));
            assertTrue(mapper.selectItemsWithKeyword(0,10,"'; DROP TABLE ershou; --").isEmpty());assertEquals(10,mapper.selectAvailableItems(0,10).size());
            var detail=mapper.selectInfoByID(1);assertEquals("合成昵称",detail.getProfile().getNickname());assertEquals(new java.math.BigDecimal("12.34"),detail.getMarketplaceItem().getPrice());
        }
    }
    @Test void publicPageRunsOneSqlAndOneBatchedAuthorLookupRegardlessOfItemCount() throws Exception {
        try(var sql=factory.openSession()) {
            var authors=mock(PublicAuthorMapper.class);when(authors.selectAuthors(anyList())).thenReturn(List.of(new PublicAuthorMapper.AuthorRow("synthetic-owner","synthetic-public-id","合成昵称")));
            var resolver=new PublicAuthorResolver();ReflectionTestUtils.setField(resolver,"publicAuthorMapper",authors);
            var service=new MarketplaceService();ReflectionTestUtils.setField(service,"marketplaceMapper",sql.getMapper(MarketplaceMapper.class));ReflectionTestUtils.setField(service,"publicAuthorResolver",resolver);
            count.statements=0;var items=service.queryItems(0);assertEquals(10,items.size());assertEquals(1,count.statements);verify(authors,times(1)).selectAuthors(anyList());
            assertTrue(items.stream().anyMatch(item->"synthetic-public-id".equals(item.getAuthorId())));assertTrue(items.stream().anyMatch(item->"用户".equals(item.getDisplayName())));
        }
    }
    @Intercepts(@Signature(type=StatementHandler.class,method="prepare",args={Connection.class,Integer.class}))
    static class QueryCounter implements Interceptor {int statements;public Object intercept(Invocation call)throws Throwable{statements++;return call.proceed();}}
}
