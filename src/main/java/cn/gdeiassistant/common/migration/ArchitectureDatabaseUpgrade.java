package cn.gdeiassistant.common.migration;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** One bounded, repeatable upgrade. Preflight never rewrites or deletes existing records. */
public final class ArchitectureDatabaseUpgrade {
    private ArchitectureDatabaseUpgrade() {}

    public static void migrate(DataSource app, DataSource data, DataSource log) throws SQLException {
        try (Connection a = app.getConnection(); Connection d = data.getConnection(); Connection l = log.getConnection()) {
            // Validate all constraints before issuing any DDL (DDL itself is not transactional).
            rejectDuplicates(a, "delivery_trade", "order_id");
            if (tableExists(l, "charge_order")) rejectDuplicates(l, "charge_order", "username,idempotency_key_hash");
            if (tableExists(l, "close_log")) rejectDuplicates(l, "close_log", "resetname");
            rejectOrphans(a,"delivery_trade","order_id","delivery_order","order_id");
            rejectOrphans(a,"secret_comment","content_id","secret_content","id");
            rejectOrphans(a,"secret_like","content_id","secret_content","id");
            rejectOrphans(a,"express_comment","express_id","express","id");
            rejectOrphans(a,"photograph_comment","photo_id","photograph","id");
            rejectMoney(a,"delivery_order","price",9999.99);
            rejectMoney(a,"ershou","price",9999.99);
            rejectMoney(d,"electricfees","electric_price",999999.9999);
            rejectMoney(d,"electricfees","total_electric_bill",9999999999.99);
            rejectMoney(d,"electricfees","average_electric_bill",9999999999.99);
            Map<String,Long> before = counts(a,"app_user","campus_credential","delivery_order","delivery_trade","ershou");
            Map<String,Long> logBefore = optionalCounts(l, "charge_order", "close_log");
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(l,
                    new org.springframework.core.io.ClassPathResource("db/architecture-log-tables.sql"));
            decimal(a,"delivery_order","price",6,2);
            decimal(a,"ershou","price",6,2);
            decimal(d,"electricfees","electric_price",10,4);
            decimal(d,"electricfees","total_electric_bill",12,2);
            decimal(d,"electricfees","average_electric_bill",12,2);
            widen(a,"delivery_order","number",64,"取件码，可为空字符串");
            widen(a,"delivery_order","company",100,"取件地点");
            index(a,"delivery_order","idx_delivery_feed",false,"state,order_time DESC,order_id DESC");
            index(a,"delivery_order","idx_delivery_owner",false,"username,order_id DESC");
            index(a,"delivery_trade","uk_delivery_trade_order",true,"order_id");
            index(a,"delivery_trade","idx_delivery_trade_owner",false,"username,create_time DESC,trade_id DESC");
            index(a,"secret_comment","idx_secret_comment_parent",false,"content_id,id");
            index(a,"express_comment","idx_express_comment_parent",false,"express_id,id");
            index(a,"photograph_comment","idx_photograph_comment_parent",false,"photo_id,comment_id");
            index(l,"charge_order","idx_charge_order_idempotency_hash",true,"username,idempotency_key_hash");
            index(l,"close_log","uk_close_log_resetname",true,"resetname");
            foreignKey(a,"delivery_trade","fk_delivery_trade_order","order_id","delivery_order","order_id","RESTRICT");
            foreignKey(a,"secret_comment","fk_secret_comment_parent","content_id","secret_content","id","CASCADE");
            foreignKey(a,"secret_like","fk_secret_like_parent","content_id","secret_content","id","CASCADE");
            foreignKey(a,"express_comment","fk_express_comment_parent","express_id","express","id","CASCADE");
            foreignKey(a,"photograph_comment","fk_photograph_comment_parent","photo_id","photograph","id","CASCADE");
            execute(a,"CREATE TABLE IF NOT EXISTS account_deletion_cleanup (" +
                    "id varchar(36) NOT NULL,username varchar(24) DEFAULT NULL,resetname varchar(24) NOT NULL," +
                    "user_id bigint DEFAULT NULL,status varchar(16) NOT NULL,attempts int NOT NULL DEFAULT 0," +
                    "next_attempt_at datetime NOT NULL,locked_until datetime DEFAULT NULL,error_code varchar(100) DEFAULT NULL," +
                    "PRIMARY KEY(id),UNIQUE KEY uk_cleanup_resetname(resetname),KEY idx_cleanup_retry(status,next_attempt_at)) " +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin");
            if (!before.equals(counts(a,"app_user","campus_credential","delivery_order","delivery_trade","ershou"))) {
                throw new SQLException("Unexpected row-count change during architecture migration");
            }
            if (!logBefore.equals(counts(l, "charge_order", "close_log"))) {
                throw new SQLException("Unexpected log row-count change during architecture migration");
            }
        }
    }

    private static boolean tableExists(Connection c, String table) throws SQLException {
        try (PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=?")) {
            q.setString(1,table);
            try (ResultSet r=q.executeQuery()) { r.next(); return r.getInt(1)>0; }
        }
    }
    private static Map<String,Long> optionalCounts(Connection c, String... tables) throws SQLException {
        Map<String,Long> result=new LinkedHashMap<>();
        for (String table:tables) result.put(table,tableExists(c,table)?scalar(c,"SELECT COUNT(*) FROM `"+table+"`"):0L);
        return result;
    }

    private static Map<String,Long> counts(Connection c,String... tables) throws SQLException {
        Map<String,Long> result=new LinkedHashMap<>();
        for(String table:tables) result.put(table,scalar(c,"SELECT COUNT(*) FROM `"+table+"`"));
        return result;
    }
    private static void rejectDuplicates(Connection c,String table,String key) throws SQLException {
        String columns = "`" + key.replace(",", "`,`") + "`";
        String present = java.util.Arrays.stream(key.split(",")).map(k -> "`" + k + "` IS NOT NULL").collect(java.util.stream.Collectors.joining(" AND "));
        if(scalar(c,"SELECT COUNT(*) FROM (SELECT "+columns+" FROM `"+table+"` WHERE "+present+" GROUP BY "+columns+" HAVING COUNT(*)>1) duplicates")!=0)
            throw new SQLException("Resolve duplicate keys before migration: "+table+"."+key);
    }
    private static void rejectOrphans(Connection c,String child,String field,String parent,String key) throws SQLException {
        if(scalar(c,"SELECT COUNT(*) FROM `"+child+"` c LEFT JOIN `"+parent+"` p ON c.`"+field+"`=p.`"+key+"` WHERE p.`"+key+"` IS NULL")!=0)
            throw new SQLException("Resolve orphan rows before migration: "+child);
    }
    private static void rejectMoney(Connection c,String table,String field,double maximum) throws SQLException {
        if(scalar(c,"SELECT COUNT(*) FROM `"+table+"` WHERE `"+field+"`<0 OR `"+field+"`>"+maximum)!=0)
            throw new SQLException("Money outside supported range: "+table+"."+field);
    }
    private static void decimal(Connection c,String table,String column,int precision,int scale) throws SQLException {
        try(PreparedStatement q=c.prepareStatement("SELECT DATA_TYPE,NUMERIC_PRECISION,NUMERIC_SCALE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            q.setString(1,table);q.setString(2,column);
            try(ResultSet r=q.executeQuery()) {
                if(!r.next()) throw new SQLException("Missing money column: "+table+"."+column);
                if("decimal".equalsIgnoreCase(r.getString(1)) && r.getInt(2)==precision && r.getInt(3)==scale) return;
            }
        }
        execute(c,"ALTER TABLE `"+table+"` MODIFY `"+column+"` DECIMAL("+precision+","+scale+") NOT NULL");
    }
    private static void widen(Connection c,String table,String column,int size,String description) throws SQLException {
        try(PreparedStatement q=c.prepareStatement("SELECT CHARACTER_MAXIMUM_LENGTH FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            q.setString(1,table);q.setString(2,column);
            try(ResultSet r=q.executeQuery()) { if(!r.next()) throw new SQLException("Missing column: "+column);if(r.getInt(1)>=size)return; }
        }
        execute(c,"ALTER TABLE `"+table+"` MODIFY `"+column+"` varchar("+size+") CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '"+description+"'");
    }
    private static void index(Connection c,String table,String name,boolean unique,String columns) throws SQLException {
        try(PreparedStatement q=c.prepareStatement("SELECT NON_UNIQUE,GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ',') FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND INDEX_NAME=? GROUP BY NON_UNIQUE")) {
            q.setString(1,table);q.setString(2,name);
            try(ResultSet r=q.executeQuery()) { if(r.next()) { if((r.getInt(1)==0)==unique && columns.replace(" DESC", "").replace(" ASC", "").equalsIgnoreCase(r.getString(2)))return;execute(c,"ALTER TABLE `"+table+"` DROP INDEX `"+name+"`"); } }
        }
        execute(c,"ALTER TABLE `"+table+"` ADD "+(unique?"UNIQUE ":"")+"INDEX `"+name+"` ("+columns+")");
    }
    private static void foreignKey(Connection c,String table,String name,String column,String parent,String key,String onDelete) throws SQLException {
        try(PreparedStatement q=c.prepareStatement("SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME=? AND CONSTRAINT_NAME=?")) {
            q.setString(1,table);q.setString(2,name);
            try(ResultSet r=q.executeQuery()) { r.next();if(r.getInt(1)!=0)return; }
        }
        execute(c,"ALTER TABLE `"+table+"` ADD CONSTRAINT `"+name+"` FOREIGN KEY (`"+column+"`) REFERENCES `"+parent+"` (`"+key+"`) ON DELETE "+onDelete);
    }
    private static long scalar(Connection c,String sql) throws SQLException { try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getLong(1);} }
    private static void execute(Connection c,String sql) throws SQLException { try(Statement s=c.createStatement()){s.execute(sql);} }
}
