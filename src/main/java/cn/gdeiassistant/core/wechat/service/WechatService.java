package cn.gdeiassistant.core.wechat.service;

import cn.gdeiassistant.common.enums.Wechat.RequestTypeEnum;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.pojo.Config.WechatOfficialAccountConfig;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import cn.gdeiassistant.common.pojo.Entity.Grade;
import cn.gdeiassistant.common.pojo.Entity.Schedule;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.gradequery.pojo.GradeQueryResult;
import cn.gdeiassistant.common.pojo.JSSDK.JSSDKSignature;
import cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult;
import cn.gdeiassistant.core.wechat.pojo.WechatBaseMessage;
import cn.gdeiassistant.core.wechat.pojo.WechatTextMessage;
import cn.gdeiassistant.common.redis.AccessToken.AccessTokenDao;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import cn.gdeiassistant.core.cardquery.service.CardQueryService;
import cn.gdeiassistant.core.gradequery.service.GradeService;
import cn.gdeiassistant.core.schedulequery.service.ScheduleService;
import cn.gdeiassistant.integration.wechat.WechatApiClient;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WechatService {

    @Autowired
    private WechatApiClient wechatApiClient;

    @Autowired
    private AccessTokenDao accessTokenDao;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private cn.gdeiassistant.core.wechat.service.WechatUserDataService wechatUserDataService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CardQueryService cardQueryService;

    @Autowired(required = false)
    private WechatOfficialAccountConfig wechatOfficialAccountConfig;

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
    public WechatBaseMessage handleUserRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage
            , RequestTypeEnum requestTypeEnum, String contentText, String wechatId) throws Exception {
        String username = wechatUserDataService.queryWechatAttachUsername(wechatId);
        if (StringUtils.isNotBlank(username)) {
            //已绑定微信账号，获取微信ID绑定的用户账号
            User user = wechatUserDataService.queryWechatUserData(username);
            if (user != null) {
                switch (requestTypeEnum) {
                    case CARD:
                        //查询校园卡基本信息
                        return handleCardInfoQueryRequest(request, wechatBaseMessage, user);

                    case GRADE:
                        //查询学期成绩信息
                        return handleGradeQueryRequest(request, wechatBaseMessage, user);

                    case TODAY_SCHEDULE:
                        //查询今日课表信息
                        return handleTodayScheduleRequest(request, wechatBaseMessage, user);

                    case ATTACH:
                        //更改绑定账号
                        return handleAttachRequest(request, wechatId, contentText, wechatBaseMessage);

                    default:
                        //不支持的请求
                        return new WechatTextMessage(wechatBaseMessage, "不支持的服务请求");
                }
            }
            return new WechatTextMessage(wechatBaseMessage, "你未绑定微信账号，请发送如下格式文本" +
                    "进行绑定：绑定账号-用户名-密码。例：绑定账号-gdeiassistant-123456");
        }
        if (requestTypeEnum == RequestTypeEnum.ATTACH) {
            //绑定账号
            return handleAttachRequest(request, wechatId, contentText, wechatBaseMessage);
        }
        return new WechatTextMessage(wechatBaseMessage, "你未绑定微信账号，请发送如下格式文本" +
                "进行绑定：绑定账号-用户名-密码。例：绑定账号-gdeiassistant-123456");
    }

    /**
     * 使用OAuth2.0授权码获取访问权限令牌和用户唯一标识
     *
     * @param code
     * @return
     */
    public Map<String, String> getAccessTokenAndOpenId(String code) {
        Map<String, String> map = new HashMap<>();
        String json = wechatApiClient.getOauth2AccessTokenJson(
                wechatOfficialAccountConfig.getAppid(),
                wechatOfficialAccountConfig.getAppsecret(),
                code
        );
        JSONObject jsonObject = JSONObject.parseObject(json);
        if (jsonObject.containsKey("access_token")) {
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("access_token");
            map.put("access_token", access_token);
            map.put("openid", openid);
            return map;
        }
        return null;
    }

    /**
     * 获取微信公众号AccessToken
     *
     * @return
     */
    protected synchronized String getWechatAccessToken() {
        //检查Redis缓存中有无AccessToken
        String accessToken = accessTokenDao.QueryWechatAccessToken();
        //若缓存中没有AccessToken则调用API数据接口
        if (StringUtils.isBlank(accessToken)) {
            String json = wechatApiClient.getAccessTokenJson(
                    wechatOfficialAccountConfig.getAppid(),
                    wechatOfficialAccountConfig.getAppsecret()
            );
            JSONObject jsonObject = JSONObject.parseObject(json);
            if (jsonObject.containsKey("access_token")) {
                accessTokenDao.SaveWechatAccessToken(jsonObject.getString("access_token"));
                return jsonObject.getString("access_token");
            }
        }
        return accessToken;
    }


    /**
     * 获取微信JS接口的临时票据
     *
     * @param accessToken
     * @return
     */
    private String getWechatJsApiTicket(String accessToken) {
        String json = wechatApiClient.getJsApiTicketJson(accessToken);
        JSONObject jsonObject = JSONObject.parseObject(json);
        if (jsonObject.containsKey("ticket")) {
            return jsonObject.getString("ticket");
        }
        return null;
    }

    /**
     * 生成JS-SDK权限验证的签名
     *
     * @param url
     * @return
     */
    public JSSDKSignature setUpJssdkConfig(String url) {
        String accessToken = getWechatAccessToken();
        String ticket = getWechatJsApiTicket(accessToken);
        //生成随机数
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        //获取时间戳
        Long timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        String signature = StringEncryptUtils.sha1HexString("jsapi_ticket=" + ticket + "&noncestr=" + nonce + "&timestamp=" + timestamp + "&url=" + url);
        JSSDKSignature jssdkSignature = new JSSDKSignature();
        jssdkSignature.setNonceStr(nonce);
        jssdkSignature.setSignature(signature);
        jssdkSignature.setTimestamp(timestamp);
        return jssdkSignature;
    }

    /**
     * 下载JSSDK上传的音频
     *
     * @param voiceId
     * @return
     */
    public InputStream downloadWechatVoiceRecord(String voiceId) {
        String accessToken = getWechatAccessToken();
        byte[] bytes = wechatApiClient.downloadJssdkVoice(accessToken, voiceId);
        return bytes != null ? new ByteArrayInputStream(bytes) : null;
    }

    /**
     * 获取微信用户的统一标识ID
     *
     * @param access_token
     * @return
     */
    public String getUserUnionId(String access_token, String openid) {
        String json = wechatApiClient.getUserInfoJson(access_token, openid);
        JSONObject jsonObject = JSONObject.parseObject(json);
        if (jsonObject.containsKey("unionid")) {
            return jsonObject.getString("unionid");
        }
        return null;
    }

    /**
     * 处理校园卡信息查询请求
     *
     * @param request
     * @param user
     * @return
     */
    private WechatBaseMessage handleCardInfoQueryRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        String sessionId = getSessionIdFromRequest(request);
        WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
        //用户登录
        userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
        CardInfo cardInfo = cardQueryService.cardInfoQuery(sessionId);
        if (cardInfo != null) {
            String content = "基本信息：\n" +
                    "姓名：" + cardInfo.getName() + "\n" +
                    "学号：" + cardInfo.getNumber() + "\n" +
                    "卡号：" + cardInfo.getCardNumber() + "\n" +
                    "\n余额信息：\n" +
                    "余额：" + cardInfo.getCardBalance() + "元\n" +
                    "过渡余额：" + cardInfo.getCardInterimBalance() + "元\n" +
                    "\n状态信息：\n" +
                    "冻结状态：" + cardInfo.getCardFreezeState() + "\n" +
                    "挂失状态：" + cardInfo.getCardLostState();
            wechatTextMessage.setContent(content);
        } else {
            wechatTextMessage.setContent("没有查询到校园卡信息");
        }
        /*wechatTextMessage.setContent(stringBuilder);
        WechatArticle cardArticle = new WechatArticle();
        cardArticle.setTitle("校园卡信息查询结果");
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
        cardArticle.setDescription(stringBuilder);
        cardArticle.setPicUrl("");
        cardArticle.setUrl("https://gdeiassistant.cn/cardinfo");
        List<WechatArticle> wechatArticleList = new ArrayList<>();
        wechatArticleList.add(cardArticle);
        WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
        wechatImageTextMessage.setArticleCount(1);
        wechatImageTextMessage.setArticles(wechatArticleList);*/
        return wechatTextMessage;
    }

    /**
     * 处理成绩查询请求
     *
     * @param request
     * @param wechatBaseMessage
     * @param user
     * @return
     * @throws Exception
     */
    private WechatBaseMessage handleGradeQueryRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        String sessionId = getSessionIdFromRequest(request);
        userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
        GradeQueryResult gradeQueryResult = gradeService.queryGrade(sessionId, null);
        int term = gradeQueryResult.getSecondTermGradeList().size() == 0 ? 1 : 2;
        if (term == 1) {
            if (!gradeQueryResult.getFirstTermGradeList().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int gradesCount = 0;
                for (Grade grade : gradeQueryResult.getFirstTermGradeList()) {
                    gradesCount++;
                    sb.append(grade.getGradeName()).append("  ").append(grade.getGradeScore()).append("\n");
                }
                if (gradesCount != 0) {
                    WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                    wechatTextMessage.setContent(sb.toString());
                        /*WechatArticle gradeArticle = new WechatArticle();
                        gradeArticle.setTitle(gradeQueryResult.getFirstTermGradeList()
                                .get(0).getGradeYear() + "学年第一学期成绩查询结果");
                        gradeArticle.setDescription(sb.toString());
                        gradeArticle.setPicUrl("");
                        gradeArticle.setUrl("https://gdeiassistant.cn/grade");
                        List<WechatArticle> wechatArticleList = new ArrayList<>();
                        wechatArticleList.add(gradeArticle);
                        WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
                        wechatImageTextMessage.setArticleCount(1);
                        wechatImageTextMessage.setArticles(wechatArticleList);*/
                    return wechatTextMessage;
                }
            }
            return new WechatTextMessage(wechatBaseMessage
                    , "当前学期没有成绩信息");
        } else {
            if (!gradeQueryResult.getSecondTermGradeList().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                int gradesCount = 0;
                for (Grade grade : gradeQueryResult.getSecondTermGradeList()) {
                    gradesCount++;
                    sb.append(grade.getGradeName()).append("  ").append(grade.getGradeScore()).append("\n");
                }
                if (gradesCount != 0) {
                    WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                    wechatTextMessage.setContent(sb.toString());
                        /*WechatArticle gradeArticle = new WechatArticle();
                        gradeArticle.setTitle(gradeQueryResult.getSecondTermGradeList()
                                .get(0).getGradeYear() + "学年第二学期成绩查询结果");
                        gradeArticle.setDescription(sb.toString());
                        gradeArticle.setPicUrl("");
                        gradeArticle.setUrl("https://gdeiassistant.cn/grade");
                        List<WechatArticle> wechatArticleList = new ArrayList<>();
                        wechatArticleList.add(gradeArticle);
                        WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
                        wechatImageTextMessage.setArticleCount(1);
                        wechatImageTextMessage.setArticles(wechatArticleList);*/
                    return wechatTextMessage;
                }
            }
            return new WechatTextMessage(wechatBaseMessage
                    , "当前学期没有成绩信息");
        }
    }

    /**
     * 处理今日课表查询请求
     *
     * @param request
     * @param wechatBaseMessage
     * @param user
     * @return
     * @throws Exception
     */
    private WechatBaseMessage handleTodayScheduleRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        String sessionId = getSessionIdFromRequest(request);
        userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
        ScheduleQueryResult scheduleQueryResult = scheduleService.querySchedule(sessionId, null);
        List<Schedule> scheduleList = scheduleQueryResult.getScheduleList();
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
                WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                wechatTextMessage.setContent(sb.toString());
                /*WechatArticle scheduleArticle = new WechatArticle();
                scheduleArticle.setTitle("今日课表查询结果");
                scheduleArticle.setDescription(sb.toString());
                scheduleArticle.setPicUrl("");
                scheduleArticle.setUrl("https://www.gdeiassistant.cn/schedule");
                List<WechatArticle> wechatArticleList = new ArrayList<>();
                wechatArticleList.add(scheduleArticle);
                WechatImageTextMessage wechatImageTextMessage = new WechatImageTextMessage(wechatBaseMessage);
                wechatImageTextMessage.setArticleCount(1);
                wechatImageTextMessage.setArticles(wechatArticleList);*/
                return wechatTextMessage;
            }
        }
        return new WechatTextMessage(wechatBaseMessage
                , "今天没有课程");
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
    private WechatBaseMessage handleAttachRequest(HttpServletRequest request, String wechatId
            , String contentText, WechatBaseMessage wechatBaseMessage) throws Exception {
        //检测用户发送的文本内容合法性
        if (contentText.split("-").length >= 3) {
            String username = contentText.split("-")[1];
            String password = null;
            if (contentText.split("-").length > 3) {
                String[] arrays = contentText.split("-");
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
                try {
                    String sessionId = getSessionIdFromRequest(request);
                    userLoginService.userLogin(sessionId, username, password);
                    //登录成功，进行微信账号绑定
                    wechatUserDataService.syncWechatUserData(username, wechatId);
                    return new WechatTextMessage(wechatBaseMessage, "绑定微信账号成功");
                } catch (PasswordIncorrectException e) {
                    //密码错误
                    return new WechatTextMessage(wechatBaseMessage, "账号密码错误，请检查并重试");
                }
            }
        }
        return new WechatTextMessage(wechatBaseMessage, "发送的文本格式有误，请检查并重试！" +
                "绑定账号文本格式：绑定账号-用户名-密码。例：绑定账号-gdeiassistant-123456");
    }

    /**
     * 从请求获取 sessionId：JWT Filter 注入的 attribute；微信回调等无 JWT 场景使用请求级 UUID。
     */
    private String getSessionIdFromRequest(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return (sessionId != null && !sessionId.isEmpty()) ? sessionId : UUID.randomUUID().toString();
    }
}
