package edu.gdei.gdeiassistant.Service.Account;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Enum.Graduation.GraduationProgramTypeEnum;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Exception.UserLoginException.UserGraduatedException;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.Graduation;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Graduation.GraduationMapper;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User.UserMapper;
import edu.gdei.gdeiassistant.Service.CardQuery.CardQueryService;
import edu.gdei.gdeiassistant.Service.GradeQuery.GradeService;
import edu.gdei.gdeiassistant.Service.ScheduleQuery.ScheduleService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class GraduatedAccountService {

    private final Logger logger = LoggerFactory.getLogger(GraduatedAccountService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.graduation']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Autowired
    private CloseAccountService closeAccountService;

    @Autowired
    private CardQueryService cardQueryService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GraduationMapper graduationMapper;

    /**
     * 查询用户填写的毕业用户账号处理方案
     *
     * @param username
     * @return
     * @throws WsgException
     */
    public Graduation QueryUserGraduationProgram(String username) throws WsgException {
        Graduation graduation = graduationMapper.selectGraduation(StringEncryptUtils.encryptString(username));
        if (graduation != null) {
            return graduation;
        }
        return new Graduation(username, GraduationProgramTypeEnum.UPGRADE_TO_GRADUATED_ACCOUNT.getType());
    }

    /**
     * 保存用户填写的毕业用户账号处理方案
     *
     * @param username
     * @param program
     * @throws WsgException
     */
    public void SaveUserGraduationProgram(String username, Integer program) throws WsgException {
        Graduation graduation = graduationMapper.selectGraduation(StringEncryptUtils.encryptString(username));
        if (graduation != null) {
            graduationMapper.updateGraduation(new Graduation(StringEncryptUtils.encryptString(username), program));
        } else {
            graduationMapper.insertGraduation(new Graduation(StringEncryptUtils.encryptString(username), program));
        }
    }

    /**
     * 每年的七月、八月和九月一日执行毕业用户账号处理方案
     */
    @Scheduled(cron = "0 0 0 1 7,8,9 ?")
    public void ProceedGraduationProgram() throws Exception {
        logger.info("{}启动了执行毕业用户账号处理方案的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        List<User> userList = userMapper.selectAllUser();
        for (User user : userList) {
            User decryptedUser = user.decryptUser();
            try {
                if (user.getGroup().equals(UserGroupEnum.GRADUATED.getValue())) {
                    //若当前用户是毕业用户，则跳过
                    continue;
                } else if (!user.getGroup().equals(UserGroupEnum.STUDENT.getValue())
                        && !user.getGroup().equals(UserGroupEnum.TEST.getValue())) {
                    //若当前用户不是学生用户和测试用户，则不支持进行毕业账号处理
                    continue;
                }
                if ((CheckUserBelongToGraduated(UUID.randomUUID().toString(), decryptedUser.getUsername()
                        , decryptedUser.getPassword()))) {
                    //检查当前用户是否为毕业生
                    Graduation graduation = graduationMapper.selectGraduation(user.getUsername());
                    if (graduation == null || graduation.getProgram().equals(GraduationProgramTypeEnum
                            .UPGRADE_TO_GRADUATED_ACCOUNT.getType())) {
                        //清除缓存的成绩和课表数据
                        gradeService.ClearGrade(decryptedUser.getUsername());
                        scheduleService.ClearSchedule(decryptedUser.getUsername());
                        //升级为毕业用户账号
                        userMapper.updateUserGroup(user.getUsername(), UserGroupEnum.GRADUATED.getValue());
                    } else {
                        //关闭待处理的社区功能信息
                        closeAccountService.CloseSocialDataState(decryptedUser.getUsername());
                        //删除用户账号
                        closeAccountService.CloseAccount(decryptedUser.getUsername(), decryptedUser.getPassword());
                    }
                }
            } catch (Exception e) {
                logger.error("test", e);
            }
        }
    }

    /**
     * 检测用户是否已经毕业
     *
     * @param sessionId
     * @param username
     * @param password
     * @return
     */
    private boolean CheckUserBelongToGraduated(String sessionId, String username, String password) throws Exception {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId
                    , false, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //登录支付管理平台
            cardQueryService.LoginCardSystem(httpClient, username, password, true);
            //获取校园卡基本信息
            CardInfo cardInfo = cardQueryService.QueryCardInformation(httpClient);
            //若当前学号前缀与当前年份相差大于等于四年，则为可能的毕业用户
            if (LocalDate.now().getYear() - Integer.valueOf("20" + cardInfo.getNumber().substring(0, 2)) >= 4) {
                userLoginService.UserLogin(sessionId, new User(username, password), false);
            }
            //差距小于四年或登录成功，为普通学生用户
            return false;
        } catch (UserGraduatedException e) {
            //账号已毕业注销，登录教务系统失败，符合毕业用户的标准
            return true;
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.SyncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
    }
}
