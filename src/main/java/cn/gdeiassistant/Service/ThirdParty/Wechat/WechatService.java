package cn.gdeiassistant.Service.ThirdParty.Wechat;

import cn.gdeiassistant.Enum.Wechat.RequestTypeEnum;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Pojo.Config.WechatOfficialAccountConfig;
import cn.gdeiassistant.Pojo.Entity.CardInfo;
import cn.gdeiassistant.Pojo.Entity.Grade;
import cn.gdeiassistant.Pojo.Entity.Schedule;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import cn.gdeiassistant.Pojo.JSSDK.JSSDKSignature;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import cn.gdeiassistant.Pojo.Wechat.WechatBaseMessage;
import cn.gdeiassistant.Pojo.Wechat.WechatTextMessage;
import cn.gdeiassistant.Repository.Redis.AccessToken.AccessTokenDao;
import cn.gdeiassistant.Service.AcademicAffairs.CardQuery.CardQueryService;
import cn.gdeiassistant.Service.AcademicAffairs.GradeQuery.GradeService;
import cn.gdeiassistant.Service.AcademicAffairs.ScheduleQuery.ScheduleService;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserLoginService;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
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
    private RestTemplate restTemplate;

    @Autowired
    private AccessTokenDao accessTokenDao;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private WechatUserDataService wechatUserDataService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private CardQueryService cardQueryService;

    @Autowired
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
    public WechatBaseMessage HandleUserRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage
            , RequestTypeEnum requestTypeEnum, String contentText, String wechatId) throws Exception {
        String username = wechatUserDataService.QueryWechatAttachUsername(wechatId);
        if (StringUtils.isNotBlank(username)) {
            //已绑定微信账号，获取微信ID绑定的用户账号
            User user = wechatUserDataService.QueryWechatUserData(username);
            if (user != null) {
                switch (requestTypeEnum) {
                    case CARD:
                        //查询校园卡基本信息
                        return HandleCardInfoQueryRequest(request, wechatBaseMessage, user);

                    case GRADE:
                        //查询学期成绩信息
                        return HandleGradeQueryRequest(request, wechatBaseMessage, user);

                    case TODAY_SCHEDULE:
                        //查询今日课表信息
                        return HandleTodayScheduleRequest(request, wechatBaseMessage, user);

                    case ATTACH:
                        //更改绑定账号
                        return HandleAttachRequest(request, wechatId, contentText, wechatBaseMessage);

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
            return HandleAttachRequest(request, wechatId, contentText, wechatBaseMessage);
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
    public Map<String, String> GetAccessTokenAndOpenId(String code) {
        Map<String, String> map = new HashMap<>();
        JSONObject jsonObject = restTemplate
                .getForObject("https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                        + wechatOfficialAccountConfig.getAppid() + "&secret="
                        + wechatOfficialAccountConfig.getAppsecret() + "&code=" + code
                        + "&grant_type=authorization_code", JSONObject.class);
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
    protected synchronized String GetWechatAccessToken() {
        //检查Redis缓存中有无AccessToken
        String accessToken = accessTokenDao.QueryWechatAccessToken();
        //若缓存中没有AccessToken则调用API数据接口
        if (StringUtils.isBlank(accessToken)) {
            JSONObject jsonObject = restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/token?" +
                    "grant_type=client_credential&appid=" + wechatOfficialAccountConfig.getAppid() +
                    "&secret=" + wechatOfficialAccountConfig.getAppsecret(), JSONObject.class);
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
    private String GetWechatJSApiTicket(String accessToken) {
        JSONObject jsonObject = restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/ticket/getticket?" +
                "access_token=" + accessToken + "&type=jsapi", JSONObject.class);
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
    public JSSDKSignature SetUpJSSDKConfig(String url) {
        String accessToken = GetWechatAccessToken();
        String ticket = GetWechatJSApiTicket(accessToken);
        //生成随机数
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        //获取时间戳
        Long timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        String signature = StringEncryptUtils.SHA1HexString("jsapi_ticket=" + ticket + "&noncestr=" + nonce + "&timestamp=" + timestamp + "&url=" + url);
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
    public InputStream DownloadWechatVoiceRecord(String voiceId) {
        String accessToken = GetWechatAccessToken();
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange("https://api.weixin.qq.com/cgi-bin/media/get/jssdk?" +
                "access_token=" + accessToken + "&media_id=" + voiceId, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), byte[].class);
        if (responseEntity.getHeaders().get("Content-Type").get(0).equals("voice/speex")) {
            byte[] bytes = responseEntity.getBody();
            return new ByteArrayInputStream(bytes);
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
    private WechatBaseMessage HandleCardInfoQueryRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
        //用户登录
        userLoginService.UserLogin(request.getSession().getId(), user.getUsername(), user.getPassword());
        CardInfo cardInfo = cardQueryService.CardInfoQuery(request.getSession().getId());
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
    private WechatBaseMessage HandleGradeQueryRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        //用户登录
        userLoginService.UserLogin(request.getSession().getId(), user.getUsername(), user.getPassword());
        //查询成绩信息
        GradeQueryResult gradeQueryResult = gradeService.QueryGrade(request.getSession().getId(), null);
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
    private WechatBaseMessage HandleTodayScheduleRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        //用户登录
        userLoginService.UserLogin(request.getSession().getId(), user.getUsername(), user.getPassword());
        //课表查询
        ScheduleQueryResult scheduleQueryResult = scheduleService.QuerySchedule(request.getSession().getId(), null);
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
    private WechatBaseMessage HandleAttachRequest(HttpServletRequest request, String wechatId
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
                    //进行用户登录
                    userLoginService.UserLogin(request.getSession().getId(), username, password);
                    //登录成功，进行微信账号绑定
                    wechatUserDataService.SyncWechatUserData(username, wechatId);
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
}
