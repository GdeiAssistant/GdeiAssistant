package cn.gdeiassistant.core.social.mapper;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SocialPublicIdentityMapperTest {
    @Test void batchPublicIdentitiesDoNotRequireOrReadCampusCredentials() throws Exception {
        var database=new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1","sa","");
        try(var connection=database.getConnection();var sql=connection.createStatement()) {
            sql.execute("CREATE TABLE app_user(id BIGINT PRIMARY KEY,public_id VARCHAR(36),status VARCHAR(16))");
            sql.execute("INSERT INTO app_user VALUES(1,'synthetic-active','ACTIVE'),(2,'synthetic-closed','CLOSED')");
        }
        var configuration=new Configuration(new Environment("synthetic",new JdbcTransactionFactory(),database));
        configuration.addMapper(SocialUserSummaryMapper.class);
        try(var session=new SqlSessionFactoryBuilder().build(configuration).openSession()) {
            var rows=session.getMapper(SocialUserSummaryMapper.class).selectPublicIds(List.of(1L,2L,999L));
            assertEquals(2,rows.size());
            for(var row:rows) assertEquals(Set.of("userId","publicId"),row.keySet());
            assertEquals(Set.of("synthetic-active","synthetic-closed"),rows.stream().map(row->row.get("publicId")).collect(java.util.stream.Collectors.toSet()));
        }
    }
}
