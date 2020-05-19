package edu.gdei.gdeiassistant.Config.Application;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.scheduling.config.TaskManagementConfigUtils;
import org.springframework.stereotype.Component;

/**
 * <p>@EnableAsync给容器注入的是AsyncAnnotationBeanPostProcessor，它用于给@Async生成代理
 * 但是它仅仅是个BeanPostProcessor并不属于自动代理创建器，因此exposeProxy = true对它无效
 *
 * <p>@Async要想顺利使用AopContext.currentProxy()获取当前代理对象来调用本类方法，需要确保你本Bean已经被自动代理创建器提前代理
 *
 * <p>从@Async案例找到Spring框架的bug：exposeProxy=true不生效原因大剖析+最佳解决方案
 *
 * @link https://cloud.tencent.com/developer/article/1497700
 */
@Component
public class ApplicationBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(TaskManagementConfigUtils.ASYNC_ANNOTATION_PROCESSOR_BEAN_NAME);
        beanDefinition.getPropertyValues().add("exposeProxy", true);
    }
}
