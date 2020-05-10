package edu.gdei.gdeiassistant.Test;

import edu.gdei.gdeiassistant.Config.Application.ApplicationContextConfig;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * {@code @RunWith}指定使用SpringJUnit4ClassRunner来执行Junit用例
 *
 * <p>{@code @WebAppConfiguration}用于声明一个ApplicationContext集成测试加载WebApplicationContext，作用是模拟ServletContext
 *
 * <p>{@code @ContextConfiguration}指定Spring的配置类或配置文件
 *
 * <p>{@code @ActiveProfiles}激活的SpringProfile环境
 *
 * <p>{@code @Transactional}用于激活数据库操作的事务管理，注解的Value属性指定了使用的事务管理器，测试人员可以根据实际需要进行修改
 *
 * <p>{@code @Rollback}将在测试方法结束后，回滚对数据库的操作，避免产生脏数据和额外的数据库恢复代码
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ActiveProfiles(profiles = "development")
@ContextConfiguration(classes = ApplicationContextConfig.class)
@Transactional("appTransactionManager")
@Rollback
public class BaseJunitTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    /**
     * 使用MockMVC模拟请求来测试Controller
     */
    private MockMvc mockMvc;

    @Before()
    public void setup() {
        //在每个方法执行之前都会初始化MockMVC对象
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
}
