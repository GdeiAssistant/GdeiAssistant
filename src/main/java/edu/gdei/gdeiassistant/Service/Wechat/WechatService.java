package edu.gdei.gdeiassistant.Service.Wechat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Enum.Wechat.RequestTypeEnum;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import edu.gdei.gdeiassistant.Pojo.JSSDK.JSSDKSignature;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import edu.gdei.gdeiassistant.Pojo.Wechat.WechatBaseMessage;
import edu.gdei.gdeiassistant.Pojo.Wechat.WechatTextMessage;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantData.Mapper.Reading.ReadingMapper;
import edu.gdei.gdeiassistant.Repository.Redis.AccessToken.AccessTokenDao;
import edu.gdei.gdeiassistant.Service.CardQuery.CardQueryService;
import edu.gdei.gdeiassistant.Service.GradeQuery.GradeService;
import edu.gdei.gdeiassistant.Service.ScheduleQuery.ScheduleService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WechatService {

    private Log log = LogFactory.getLog(WechatService.class);

    private String appid;

    private String appsecret;

    @Value("#{propertiesReader['wechat.account.appid']}")
    public void setAppid(String appid) {
        this.appid = appid;
    }

    @Value("#{propertiesReader['wechat.account.appsecret']}")
    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    @Autowired
    private Environment environment;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AccessTokenDao accessTokenDao;

    @Autowired
    private ReadingMapper readingMapper;

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
            //已绑定微信账号，获取微信ID绑定的用户账�?
            User user = wechatUserDataService.QueryWechatUserData(username);
            if (user != null) {
                switch (requestTypeEnum) {
                    case CARD:
                        //查询校园卡基本信�?
                        return HandleCardInfoQueryRequest(request, wechatBaseMessage, user);

                    case GRADE:
                        //查询学期成绩信息
                        return HandleGradeQueryRequest(wechatBaseMessage, user);

                    case TODAY_SCHEDULE:
                        //查询今日课表信息
                        return HandleTodayScheduleRequest(wechatBaseMessage, user);

                    case ATTACH:
                        //更改绑定账号
                        return HandleAttachRequest(request, wechatId, contentText, wechatBaseMessage);

                    default:
                        //不支持的请求
                        return new WechatTextMessage(wechatBaseMessage, "不支持的服务请求");
                }
            }
            return new WechatTextMessage(wechatBaseMessage, "你未绑定微信账号，请发�?�如下格式文�?" +
                    "进行绑定：绑定账�?-用户�?-密码。例：绑定账�?-gdeiassistant-123456");
        }
        if (requestTypeEnum == RequestTypeEnum.ATTACH) {
            //绑定账号
            return HandleAttachRequest(request, wechatId, contentText, wechatBaseMessage);
        }
        return new WechatTextMessage(wechatBaseMessage, "你未绑定微信账号，请发�?�如下格式文�?" +
                "进行绑定：绑定账�?-用户�?-密码。例：绑定账�?-gdeiassistant-123456");
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
                        + appid + "&secret=" + appsecret + "&code=" + code
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
    private synchronized String GetWechatAccessToken() {
        //�?查Redis缓存中有无AccessToken
        String accessToken = accessTokenDao.QueryWechatAccessToken();
        //若缓存中没有AccessToken则调用API数据接口
        if (StringUtils.isBlank(accessToken)) {
            JSONObject jsonObject = restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/token?" +
                    "grant_type=client_credential&appid=" + appid + "&secret=" + appsecret, JSONObject.class);
            if (jsonObject.containsKey("access_token")) {
                accessTokenDao.SaveWechatAccessToken(jsonObject.getString("access_token"));
                return jsonObject.getString("access_token");
            }
        }
        return accessToken;
    }


    /**
     * 获取微信JS接口的临时票�?
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
     * 生成JS-SDK权限验证的签�?
     *
     * @param url
     * @return
     */
    public JSSDKSignature SetUpJSSDKConfig(String url) {
        String accessToken = GetWechatAccessToken();
        String ticket = GetWechatJSApiTicket(accessToken);
        //生成随机�?
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        //获取时间�?
        Long timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond();
        String signature = StringEncryptUtils.SHA1HexString("jsapi_ticket=" + ticket + "&noncestr=" + nonce + "&timestamp=" + timestamp + "&url=" + url);
        JSSDKSignature jssdkSignature = new JSSDKSignature();
        jssdkSignature.setNonceStr(nonce);
        jssdkSignature.setSignature(signature);
        jssdkSignature.setTimestamp(timestamp);
        return jssdkSignature;
    }

    /**
     * 下载JSSDK上传的音�?
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
     * 定时同步微信专题阅读素材
     *
     * @return
     */
    @Scheduled(fixedDelay = 21600000)
    @Transactional("dataTransactionManager")
    public void SyncWechatReadingItem() {
        log.info(LocalDateTime.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy年MM月dd�? HH:mm:ss")) + "启动了同步微信专题阅读素材的任务");
        String accessToken = GetWechatAccessToken();
        //获取专题阅读素材总数
        JSONObject jsonObject = restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/material/get_materialcount?access_token=" + accessToken, JSONObject.class);
        int count = 0;
        if (jsonObject.containsKey("news_count")) {
            count = jsonObject.getInteger("news_count");
        }
        int page = count % 20 == 0 ? (count / 20) : (count / 20 + 1);
        for (int i = 0; i < page; i++) {
            JSONObject params = new JSONObject();
            params.put("type", "news");
            params.put("offset", String.valueOf(i));
            params.put("count", "20");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            JSONObject result = restTemplate.postForObject("https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=" + accessToken
                    , new HttpEntity<>(params, httpHeaders), JSONObject.class);
            if (result.containsKey("item")) {
                JSONArray items = result.getJSONArray("item");
                for (int j = 0; j < items.size(); j++) {
                    Reading reading = new Reading();
                    reading.setId(items.getJSONObject(j).getString("media_id"));
                    reading.setTitle(items.getJSONObject(j).getJSONObject("content")
                            .getJSONArray("news_item").getJSONObject(0).getString("title"));
                    reading.setDescription(items.getJSONObject(j).getJSONObject("content")
                            .getJSONArray("news_item").getJSONObject(0).getString("digest"));
                    reading.setLink(items.getJSONObject(j).getJSONObject("content")
                            .getJSONArray("news_item").getJSONObject(0).getString("url"));
                    reading.setCreateTime(Date.from(Instant.ofEpochSecond(items.getJSONObject(j).getJSONObject("content")
                            .getLongValue("create_time"))));
                    if (readingMapper.selectReadingById(items.getJSONObject(j).getString("media_id")) == null) {
                        readingMapper.insertReading(reading);
                    } else {
                        readingMapper.updateReading(reading);
                    }
                }
            }
        }
    }

    /**
     * 获取微信用户的统�?标识ID
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
     * 处理校园卡信息查询请�?
     *
     * @param request
     * @param user
     * @return
     */
    private WechatBaseMessage HandleCardInfoQueryRequest(HttpServletRequest request, WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
        Integer group = userLoginService.GetUserByUsername(user.getUsername()).getGroup();
        CardInfo cardInfo = null;
        if (UserGroupEnum.TRIAL.getValue().equals(group)) {
            DataJsonResult<CardInfo> result = new Gson().fromJson(environment.getProperty("trial.data.cardinfo")
                    , new TypeToken<DataJsonResult<CardInfo>>() {
                    }.getType());
            if (result != null) {
                cardInfo = result.getData();
            }
        } else {
            cardInfo = cardQueryService.CardInfoQuery(request.getSession().getId(), user.getUsername(), user.getPassword());
        }
        if (cardInfo != null) {
            String content = "基本信息：\n" +
                    "姓名�?" + cardInfo.getName() + "\n" +
                    "学号�?" + cardInfo.getNumber() + "\n" +
                    "卡号�?" + cardInfo.getCardNumber() + "\n" +
                    "\n余额信息：\n" +
                    "余额�?" + cardInfo.getCardBalance() + "元\n" +
                    "过渡余额�?" + cardInfo.getCardInterimBalance() + "元\n" +
                    "\n状�?�信息：\n" +
                    "冻结状�?�：" + cardInfo.getCardFreezeState() + "\n" +
                    "挂失状�?�：" + cardInfo.getCardLostState();
            wechatTextMessage.setContent(content);
        } else {
            wechatTextMessage.setContent("没有查询到校园卡信息");
        }
        /*wechatTextMessage.setContent(stringBuilder);
        WechatArticle cardArticle = new WechatArticle();
        cardArticle.setTitle("校园卡信息查询结�?");
        String stringBuilder = "基本信息：\n" +
                "姓名�?" + cardInfo.getName() + "\n" +
                "学号�?" + cardInfo.getNumber() + "\n" +
                "卡号�?" + cardInfo.getCardNumber() + "\n" +
                "\n余额信息：\n" +
                "余额�?" + cardInfo.getCardBalance() + "元\n" +
                "过渡余额�?" + cardInfo.getCardInterimBalance() + "元\n" +
                "\n状�?�信息：\n" +
                "冻结状�?�：" + cardInfo.getCardFreezeState() + "\n" +
                "挂失状�?�：" + cardInfo.getCardLostState();
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
     * @param wechatBaseMessage
     * @param user
     * @return
     * @throws Exception
     */
    private WechatBaseMessage HandleGradeQueryRequest(WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        Integer group = userLoginService.GetUserByUsername(user.getUsername()).getGroup();
        if (UserGroupEnum.TRIAL.getValue().equals(group)) {
            WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
            DataJsonResult<GradeQueryResult> result = new Gson().fromJson(environment.getProperty("trial.data.grade")
                    , new TypeToken<DataJsonResult<GradeQueryResult>>() {
                    }.getType());
            if (result != null) {
                GradeQueryResult gradeQueryResult = result.getData();
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
                            wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                            wechatTextMessage.setContent(sb.toString());
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
                            wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
                            wechatTextMessage.setContent(sb.toString());
                            return wechatTextMessage;
                        }
                    }
                    return new WechatTextMessage(wechatBaseMessage, "当前学期没有成绩信息");
                }
            }
            return new WechatTextMessage(wechatBaseMessage, "没有查询到成绩信�?");
        }
        GradeQueryResult gradeQueryResult = gradeService.QueryUserGradeFromDocument(user.getUsername(), null);
        if (gradeQueryResult != null) {
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
        return new WechatTextMessage(wechatBaseMessage
                , "成绩缓存信息未同步，请确保已在个人中心的隐私设置中开启教务缓存功�?");
    }

    /**
     * 处理今日课表查询请求
     *
     * @param wechatBaseMessage
     * @param user
     * @return
     * @throws Exception
     */
    private WechatBaseMessage HandleTodayScheduleRequest(WechatBaseMessage wechatBaseMessage, User user) throws Exception {
        Integer group = userLoginService.GetUserByUsername(user.getUsername()).getGroup();
        if (UserGroupEnum.TRIAL.getValue().equals(group)) {
            WechatTextMessage wechatTextMessage = new WechatTextMessage(wechatBaseMessage);
            DataJsonResult<ScheduleQueryResult> result = new Gson().fromJson(environment.getProperty("trial.data.schedule")
                    , new TypeToken<DataJsonResult<ScheduleQueryResult>>() {
                    }.getType());
            if (result != null) {
                List<Schedule> scheduleList = result.getData().getScheduleList();
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
                        wechatTextMessage.setContent(sb.toString());
                        return wechatTextMessage;
                    }
                }
                return new WechatTextMessage(wechatBaseMessage, "今天没有课程");
            }
            return new WechatTextMessage(wechatBaseMessage, "没有查找到成绩信�?");
        }
        ScheduleQueryResult scheduleQueryResult = scheduleService.QueryScheduleFromDocument(user.getUsername(), null);
        if (scheduleQueryResult != null) {
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
        return new WechatTextMessage(wechatBaseMessage
                , "课表缓存信息未同步，请确保已在个人中心的隐私设置中开启教务缓存功�?");
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
        //�?测用户发送的文本内容合法�?
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
                    userLoginService.UserLogin(request.getSession().getId(), new User(username, password), true);
                    //登录成功，进行微信账号绑�?
                    wechatUserDataService.SyncWechatUserData(username, wechatId);
                    return new WechatTextMessage(wechatBaseMessage, "绑定微信账号成功");
                } catch (PasswordIncorrectException e) {
                    //密码错误
                    return new WechatTextMessage(wechatBaseMessage, "账号密码错误，请�?查并重试");
                }
            }
        }
        return new WechatTextMessage(wechatBaseMessage, "发�?�的文本格式有误，请�?查并重试�?" +
                "绑定账号文本格式：绑定账�?-用户�?-密码。例：绑定账�?-gdeiassistant-123456");
    }
}
