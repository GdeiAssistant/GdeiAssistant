package cn.gdeiassistant.common.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="GDEI_UPGRADE_TEST_JDBC_BASE", matches=".+")
class ArchitectureDatabaseUpgradeMySqlTest {
    private DriverManagerDataSource source(String schema) {
        return new DriverManagerDataSource(System.getenv("GDEI_UPGRADE_TEST_JDBC_BASE")+schema+"?sslMode=DISABLED&allowPublicKeyRetrieval=true",
                "root",System.getenv("GDEI_UPGRADE_TEST_PASSWORD"));
    }
    @Test
    void upgradesExistingSchemaTwicePreservingRecordsAndEnforcingConstraints() throws Exception {
        var app=source("gdeiassistant");var data=source("gdeiassistant_data");var log=source("gdeiassistant_log");
        var a=new JdbcTemplate(app);var l=new JdbcTemplate(log);
        int originalItems=a.queryForObject("select count(*) from ershou",Integer.class);
        ArchitectureDatabaseUpgrade.migrate(app,data,log);
        ArchitectureDatabaseUpgrade.migrate(app,data,log);
        assertEquals(originalItems,a.queryForObject("select count(*) from ershou",Integer.class));
        assertEquals("decimal",a.queryForObject("select DATA_TYPE from information_schema.COLUMNS where TABLE_SCHEMA=DATABASE() and TABLE_NAME='ershou' and COLUMN_NAME='price'",String.class));
        assertEquals(0,l.queryForObject("select min(NON_UNIQUE) from information_schema.STATISTICS where TABLE_SCHEMA=DATABASE() and TABLE_NAME='charge_order' and INDEX_NAME='idx_charge_order_idempotency_hash'",Integer.class));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> a.update("insert into delivery_trade(order_id,username,create_time,state) values(2147483647,'synthetic',now(),0)"));
        var plan=a.queryForMap("EXPLAIN SELECT order_id FROM delivery_order WHERE state=0 ORDER BY order_time DESC,order_id DESC LIMIT 20");
        assertTrue(String.valueOf(plan.get("possible_keys")).contains("idx_delivery_feed"));
    }
    @Test
    void idempotencyIsUniquePerUserAndDoesNotBlockAnotherUsersKey() throws Exception {
        ArchitectureDatabaseUpgrade.migrate(source("gdeiassistant"), source("gdeiassistant_data"), source("gdeiassistant_log"));
        var l = new JdbcTemplate(source("gdeiassistant_log"));
        String key = "synthetic-architecture-key";
        String insert = "INSERT INTO charge_order(order_id,username,amount,status,idempotency_key_hash,created_at,updated_at) VALUES(?,?,1,'CREATED',?,now(),now())";
        try {
            l.update(insert, "synthetic-order-a", "synthetic-a", key);
            l.update(insert, "synthetic-order-b", "synthetic-b", key);
            assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                    () -> l.update(insert, "synthetic-order-duplicate", "synthetic-a", key));
            assertEquals(2, l.queryForObject("SELECT COUNT(*) FROM charge_order WHERE idempotency_key_hash=?", Integer.class, key));
        } finally { l.update("DELETE FROM charge_order WHERE idempotency_key_hash=?", key); }
    }

    @Test
    void duplicatePreflightAbortsBeforeAnyDdl() throws Exception {
        var server = new JdbcTemplate(source("mysql"));
        server.execute("CREATE DATABASE IF NOT EXISTS gdei_preflight_test");
        var preflight = source("gdei_preflight_test");
        var a = new JdbcTemplate(preflight);
        a.execute("CREATE TABLE delivery_trade(order_id int, price float)");
        try {
            a.update("INSERT INTO delivery_trade VALUES(1,1.25),(1,2.5)");
            assertThrows(java.sql.SQLException.class, () -> ArchitectureDatabaseUpgrade.migrate(
                    preflight, source("gdeiassistant_data"), source("gdeiassistant_log")));
            assertEquals("float", a.queryForObject("SELECT DATA_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='delivery_trade' AND COLUMN_NAME='price'", String.class));
            assertEquals(2, a.queryForObject("SELECT COUNT(*) FROM delivery_trade", Integer.class));
        } finally { a.execute("DROP TABLE delivery_trade"); }
    }

    @Test
    void oneOrderCannotHaveTwoTrades() throws Exception {
        var app=source("gdeiassistant");var data=source("gdeiassistant_data");var log=source("gdeiassistant_log");
        var a=new JdbcTemplate(app);
        a.update("insert into delivery_order(username,order_time,name,number,phone,price,company,address,state,remarks) values('synthetic',now(),'task','','00000000000',1,'pickup','address',0,'')");
        int order=a.queryForObject("select max(order_id) from delivery_order",Integer.class);
        a.update("insert into delivery_trade(order_id,username,create_time,state) values(?,'synthetic',now(),0)",order);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> a.update("insert into delivery_trade(order_id,username,create_time,state) values(?,'synthetic-2',now(),0)",order));
        a.update("delete from delivery_trade where order_id=?",order);
        a.update("delete from delivery_order where order_id=?",order);
    }
}
