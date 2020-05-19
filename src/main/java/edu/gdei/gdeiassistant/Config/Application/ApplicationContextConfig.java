package edu.gdei.gdeiassistant.Config.Application;

import edu.gdei.gdeiassistant.Converter.HttpMessageConvert.WeChatMappingJackson2HttpMessageConverter;
import edu.gdei.gdeiassistant.ErrorHandler.RestTemplateResponseErrorHandler;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = "edu.gdei.gdeiassistant")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
public class ApplicationContextConfig {

    /**
     * RestTemplate
     *
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new WeChatMappingJackson2HttpMessageConverter());
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        return restTemplate;
    }

    /**
     * AsyncRestTemplate
     *
     * @return
     */
    @Bean
    public AsyncRestTemplate asyncRestTemplate() {
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        asyncRestTemplate.getMessageConverters().add(new WeChatMappingJackson2HttpMessageConverter());
        asyncRestTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        return asyncRestTemplate;
    }

    /**
     * Properties配置文件读取器
     *
     * @return
     */
    @Bean
    public PropertiesFactoryBean propertiesReader() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setFileEncoding(StandardCharsets.UTF_8.displayName());
        propertiesFactoryBean.setLocations(ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())
                .getResources("classpath*:/config/**/*.properties"));
        return propertiesFactoryBean;
    }

    /**
     * 注册方法级别验证后处理器
     *
     * @return
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    /**
     * 文件上传配置的MultipartResolver处理器
     *
     * @return
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        return commonsMultipartResolver;
    }

    /**
     * RSS订阅的校园新闻通知系统地址列表，新闻类型都属于综合信息
     *
     * @return
     */
    @Bean
    public List<String> rssNewsUrlsList() {
        List<String> rssNewsUrlsList = new ArrayList<>();
        //公告
        rssNewsUrlsList.add("http://www.gdei.edu.cn/www/gonggaoxinxi/inform/index.xml");
        //通知
        rssNewsUrlsList.add("http://www.gdei.edu.cn/www/gonggaoxinxi/tongzhi/index.xml");
        //网络通知
        rssNewsUrlsList.add("http://www.gdei.edu.cn/www/gonggaoxinxi/netinform/index.xml");
        //学校新闻
        rssNewsUrlsList.add("http://www.gdei.edu.cn/www/xueyuanxinwen/index.xml");
        return rssNewsUrlsList;
    }

    /**
     * 校园新闻通知系统地址列表
     *
     * @return
     */
    @Bean
    public List<List<String>> newsUrlsList() {
        List<List<String>> list = new ArrayList<>();
        //教学信息新闻列表
        List<String> studyNewsUrlList = new ArrayList<>();
        //毕业论文
        studyNewsUrlList.add("http://web.gdei.edu.cn/jwc/sjjx/bylw");
        //创新创业
        studyNewsUrlList.add("http://web.gdei.edu.cn/jwc/sjjx/cxcy");
        //实习见习与社会实践
        studyNewsUrlList.add("http://web.gdei.edu.cn/jwc/sjjx/sxjxyshsj");
        //实验教学
        studyNewsUrlList.add("http://web.gdei.edu.cn/jwc/sjjx/syjx");
        //学科竞赛
        studyNewsUrlList.add("http://web.gdei.edu.cn/jwc/sjjx/xkjs");
        //教学质量监控
        studyNewsUrlList.add("http://web.gdei.edu.cn/jwc/zljk");
        //落实教学计划
        studyNewsUrlList.add("http://web.gdei.edu.cn/jwc/jwtz/jhrwls");
        //考试信息新闻列表
        List<String> examinationNewsUrlList = new ArrayList<>();
        //校内课程考核
        examinationNewsUrlList.add("http://web.gdei.edu.cn/jwc/xnwks/xnkckh");
        //等级竞赛考试
        examinationNewsUrlList.add("http://web.gdei.edu.cn/jwc/xnwks/kwtz");
        //教务信息新闻列表
        List<String> academicNewsUrlList = new ArrayList<>();
        //科研项目申报
        academicNewsUrlList.add("http://web.gdei.edu.cn/gdei/xsky/xmsb");
        //学科建设
        academicNewsUrlList.add("http://web.gdei.edu.cn/gdei/xsky/kyxx");
        //科研动态
        academicNewsUrlList.add("http://web.gdei.edu.cn/gdei/xsky/kyxw");
        //行政通知新闻列表
        List<String> adminNewsUrlList = new ArrayList<>();
        //行政通知
        adminNewsUrlList.add("http://web.gdei.edu.cn/gdei/academicinfo/admininfo");
        //学校文件
        adminNewsUrlList.add("http://web.gdei.edu.cn/gdei/academicinfo/fileresult");
        //教学事故
        adminNewsUrlList.add("http://web.gdei.edu.cn/jwc/jwtz/jxsg");
        //综合信息新闻列表
        List<String> schoolNewsUrlList = new ArrayList<>();
        //保卫工作
        schoolNewsUrlList.add("http://web.gdei.edu.cn/gdei/bwc/bwgz");
        //就业工作
        schoolNewsUrlList.add("http://web.gdei.edu.cn/gdei/zsjy/jygz");
        //公示公告通知
        schoolNewsUrlList.add("http://web.gdei.edu.cn/gdei/rsgz/gsgztz");
        list.add(studyNewsUrlList);
        list.add(examinationNewsUrlList);
        list.add(academicNewsUrlList);
        list.add(adminNewsUrlList);
        list.add(schoolNewsUrlList);
        return list;
    }
}
