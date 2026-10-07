package cn.gdeiassistant.common.config.datasource;

import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SqlSessionFactoryConfigTest {
    @Test void threeFactoriesBuildWithoutScanningDuplicateBusinessDtoAliases() throws Exception {
        var source=mock(DataSource.class);
        var app=new AppDataSourceConfig().appSqlSessionFactory(source).getObject();
        var data=new DataDataSourceConfig().dataSqlSessionFactory(source).getObject();
        var log=new LogDataSourceConfig().logSqlSessionFactory(source).getObject();
        assertNotNull(app);assertNotNull(data);assertNotNull(log);
        app.getConfiguration().addMapper(cn.gdeiassistant.core.objectstorage.mapper.StoredAssetMapper.class);
        app.getConfiguration().addMapper(cn.gdeiassistant.core.social.mapper.SocialUserSummaryMapper.class);
        app.getConfiguration().addMapper(cn.gdeiassistant.core.user.mapper.PublicAuthorMapper.class);
        assertTrue(app.getConfiguration().hasStatement("cn.gdeiassistant.core.objectstorage.mapper.StoredAssetMapper.pending"));
    }
}
