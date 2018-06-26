package com.linguancheng.gdeiassistant.Service.Wechat;

import com.linguancheng.gdeiassistant.Enum.Wechat.RequestTypeEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.Grade;
import com.linguancheng.gdeiassistant.Pojo.Entity.Schedule;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatArticle;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatBaseMessage;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatImageTextMessage;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatTextMessage;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WechatService {

    private String appid;

    private String appsecret;

    @Autowired
    private RestTemplate restTemplate;

    @Value("#{propertiesReader['wechat.account.appid']}")
    public void setAppid(String appid) {
        this.appid = appid;
    }

    @Value("#{propertiesReader['wechat.account.appsecret']}")
    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    /**
     * 处理微信用户查询请求
     *
     * @param wechatBaseMessage
     * @param requestTypeEnum
     * @param user
     * @return
     */
    public WechatBaseMessage HandleUserRequest(WechatBaseMessage wechatBaseMessage
            , RequestTypeEnum requestTypeEnum, User user) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", user.getUsername());
        params.add("password", user.getPassword());
        params.add("keycode", user.getKeycode());
        params.add("number", user.getNumber());
        switch (requestTypeEnum) {
            case GRADE:
                //查询成绩信息
                GradeQueryJsonResult gradeQueryJsonResult = restTemplate
                        .postForObject("https://www.gdeiassistant.cn/rest/gradequery"
                                , params, GradeQueryJsonResult.class);
                if (gradeQueryJsonResult.isSuccess()) {
                    int term = gradeQueryJsonResult.getCurrentTerm();
                    if (term == 1) {
                        if (!gradeQueryJsonResult.getFirstTermGradeList().isEmpty()) {
                            StringBuilder sb = new StringBuilder("当前学期的成绩：\n");
                            int gradesCount = 0;
                            for (Grade grade : gradeQueryJsonResult.getFirstTermGradeList()) {
                                gradesCount++;
                                sb.append(grade.getGrade_name()).append("  ").append(grade.getGrade_score()).append("\n");
                            }
                            if (gradesCount != 0) {
                                WechatArticle gradeArticle = new WechatArticle();
                                gradeArticle.setTitle("成绩查询结果");
                                gradeArticle.setDescription(sb.toString());
                                gradeArticle.setPicUrl("");
                                gradeArticle.setUrl("https://www.gdeiassistant.cn/grade");
                                List<WechatArticle> wechatArticleList = new ArrayList<>();
                                wechatArticleList.add(gradeArticle);
                                WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
                                wechatImageTextMessage.setArticleCount(1);
                                wechatImageTextMessage.setArticles(wechatArticleList);
                                return wechatBaseMessage;
                            }
                        }
                        WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                        wechatTextMessage.setContent("当前学期没有成绩信息");
                        return wechatTextMessage;
                    } else {
                        if (!gradeQueryJsonResult.getSecondTermGradeList().isEmpty()) {
                            StringBuilder sb = new StringBuilder("当前学期的成绩：\n");
                            int gradesCount = 0;
                            for (Grade grade : gradeQueryJsonResult.getSecondTermGradeList()) {
                                gradesCount++;
                                sb.append(grade.getGrade_name()).append("  ").append(grade.getGrade_score()).append("\n");
                            }
                            if (gradesCount != 0) {
                                WechatArticle gradeArticle = new WechatArticle();
                                gradeArticle.setTitle("成绩查询结果");
                                gradeArticle.setDescription(sb.toString());
                                gradeArticle.setPicUrl("");
                                gradeArticle.setUrl("https://www.gdeiassistant.cn/grade");
                                List<WechatArticle> wechatArticleList = new ArrayList<>();
                                wechatArticleList.add(gradeArticle);
                                WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
                                wechatImageTextMessage.setArticleCount(1);
                                wechatImageTextMessage.setArticles(wechatArticleList);
                                return wechatImageTextMessage;
                            }
                        }
                        WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                        wechatTextMessage.setContent("当前学期没有成绩信息");
                        return wechatTextMessage;
                    }
                }
                //查询成绩信息失败
                return new WechatTextMessage(wechatBaseMessage
                        , "查询失败，错误信息为：" + gradeQueryJsonResult.getErrorMessage());

            case TODAY_SCHEDULE:
                //查询今日课表信息
                ScheduleQueryJsonResult scheduleQueryJsonResult = restTemplate
                        .postForObject("https://www.gdeiassistant.cn/rest/schedulequery"
                                , params, ScheduleQueryJsonResult.class);
                if (scheduleQueryJsonResult.isSuccess()) {
                    List<Schedule> scheduleList = scheduleQueryJsonResult.getScheduleList();
                    int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
                    StringBuilder sb = new StringBuilder("今日的课表：\n");
                    if (!scheduleList.isEmpty()) {
                        int schedulesCount = 0;
                        for (Schedule schedule : scheduleList) {
                            if (schedule.getColumn() + 1 == dayOfWeek) {
                                schedulesCount++;
                                sb.append(schedule.getScheduleLesson()).append("  ").append(schedule.getScheduleName())
                                        .append("  ").append(schedule.getScheduleLocation()).append("\n");
                            }
                        }
                        if (schedulesCount != 0) {
                            WechatArticle scheduleArticle = new WechatArticle();
                            scheduleArticle.setTitle("今日课表查询结果");
                            scheduleArticle.setDescription(sb.toString());
                            scheduleArticle.setPicUrl("");
                            scheduleArticle.setUrl("https://www.gdeiassistant.cn/schedule");
                            List<WechatArticle> wechatArticleList = new ArrayList<>();
                            wechatArticleList.add(scheduleArticle);
                            WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
                            wechatImageTextMessage.setArticleCount(1);
                            wechatImageTextMessage.setArticles(wechatArticleList);
                            return wechatImageTextMessage;
                        }
                    }
                    WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                    wechatTextMessage.setContent("今天没有课程");
                    return wechatTextMessage;
                }
                //查询课表信息失败
                return new WechatTextMessage(wechatBaseMessage
                        , "查询失败，错误信息为：" + scheduleQueryJsonResult.getErrorMessage());

            default:
                //其他请求
                return new WechatTextMessage(wechatBaseMessage, "不支持的服务请求");

        }
    }

    /**
     * 获取访问权限令牌和应用唯一标识
     *
     * @param code
     * @return
     */
    public Map<String, String> GetAccessTokenAndOpenId(String code) {
        Map<String, String> map = new HashMap<>();
        JSONObject jsonObject = restTemplate
                .getForObject("https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                        + appid + "&secret=" + appsecret + "&code=" + code
                        + "&grant_type=authorization_code", JSONObject.class);
        if (jsonObject.has("access_token")) {
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("access_token");
            map.put("access_token", access_token);
            map.put("openid", openid);
            return map;
        }
        return null;
    }

    /**
     * 获取微信用户的统一标识ID
     *
     * @param access_token
     * @return
     */
    public String GetUserUnionID(String access_token, String openid) {
        JSONObject jsonObject = restTemplate
                .getForObject("https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token
                        + "&openid=" + openid, JSONObject.class);
        if (jsonObject.has("unionid")) {
            return jsonObject.getString("unionid");
        }
        return null;
    }
}
