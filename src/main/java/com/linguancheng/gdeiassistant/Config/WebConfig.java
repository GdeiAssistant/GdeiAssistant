package com.linguancheng.gdeiassistant.Config;

import com.linguancheng.gdeiassistant.Converter.HttpMessageConvert.WeChatMappingJackson2HttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

    /**
     * RestTemplate
     *
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new WeChatMappingJackson2HttpMessageConverter());
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
        return asyncRestTemplate;
    }

    /**
     * 登录拦截器不进行拦截的URL列表
     *
     * @return
     */
    @Bean
    public List<String> loginInterceptorExceptionList() {
        List<String> loginInterceptorExceptionList = new ArrayList<>();
        //退出账号
        loginInterceptorExceptionList.add("/logout");
        //登录账号
        loginInterceptorExceptionList.add("/login");
        //用户登录接口
        loginInterceptorExceptionList.add("/api/userlogin");
        //协议与政策
        loginInterceptorExceptionList.add("/agreement");
        loginInterceptorExceptionList.add("/policy");
        loginInterceptorExceptionList.add("/license");
        //软件说明
        loginInterceptorExceptionList.add("/about");
        //Restful API
        loginInterceptorExceptionList.add("/rest");
        //微信API接口
        loginInterceptorExceptionList.add("/wechat");
        //易班API接口
        loginInterceptorExceptionList.add("/yiban");
        return loginInterceptorExceptionList;
    }

//    <!-- 校园新闻系统地址 -->
//    <util:list id="newsUrlsList" value-type="java.lang.String">
//        <!-- 学生工作 -->
//        <value>http://web.gdei.edu.cn/gdei/stuwork/stuadmin</value>
//        <!-- 后勤工作 -->
//        <value>http://web.gdei.edu.cn/gdei/hqserv/ggxx</value>
//        <!-- 选课工作 -->
//        <value>http://web.gdei.edu.cn/jwc/jwtz/xkgz</value>
//        <!-- 调停课通知 -->
//        <value>http://web.gdei.edu.cn/jwc/jwtz/tktz</value>
//        <!-- 等级竞赛通知 -->
//        <value>http://web.gdei.edu.cn/jwc/jwtz/kwtz</value>
//    </util:list>

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

    /**
     * 个人资料性别字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> genderMap() {
        Map<Integer, String> genderMap = new HashMap<>();
        genderMap.put(0, "未选择");
        genderMap.put(1, "男");
        genderMap.put(2, "女");
        genderMap.put(3, "自定义");
        return genderMap;
    }

    /**
     * 个人资料性取向字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> genderOrientationMap() {
        Map<Integer, String> genderOrientationMap = new HashMap<>();
        genderOrientationMap.put(0, "未选择");
        genderOrientationMap.put(1, "异性恋");
        genderOrientationMap.put(2, "同性恋");
        genderOrientationMap.put(3, "双性恋");
        genderOrientationMap.put(4, "无性恋");
        genderOrientationMap.put(5, "泛性恋");
        genderOrientationMap.put(6, "其他");
        return genderOrientationMap;
    }

    /**
     * 院系字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> facultyMap() {
        Map<Integer, String> facultyMap = new HashMap<>();
        facultyMap.put(0, "未选择");
        facultyMap.put(1, "教育学院");
        facultyMap.put(2, "政法系");
        facultyMap.put(3, "中文系");
        facultyMap.put(4, "数学系");
        facultyMap.put(5, "外语系");
        facultyMap.put(6, "物理与信息工程系");
        facultyMap.put(7, "化学系");
        facultyMap.put(8, "生物与食品工程学院");
        facultyMap.put(9, "体育学院");
        facultyMap.put(10, "美术学院");
        facultyMap.put(11, "计算机科学系");
        facultyMap.put(12, "音乐系");
        facultyMap.put(13, "教师研修学院");
        facultyMap.put(14, "成人教育学院");
        facultyMap.put(15, "网络教育学院");
        facultyMap.put(16, "马克思主义学院");
        return facultyMap;
    }
}
