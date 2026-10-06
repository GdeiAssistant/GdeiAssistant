package cn.gdeiassistant.core.delivery.service;

import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.pojo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryTransactionTest {
    @Test
    void acceptingThroughPublicEntryRollsBackOrderWhenTradeInsertFails() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:deliveryRollback;DB_CLOSE_DELAY=-1", "sa", "");
        var sql = new JdbcTemplate(source);
        sql.execute("create table delivery_order(order_id int primary key, state int)");
        sql.update("insert into delivery_order values(1,0)");
        var mapper = mock(DeliveryMapper.class);
        var certificate = mock(UserCertificateService.class);
        when(certificate.getUserLoginCertificate("session")).thenReturn(new User("runner"));
        when(mapper.selectDeliveryOrderUsername(1)).thenReturn("owner");
        when(mapper.updateOrderState(1,1)).thenAnswer(call -> sql.update("update delivery_order set state=1 where order_id=1 and state=0"));
        doThrow(new IllegalStateException("synthetic insert failure")).when(mapper).insertTradeRecord(any(DeliveryTradeEntity.class));
        var service = new DeliveryService();
        ReflectionTestUtils.setField(service,"deliveryMapper",mapper);
        ReflectionTestUtils.setField(service,"userCertificateService",certificate);
        var manager = new DataSourceTransactionManager(source);
        var beans = new DefaultListableBeanFactory();
        beans.registerSingleton("appTransactionManager",manager);
        var advice = new TransactionInterceptor(manager,new AnnotationTransactionAttributeSource());
        advice.setBeanFactory(beans);
        var factory = new ProxyFactory(service);
        factory.setProxyTargetClass(true);
        factory.addAdvice(advice);
        var proxy = (DeliveryService)factory.getProxy();
        assertThrows(IllegalStateException.class, () -> proxy.acceptOrder(1,"session"));
        assertEquals(0,sql.queryForObject("select state from delivery_order where order_id=1",Integer.class));
    }
}
