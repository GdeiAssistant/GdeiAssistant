package cn.gdeiassistant.common.config.DataSource;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataSourceMapperScanConfigTest {

    @Test
    void allMapperScanPackagesResolveOnClasspath() {
        assertMapperPackagesResolvable(AppDataSourceConfig.class);
        assertMapperPackagesResolvable(DataDataSourceConfig.class);
        assertMapperPackagesResolvable(LogDataSourceConfig.class);
    }

    private void assertMapperPackagesResolvable(Class<?> configClass) {
        MapperScan mapperScan = configClass.getAnnotation(MapperScan.class);
        assertNotNull(mapperScan, configClass.getSimpleName() + " should declare @MapperScan");

        ClassLoader classLoader = configClass.getClassLoader();
        for (String basePackage : mapperScan.basePackages()) {
            String resourcePath = basePackage.replace('.', '/');
            assertNotNull(classLoader.getResource(resourcePath),
                    () -> configClass.getSimpleName() + " references missing mapper package: " + basePackage);
        }
    }
}
