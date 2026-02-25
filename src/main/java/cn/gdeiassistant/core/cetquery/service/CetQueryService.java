package cn.gdeiassistant.core.cetquery.service;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.pojo.Entity.Cet;
import cn.gdeiassistant.common.pojo.Entity.CetNumber;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.cet.mapper.CetMapper;
import cn.gdeiassistant.core.cetquery.pojo.dto.CetQueryDTO;
import cn.gdeiassistant.core.cetquery.pojo.vo.CetNumberVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.integration.chsi.ChsiClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CetQueryService {

    private final Logger logger = LoggerFactory.getLogger(CetQueryService.class);

    @Autowired
    private CetMapper cetMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private ChsiClient chsiClient;

    public String cetIndex(String sessionId) throws Exception {
        try {
            return chsiClient.fetchCetCaptchaImageBase64(sessionId);
        } catch (IOException e) {
            logger.error("查询四六级成绩异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        }
    }

    public Cet cetQuery(String sessionId, CetQueryDTO dto) throws Exception {
        try {
            Document document = chsiClient.fetchCetQueryPage(
                    sessionId,
                    dto.getNumber(),
                    dto.getName(),
                    dto.getCheckcode() != null ? dto.getCheckcode() : "");
            if (document.getElementsByClass("cetTable").size() == 0) {
                if (document.getElementsByClass("error alignC marginT20").size() != 0) {
                    throw new PasswordIncorrectException("四六级查询信息错误");
                }
                if (document.getElementsByClass("error alignC").size() != 0) {
                    throw new ErrorQueryConditionException("验证码信息错误");
                }
                throw new ServerErrorException("四六级查询系统异常");
            }
            Element element = document.getElementsByClass("cetTable").get(0);
            Elements trs = element.getElementsByTag("tr");
            String name = trs.get(0).getElementsByTag("td").text();
            String school = trs.get(1).getElementsByTag("td").text();
            String type = trs.get(2).getElementsByTag("td").text();
            String admissionCard = trs.get(4).getElementsByTag("td").text();
            String totalScore = trs.get(5).getElementsByClass("colorRed").get(0).text().replace(" ", "");
            String listeningScore = trs.get(6).select("td").get(1).text().replace(" ", "");
            String readingScore = trs.get(7).select("td").get(1).text().replace(" ", "");
            String writingAndTranslatingScore = trs.get(8).select("td").get(1).text().replace(" ", "");
            Cet cet = new Cet();
            cet.setName(name);
            cet.setSchool(school);
            cet.setType(type);
            cet.setAdmissionCard(admissionCard);
            cet.setTotalScore(totalScore);
            cet.setListeningScore(listeningScore);
            cet.setReadingScore(readingScore);
            cet.setWritingAndTranslatingScore(writingAndTranslatingScore);
            return cet;
        } catch (PasswordIncorrectException e) {
            throw new PasswordIncorrectException("账户密码错误");
        } catch (ErrorQueryConditionException e) {
            logger.error("查询四六级成绩异常：", e);
            throw new ErrorQueryConditionException("查询条件错误");
        } catch (IOException e) {
            logger.error("查询四六级成绩异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        }
    }

    public CetNumberVO getCetNumber(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        CetNumber cetNumber = cetMapper.selectNumber(user.getUsername());
        if (cetNumber == null || cetNumber.getNumber() == null) {
            return null;
        }
        return new CetNumberVO(cetNumber.getNumber(), null);
    }

    public void saveCetNumber(String sessionId, Long number, String name) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String username = user.getUsername();
        CetNumber cetNumber = cetMapper.selectNumber(username);
        if (cetNumber == null) {
            cetMapper.insertNumber(username, number);
        } else {
            cetMapper.updateNumber(username, number);
        }
    }

    public Cet queryCetScore(String sessionId, String ticketNumber, String name, String checkcode) throws Exception {
        CetQueryDTO dto = new CetQueryDTO();
        dto.setNumber(ticketNumber);
        dto.setName(name);
        dto.setCheckcode(checkcode != null ? checkcode : "");
        return cetQuery(sessionId, dto);
    }
}
