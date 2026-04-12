package cn.gdeiassistant.core.graduateExam.service;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.pojo.Entity.Postgraduate;
import cn.gdeiassistant.core.imageRecognition.service.ImageRecognitionService;
import cn.gdeiassistant.common.tools.Utils.ImageEncodeUtils;
import cn.gdeiassistant.integration.chsi.ChsiClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class GraduateExamService {

    @Autowired
    private ImageRecognitionService imageRecognitionService;

    @Autowired
    private ChsiClient chsiClient;

    private final Logger logger = LoggerFactory.getLogger(GraduateExamService.class);

    /**
     * 查询考研初试成绩
     */
    public Postgraduate queryPostgraduateScore(String name, String examNumber, String idNumber) throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException, RecognitionException {
        try {
            Document document = chsiClient.fetchPostgraduateCjcxPage();
            Element cjcxForm = document.getElementsByAttributeValue("name", "cjcxForm").first();
            boolean hasCheckCode = false;
            String checkcode = null;
            if (cjcxForm.getElementById("checkcode") != null) {
                hasCheckCode = true;
                String imageURL = "https://yz.chsi.com.cn" + cjcxForm.select("td[align='left']").get(5)
                        .select("img").first().attr("src");
                byte[] imageBytes = chsiClient.fetchPostgraduateCaptchaImage(imageURL);
                String base64 = ImageEncodeUtils.convertToBase64(new ByteArrayInputStream(imageBytes));
                checkcode = imageRecognitionService.CheckCodeRecognize(base64, CheckCodeTypeEnum.NUMBER, 4);
            }
            document = chsiClient.submitPostgraduateQuery(name, examNumber, idNumber, checkcode);
            Element ch_alert_message = document.getElementsByClass("ch-alert-message").first();
            if (ch_alert_message != null) {
                throw new ErrorQueryConditionException("查询条件错误");
            }
            Element container_clearfix = document.getElementsByClass("container clearfix").first();
            Element zx_no_answer = container_clearfix.getElementsByClass("zx-no-answer").first();
            if (zx_no_answer != null) {
                return null;
            }
            Elements data = container_clearfix.getElementsByClass("cjxx-info").select("tr");
            Postgraduate postgraduate = new Postgraduate();
            postgraduate.setName(data.get(0).select("td").get(1).text());
            postgraduate.setSignUpNumber(data.get(1).select("td").get(1).text());
            postgraduate.setExamNumber(data.get(2).select("td").get(1).text());
            postgraduate.setTotalScore(data.get(4).select("td").get(1).text());
            postgraduate.setFirstScore(data.get(5).select("td").get(1).text().replace(" ", ""));
            postgraduate.setSecondScore(data.get(6).select("td").get(1).text().replace(" ", ""));
            postgraduate.setThirdScore(data.get(6).select("td").get(1).text().replace(" ", ""));
            postgraduate.setFourthScore(data.get(7).select("td").get(1).text().replace(" ", ""));
            return postgraduate;
        } catch (IOException e) {
            logger.error("查询考研成绩异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (ErrorQueryConditionException e) {
            logger.error("查询考研成绩异常：", e);
            throw new ErrorQueryConditionException("查询条件错误");
        } catch (RecognitionException e) {
            logger.error("查询考研成绩验证码识别异常：", e);
            throw new RecognitionException("图像识别服务异常，请稍后重试");
        } catch (Exception e) {
            logger.error("查询考研成绩异常：", e);
            throw new ServerErrorException("教务系统异常");
        }
    }
}
