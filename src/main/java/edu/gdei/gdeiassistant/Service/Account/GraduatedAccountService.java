package edu.gdei.gdeiassistant.Service.Account;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Enum.Graduation.GraduationProgramTypeEnum;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupTypeEnum;
import edu.gdei.gdeiassistant.Exception.UserLoginException.UserGraduatedException;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.Graduation;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Graduation.GraduationMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import edu.gdei.gdeiassistant.Service.CardQuery.CardQueryService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GraduatedAccountService {

    private Log log = LogFactory.getLog(GraduatedAccountService.class);

    private int timeout;

    @Autowired
    private CloseAccountService closeAccountService;

    @Autowired
    private CardQueryService cardQueryService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GraduationMapper graduationMapper;

    @Value("#{propertiesReader['timeout.graduation']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

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
     * 从每年的七月开始至九月结束，每隔半个月执行毕业用户账号处理方案
     */
    @Scheduled(cron = "0 0 0 1,15,30 7,8,9 ?")
    public void ProceedGraduationProgram() throws Exception {
        List<User> userList = userMapper.selectAllUser();
        for (User user : userList) {
            User decryptedUser = user.decryptUser();
            try {
                if (user.getGroup().equals(UserGroupTypeEnum.GRADUATED.getType())) {
                    //若当前用户是毕业用户，则跳过检测
                    continue;
                }
                if ((CheckUserBelongToGraduated(UUID.randomUUID().toString(), decryptedUser.getUsername()
                        , decryptedUser.getPassword()))) {
                    //检查当前用户是否为毕业生
                    Graduation graduation = graduationMapper.selectGraduation(user.getUsername());
                    if (graduation != null && graduation.getProgram().equals(GraduationProgramTypeEnum
                            .UPGRADE_TO_GRADUATED_ACCOUNT.getType())) {
                        //升级为毕业用户账号
                        userMapper.updateUserGroup(user.getUsername(), UserGroupTypeEnum.GRADUATED.getType());
                    } else {
                        //删除用户账号
                        closeAccountService.CloseAccount(decryptedUser.getUsername(), decryptedUser.getPassword());
                    }
                }
            } catch (Exception e) {
                log.error(e);
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
