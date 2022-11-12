package cn.gdeiassistant.Annotation;

import cn.gdeiassistant.Enum.IPAddress.IPAddressEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordIPAddress {

    IPAddressEnum type();

    boolean rest() default false;
}
