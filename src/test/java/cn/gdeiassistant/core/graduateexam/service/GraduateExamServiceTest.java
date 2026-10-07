package cn.gdeiassistant.core.graduateexam.service;

import cn.gdeiassistant.common.enums.recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.exception.queryexception.ErrorQueryConditionException;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.core.imagerecognition.service.ImageRecognitionService;
import cn.gdeiassistant.integration.chsi.ChsiClient;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GraduateExamServiceTest {
    private final ChsiClient client = mock(ChsiClient.class);
    private final ImageRecognitionService recognition = mock(ImageRecognitionService.class);
    private final GraduateExamService service = new GraduateExamService();
    @BeforeEach void setup() throws Exception {
        ReflectionTestUtils.setField(service, "chsiClient", client);
        ReflectionTestUtils.setField(service, "imageRecognitionService", recognition);
        when(client.fetchPostgraduateCjcxPage()).thenReturn(Jsoup.parse("<form name='cjcxForm'></form>"));
    }
    @Test void fourDistinctSubjectsMustRemainDistinctInTheExistingTableContract() throws Exception {
        String[] values={"合成姓名","synthetic-signup","synthetic-exam","合成单位","345","70","80","90","105"};
        var html=new StringBuilder("<div class='container clearfix'><table class='cjxx-info'>");
        for(String value:values) html.append("<tr><td>字段</td><td>").append(value).append("</td></tr>");
        html.append("</table></div>");
        when(client.submitPostgraduateQuery("name","exam","identity",null)).thenReturn(Jsoup.parse(html.toString()));
        var result=service.queryPostgraduateScore("name","exam","identity");
        assertEquals("合成姓名",result.getName()); assertEquals("synthetic-signup",result.getSignUpNumber());
        assertEquals("synthetic-exam",result.getExamNumber()); assertEquals("345",result.getTotalScore());
        assertEquals("70",result.getFirstScore()); assertEquals("80",result.getSecondScore());
        assertEquals("90",result.getThirdScore()); assertEquals("105",result.getFourthScore());
        verifyNoInteractions(recognition);
    }
    @Test void noResultsAndInvalidConditionsDoNotProduceFabricatedScores() throws Exception {
        when(client.submitPostgraduateQuery(anyString(),anyString(),anyString(),isNull())).thenReturn(
                Jsoup.parse("<div class='container clearfix'><div class='zx-no-answer'></div></div>"),
                Jsoup.parse("<div class='ch-alert-message'>条件错误</div>"), Jsoup.parse("<div>变化的页面</div>"));
        assertNull(service.queryPostgraduateScore("name","exam","identity"));
        assertThrows(ErrorQueryConditionException.class,()->service.queryPostgraduateScore("name","exam","identity"));
        assertThrows(ServerErrorException.class,()->service.queryPostgraduateScore("name","exam","identity"));
    }
    @Test void captchaFailureStopsBeforeSubmittingTheQuery() throws Exception {
        var form="<form name='cjcxForm'><input id='checkcode'><table>"+"<tr><td align='left'>field</td></tr>".repeat(5)
                +"<tr><td align='left'><img src='/synthetic-captcha'></td></tr></table></form>";
        when(client.fetchPostgraduateCjcxPage()).thenReturn(Jsoup.parse(form));
        when(client.fetchPostgraduateCaptchaImage("https://yz.chsi.com.cn/synthetic-captcha")).thenReturn(new byte[]{1,2,3});
        when(recognition.checkCodeRecognize(anyString(),eq(CheckCodeTypeEnum.NUMBER),eq(4))).thenThrow(new RecognitionException("synthetic"));
        assertThrows(RecognitionException.class,()->service.queryPostgraduateScore("name","exam","identity"));
        verify(client,never()).submitPostgraduateQuery(any(),any(),any(),any());
    }
    @Test void transportAndMalformedTableFailuresKeepTheirClassification() throws Exception {
        when(client.fetchPostgraduateCjcxPage()).thenThrow(new IOException("synthetic timeout"));
        assertThrows(NetWorkTimeoutException.class,()->service.queryPostgraduateScore("name","exam","identity"));
        doReturn(Jsoup.parse("<form name='cjcxForm'></form>")).when(client).fetchPostgraduateCjcxPage();
        when(client.submitPostgraduateQuery(anyString(),anyString(),anyString(),isNull())).thenReturn(Jsoup.parse("<div class='container clearfix'><table class='cjxx-info'><tr><td>缺失列</td></tr></table></div>"));
        assertThrows(ServerErrorException.class,()->service.queryPostgraduateScore("name","exam","identity"));
    }
}
