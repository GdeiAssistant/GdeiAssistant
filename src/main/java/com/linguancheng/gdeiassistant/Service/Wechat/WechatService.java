package com.linguancheng.gdeiassistant.Service.Wechat;

import com.linguancheng.gdeiassistant.Enum.Base.AttachResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Enum.Wechat.RequestTypeEnum;
import com.linguancheng.gdeiassistant.Pojo.CardQuery.CardInfoQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.Grade;
import com.linguancheng.gdeiassistant.Pojo.Entity.Schedule;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatArticle;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatBaseMessage;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatImageTextMessage;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatTextMessage;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
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

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private WechatUserDataService wechatUserDataService;

    /**
     * 处理微信用户请求
     *
     * @param request
     * @param wechatBaseMessage
     * @param requestTypeEnum
     * @param contentText
     * @param wechatId
     * @return
     */
    public WechatBaseMessage HandleUserRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage
            , RequestTypeEnum requestTypeEnum, String contentText, String wechatId) {
        BaseResult<String, AttachResultEnum> checkWechatAttachStateResult = wechatUserDataService
                .CheckWechatAttachState(wechatId);
        switch (checkWechatAttachStateResult.getResultType()) {
            case ATTACHED:
                //已绑定微信账号，获取微信ID绑定的用户账号
                BaseResult<User, BoolResultEnum> queryWechatUserDataResult = wechatUserDataService
                        .QueryWechatUserData(checkWechatAttachStateResult.getResultData());
                switch (queryWechatUserDataResult.getResultType()) {
                    case SUCCESS:
                        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                        params.add("username", queryWechatUserDataResult.getResultData().getUsername());
                        params.add("password", queryWechatUserDataResult.getResultData().getPassword());
                        params.add("keycode", queryWechatUserDataResult.getResultData().getKeycode());
                        params.add("number", queryWechatUserDataResult.getResultData().getNumber());
                        switch (requestTypeEnum) {
                            case CARD:
                                //查询校园卡基本信息
                                CardInfoQueryJsonResult cardInfoQueryJsonResult = restTemplate
                                        .postForObject("https://www.gdeiassistant.cn/rest/cardinfo"
                                                , params, CardInfoQueryJsonResult.class);
                                if (cardInfoQueryJsonResult.isSuccess()) {
                                    CardInfo cardInfo = cardInfoQueryJsonResult.getCardInfo();
                                    WechatArticle scheduleArticle = new WechatArticle();
                                    scheduleArticle.setTitle("校园卡信息查询结果");
                                    String stringBuilder = "基本信息：\n" +
                                            "姓名：" + cardInfo.getName() + "\n" +
                                            "学号：" + cardInfo.getNumber() + "\n" +
                                            "卡号：" + cardInfo.getCardNumber() + "\n" +
                                            "\n余额信息：\n" +
                                            "余额：" + cardInfo.getCardBalance() + "元\n" +
                                            "过渡余额：" + cardInfo.getCardInterimBalance() + "元\n" +
                                            "\n状态信息：\n" +
                                            "冻结状态：" + cardInfo.getCardFreezeState() + "\n" +
                                            "挂失状态：" + cardInfo.getCardLostState();
                                    scheduleArticle.setDescription(stringBuilder);
                                    scheduleArticle.setPicUrl("");
                                    scheduleArticle.setUrl("https://www.gdeiassistant.cn/cardinfo");
                                    List<WechatArticle> wechatArticleList = new ArrayList<>();
                                    wechatArticleList.add(scheduleArticle);
                                    WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
                                    wechatImageTextMessage.setArticleCount(1);
                                    wechatImageTextMessage.setArticles(wechatArticleList);
                                    return wechatImageTextMessage;
                                }
                                //查询校园卡基本信息失败
                                return new WechatTextMessage(wechatBaseMessage
                                        , "查询失败，错误信息为：" + cardInfoQueryJsonResult.getErrorMessage());

                            case GRADE:
                                //查询成绩信息
                                GradeQueryJsonResult gradeQueryJsonResult = restTemplate
                                        .postForObject("https://www.gdeiassistant.cn/rest/gradequery"
                                                , params, GradeQueryJsonResult.class);
                                if (gradeQueryJsonResult.isSuccess()) {
                                    int term = gradeQueryJsonResult.getSecondTermGradeList().size() == 0 ? 1 : 2;
                                    if (term == 1) {
                                        if (!gradeQueryJsonResult.getFirstTermGradeList().isEmpty()) {
                                            StringBuilder sb = new StringBuilder();
                                            int gradesCount = 0;
                                            for (Grade grade : gradeQueryJsonResult.getFirstTermGradeList()) {
                                                gradesCount++;
                                                sb.append(grade.getGrade_name()).append("  ").append(grade.getGrade_score()).append("\n");
                                            }
                                            if (gradesCount != 0) {
                                                WechatArticle gradeArticle = new WechatArticle();
                                                gradeArticle.setTitle(gradeQueryJsonResult.getFirstTermGradeList()
                                                        .get(0).getGrade_year() + "学年第一学期成绩查询结果");
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
                                    } else {
                                        if (!gradeQueryJsonResult.getSecondTermGradeList().isEmpty()) {
                                            StringBuilder sb = new StringBuilder();
                                            int gradesCount = 0;
                                            for (Grade grade : gradeQueryJsonResult.getSecondTermGradeList()) {
                                                gradesCount++;
                                                sb.append(grade.getGrade_name()).append("  ").append(grade.getGrade_score()).append("\n");
                                            }
                                            if (gradesCount != 0) {
                                                WechatArticle gradeArticle = new WechatArticle();
                                                gradeArticle.setTitle(gradeQueryJsonResult.getSecondTermGradeList()
                                                        .get(0).getGrade_year() + "学年第二学期成绩查询结果");
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

                            case ATTACH:
                                //更改绑定账号
                                return HandleAttachRequest(request, wechatId, contentText, wechatBaseMessage);

                            default:
                                //其他请求
                                return new WechatTextMessage(wechatBaseMessage, "不支持的服务请求");
                        }

                    case ERROR:
                    default:
                        return new WechatTextMessage(wechatBaseMessage, "服务器维护中，请稍候再试");
                }

            case NOT_ATTACHED:
                //未绑定微信账号
                switch (requestTypeEnum) {
                    case ATTACH:
                        return HandleAttachRequest(request, wechatId, contentText, wechatBaseMessage);

                    default:
                        return new WechatTextMessage(wechatBaseMessage, "你未绑定微信账号，请发送如下格式文本" +
                                "进行绑定：绑定账号-教务系统账号用户名-教务系统账号密码。例：绑定账号-lisiyi-123456");
                }

            case SERVER_ERROR:
            default:
                return new WechatTextMessage(wechatBaseMessage, "服务器维护中，请稍候再试");
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

    /**
     * 处理绑定账号请求
     *
     * @param request
     * @param wechatId
     * @param contentText
     * @param wechatBaseMessage
     * @return
     */
    private WechatBaseMessage HandleAttachRequest(HttpServletRequest request, String wechatId
            , String contentText, WechatBaseMessage wechatBaseMessage) {
        //检测用户发送的文本内容合法性
        if (contentText.split("-").length >= 3) {
            String username = contentText.split("-")[1];
            String password = null;
            if (contentText.split("-").length > 3) {
                String arrays[] = contentText.split("-");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 2; i < arrays.length; i++) {
                    if (i != arrays.length - 1) {
                        stringBuilder.append(arrays[i]).append("-");
                    } else {
                        stringBuilder.append(arrays[i]);
                    }
                }
                password = stringBuilder.toString();
            } else {
                password = contentText.split("-")[2];
            }
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)
                    && username.length() <= 20 && password.length() <= 35) {
                //进行用户登录
                BaseResult<UserCertificate, LoginResultEnum> userLoginResult = userLoginService.UserLogin(request
                        , new User(username, password), true);
                switch (userLoginResult.getResultType()) {
                    case LOGIN_SUCCESS:
                        //登录成功，进行微信账号绑定
                        if (wechatUserDataService.SyncWechatUserData(username, wechatId)) {
                            return new WechatTextMessage(wechatBaseMessage, "绑定微信账号成功");
                        }
                        return new WechatTextMessage(wechatBaseMessage, "服务器维护中，请稍候再试");

                    case PASSWORD_ERROR:
                        //密码错误
                        return new WechatTextMessage(wechatBaseMessage, "账号密码错误，请检查并重试");

                    case SERVER_ERROR:
                    case TIME_OUT:
                    default:
                        //服务器异常
                        return new WechatTextMessage(wechatBaseMessage, "服务器维护中，请稍候再试");
                }
            }
        }
        return new WechatTextMessage(wechatBaseMessage, "发送的文本格式有误，请检查并重试！" +
                "绑定账号文本格式：绑定账号-教务系统账号用户名-教务系统账号密码。例：绑定账号-lisiyi-123456");
    }
}
