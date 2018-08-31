package com.linguancheng.gdeiassistant.Service.UserLogin;

import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Exception.RecognitionException.RecognitionException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.Recognition.RecognitionService;
import com.linguancheng.gdeiassistant.Tools.ImageEncodeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpSessionRequiredException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherLoginService {

    private String url;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Autowired
    private RecognitionService recognitionService;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }

    private Log log = LogFactory.getLog(TeacherLoginService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.teacherlogin']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 教师用户登录
     *
     * @param request
     * @param username
     * @param password
     * @return
     */
    public LoginResultEnum TeacherLogin(HttpServletRequest request, String username, String password) {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("欢迎使用正方教务管理系统！请登录")) {
                //获取验证码图片
                httpGet = new HttpGet(url + "CheckCode.aspx");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    InputStream checkCodeImage = httpResponse.getEntity().getContent();
                    String checkCode = recognitionService.CheckCodeRecognize(ImageEncodeUtils.ConvertToBase64(checkCodeImage)
                            , RecognitionService.CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 4);
                    HttpPost httpPost = new HttpPost(url + "default2.aspx");
                    List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                    basicNameValuePairList.add(new BasicNameValuePair("__VIEWSTATE", document
                            .getElementsByAttributeValue("name", "__VIEWSTATE").val()));
                    basicNameValuePairList.add(new BasicNameValuePair("txtUserName", username));
                    basicNameValuePairList.add(new BasicNameValuePair("txtSecretCode", checkCode));
                    basicNameValuePairList.add(new BasicNameValuePair("Textbox1", ""));
                    basicNameValuePairList.add(new BasicNameValuePair("TextBox2", password));
                    basicNameValuePairList.add(new BasicNameValuePair("RadioButtonList1", "教师"));
                    basicNameValuePairList.add(new BasicNameValuePair("Button1", ""));
                    basicNameValuePairList.add(new BasicNameValuePair("lbLanguage", ""));
                    basicNameValuePairList.add(new BasicNameValuePair("hidPdrs", ""));
                    basicNameValuePairList.add(new BasicNameValuePair("hidsc", ""));
                    httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                    httpResponse = httpClient.execute(httpPost);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 302) {
                        httpGet = new HttpGet(url + "js_main.aspx?xh=" + username);
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("正方教务管理系统")) {
                            return LoginResultEnum.LOGIN_SUCCESS;
                        }
                        throw new ServerErrorException("进入教师主页失败");
                    } else if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("欢迎使用正方教务管理系统！请登录")) {
                        throw new PasswordIncorrectException("账号密码错误");
                    } else {
                        throw new ServerErrorException("登录系统异常");
                    }
                }
                throw new ServerErrorException("获取验证码图片失败");
            }
            throw new ServerErrorException("教务系统异常");
        } catch (IOException e) {
            log.error("教师登录异常：" , e);
            return LoginResultEnum.TIME_OUT;
        } catch (ServerErrorException | RecognitionException e) {
            log.error("教师登录异常：" , e);
            return LoginResultEnum.SERVER_ERROR;
        } catch (PasswordIncorrectException e) {
            log.error("教师登录异常：" , e);
            return LoginResultEnum.PASSWORD_ERROR;
        } catch (Exception e) {
            log.error("教师登录异常：" , e);
            return LoginResultEnum.SERVER_ERROR;
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
