package edu.gdei.gdeiassistant.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestCheckAuthentication {

    /**
     * 功能模块名，对应配置文件中的名称
     *
     * @return
     */
    String name();
}
