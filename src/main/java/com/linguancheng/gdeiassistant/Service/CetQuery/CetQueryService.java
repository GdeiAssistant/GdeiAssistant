package com.linguancheng.gdeiassistant.Service.CetQuery;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Cet.CetMapper;
import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetNumberQueryResult;
import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetQuery;
import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetQueryResult;
import com.linguancheng.gdeiassistant.Pojo.Entity.Cet;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.ImageEncodeUtils;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by linguancheng on 2017/7/21.
 */

@Service
public class CetQueryService {

    @Resource(name = "cetMapper")
    private CetMapper cetMapper;

    @Autowired
    private HttpClientFactory httpClientFactory;

    private Log log = LogFactory.getLog(CetQueryService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.cet']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 进入学信网四六级成绩查询页面，获取验证码
     *
     * @return
     */
    public BaseResult<String, ServiceResultEnum> CetIndex(HttpServletRequest request) {
        BaseResult<String, ServiceResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            HttpGet httpGet = new HttpGet("http://www.chsi.com.cn/cet/");
            httpGet.setHeader("Referer", "http://www.chsi.com.cn/cet/");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                httpGet = new HttpGet("http://www.chsi.com.cn/cet/ValidatorIMG.JPG");
                httpGet.setHeader("Referer", "http://www.chsi.com.cn/cet/");
                httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    String base64 = ImageEncodeUtils.ConvertToBase64(httpResponse.getEntity().getContent());
                    result.setResultType(ServiceResultEnum.SUCCESS);
                    result.setResultData(base64);
                    return result;
                }
                throw new ServerErrorException("获取学信网验证码图片异常");
            }
            throw new ServerErrorException("访问学信网异常");
        } catch (ServerErrorException e) {
            log.error("查询四六级成绩异常：" + e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
        } catch (IOException e) {
            log.error("查询四六级成绩异常：" + e);
            result.setResultType(ServiceResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("查询四六级成绩异常：" + e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 查询四六级考试成绩
     *
     * @param cetQuery
     * @return
     */
    public CetQueryResult CetQuery(HttpServletRequest request, CetQuery cetQuery) {
        CetQueryResult cetQueryResult = new CetQueryResult();
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            //查询CET成绩信息
            HttpGet httpGet = new HttpGet("http://www.chsi.com.cn/cet/query?zkzh=" + cetQuery.getNumber() + "&xm=" + cetQuery.getName() + "&yzm=" + cetQuery.getCheckcode());
            httpGet.setHeader("Referer", "http://www.chsi.com.cn/cet/");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (document.getElementsByClass("cetTable").size() == 0) {
                    if (document.getElementsByClass("error alignC marginT20").size() != 0) {
                        //准考证号或姓名错误
                        throw new PasswordIncorrectException("四六级查询信息错误");
                    }
                    if (document.getElementsByClass("error alignC").size() != 0) {
                        //验证码错误
                        throw new ErrorQueryConditionException("验证码信息错误");
                    }
                    throw new ServerErrorException("四六级查询系统异常");
                }
                //信息输入正确,进行信息解析
                //获取存放成绩的表格
                Element element = document.getElementsByClass("cetTable").get(0);
                //得到所有的行
                Elements trs = element.getElementsByTag("tr");
                //获取信息
                String name = trs.get(0).getElementsByTag("td").text();
                String school = trs.get(1).getElementsByTag("td").text();
                String type = trs.get(2).getElementsByTag("td").text();
                String admissionCard = trs.get(4).getElementsByTag("td").text();
                //获取总分信息,总分信息含有空格,需要将字符串中的空格删除,听力/阅读/写作与翻译的分数数据同理
                String totalScore = trs.get(5).getElementsByClass("colorRed").get(0).text().replace(" ", "");
                //获取听力分数
                String listeningScore = trs.get(6).select("td").get(1).text().replace(" ", "");
                //获取阅读分数
                String readingScore = trs.get(7).select("td").get(1).text().replace(" ", "");
                //获取写作与翻译分数
                String writingAndTranslatingScore = trs.get(8).select("td").get(1).text().replace(" ", "");
                //保存查询成绩结果并返回
                Cet cet = new Cet();
                cet.setName(name);
                cet.setSchool(school);
                cet.setType(type);
                cet.setAdmissionCard(admissionCard);
                cet.setTotalScore(totalScore);
                cet.setListeningScore(listeningScore);
                cet.setReadingScore(readingScore);
                cet.setWritingAndTranslatingScore(writingAndTranslatingScore);
                cetQueryResult.setCet(cet);
                cetQueryResult.setCetQueryResultEnum(ServiceResultEnum.SUCCESS);
                return cetQueryResult;
            }
            throw new ServerErrorException("学信网系统异常");
        } catch (ServerErrorException e) {
            log.error("查询四六级成绩异常：" + e);
            cetQueryResult.setCetQueryResultEnum(ServiceResultEnum.SERVER_ERROR);
        } catch (PasswordIncorrectException e) {
            log.error("查询四六级成绩异常：" + e);
            cetQueryResult.setCetQueryResultEnum(ServiceResultEnum.PASSWORD_INCORRECT);
        } catch (ErrorQueryConditionException e) {
            log.error("查询四六级成绩异常：" + e);
            cetQueryResult.setCetQueryResultEnum(ServiceResultEnum.ERROR_CONDITION);
        } catch (IOException e) {
            log.error("查询四六级成绩异常：" + e);
            cetQueryResult.setCetQueryResultEnum(ServiceResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("查询四六级成绩异常：" + e);
            cetQueryResult.setCetQueryResultEnum(ServiceResultEnum.SERVER_ERROR);
        }
        return cetQueryResult;
    }

    /**
     * 查询保存的四六级准考证号
     *
     * @param username
     * @return
     */
    public BaseResult<Long, DataBaseResultEnum> getCetNumber(String username) {
        BaseResult<Long, DataBaseResultEnum> result = new BaseResult<>();
        try {
            CetNumberQueryResult cetNumberQueryResult = cetMapper.selectNumber(StringEncryptUtils.encryptString(username));
            if (cetNumberQueryResult == null || cetNumberQueryResult.getNumber() == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                result.setResultData(cetNumberQueryResult.getNumber());
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("导入四六级准考证号异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 保存四六级准考证号
     *
     * @param username
     * @param number
     * @return
     */
    public BoolResultEnum saveCetNumber(String username, Long number) {
        try {
            CetNumberQueryResult cetNumberQueryResult = cetMapper.selectNumber(StringEncryptUtils.encryptString(username));
            if (cetNumberQueryResult == null) {
                cetMapper.insertNumber(StringEncryptUtils.encryptString(username), number);
            } else {
                cetMapper.updateNumber(StringEncryptUtils.encryptString(username), number);
            }
            return BoolResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("保存四六级准考证号异常：" + e);
            return BoolResultEnum.ERROR;
        }
    }

}
