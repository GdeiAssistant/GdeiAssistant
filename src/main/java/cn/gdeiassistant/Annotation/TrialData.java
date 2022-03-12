package cn.gdeiassistant.Annotation;

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
     * 是否通过令牌访问资源
     *
     * @return
     */
    boolean rest();

    /**
     * 若请求的数据要求携带时间信息，则填写对应的请求时间信息属性字段名。默认该值为空
     * <p>
     * 如成绩查询功能，此处填写注解的RequestTime属性值为Year，则系统会拦截并获取成绩查询请求中属性名为Year的参数属性值
     * 作为请求模拟数据时携带的时间信息
     * <p>
     * 该时间信息将被用于获取模拟数据或模拟数据后加工的过程中
     * <p>
     * 如根据课表查询的指定周数，筛选剔除掉不在当前周数范围内的课程信息
     * 又如成绩查询的指定学年，会区分不同学年，返回对应学年的模拟数据结果
     * <p>
     * 若该项不为字符串，但系统没有拦截到对应名称的属性（或值为空），则系统该会默认为其赋予一个时间信息
     *
     * @return
     */
    String requestTime() default "";

    /**
     * 若返回的JSON数据携带时间信息，如查询的学年、周数和日期。默认该值为空
     * <p>
     * 如成绩查询功能，此处填写注解的ResponseTime属性值为Year，则系统会拦截并获取成绩查询请求中属性名为Year的参数属性值
     * 作为需要填充入返回用户的测试数据中时间信息
     * <p>
     * 该时间信息属性将填充入返回用户的测试数据中
     * <p>
     * 若该项不为空字符串，但系统没有拦截到对应名称的属性（或值为空），则系统该会默认为其赋予一个时间信息
     *
     * @return
     */
    String responseTime() default "";
}
