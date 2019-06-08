package edu.gdei.gdeiassistant.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrialData {

    /**
     * 功能模块名，对应data.properties配置文件中的Key：trial.data.[value]中的Value值
     *
     * @return
     */
    String value();

    /**
     * 是否返回基础JSON数据，若是则返回JSONResult类型，否则返回DataJSONResult类型，默认为否
     *
     * @return
     */
    boolean base() default false;


    /**
     * 若返回的JSON数据携带时间信息，如查询的学年、周数和日期
     * <p>
     * 则填写对应的时间信息属性字段名，默认为空，该时间信息属性将填充入返回用户的测试数据中
     *
     * @return
     */
    String time() default "";
}
