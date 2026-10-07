package cn.gdeiassistant.core.cardquery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.enums.recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.cardquery.pojo.CardQuery;
import cn.gdeiassistant.core.imagerecognition.service.ImageRecognitionService;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.card.CardClient;
import cn.gdeiassistant.integration.httpclient.*;

import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicStatusLine;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.*;

/** Every HTTP exchange and card mutation is mocked; no campus system is contacted. */
@ExtendWith(MockitoExtension.class)
class CardQueryServiceTest {
    @Mock UserCertificateService certificates;
    @Mock HttpClientUtils clients;
    @Mock CardClient cards;
    @Mock ImageRecognitionService recognition;
    @Mock CloseableHttpClient http;
    @InjectMocks CardQueryService service;
    BasicCookieStore cookies = new BasicCookieStore();

    @BeforeEach
    void setup() throws Exception {
        User u = new User("owner");
        u.setPassword("synthetic-password");
        UserCertificateEntity e = new UserCertificateEntity();
        e.setUser(u);
        when(certificates.getUserSessionCertificate("session")).thenReturn(e);
        when(clients.getHttpClient("session", true, 15))
                .thenReturn(new HttpClientSession(http, cookies));
    }

    CloseableHttpResponse response(int status, String html) {
        CloseableHttpResponse r = mock(CloseableHttpResponse.class);
        lenient()
                .when(r.getStatusLine())
                .thenReturn(
                        new BasicStatusLine(
                                new ProtocolVersion("HTTP", 1, 1), status, "synthetic"));
        lenient().when(r.getEntity()).thenReturn(new StringEntity(html, StandardCharsets.UTF_8));
        return r;
    }

    void respond(CloseableHttpResponse... responses) throws Exception {
        when(http.execute(any(HttpUriRequest.class)))
                .thenReturn(responses[0], Arrays.copyOfRange(responses, 1, responses.length));
    }

    void login() throws Exception {
        respond(
                response(
                        200,
                        "<div class='pcclient'></div><input id='tokens' value='synthetic'><input"
                                + " id='stamp' value='synthetic'>"),
                response(200, "<a href='http://ecard.gdei.edu.cn/fixture'>continue</a>"),
                response(200, "<div class='clear main'></div>"));
    }

    String info() {
        return "<div class='Jbinfo'><em>Synthetic Student</em><em>10000000001</em></div><div"
                + " class='Jbinfo'><em> 100 001 </em><em>20.50</em></div><div"
                + " class='Jbinfo'><em>1.00</em></div><div"
                + " class='careful'><em>正常</em><em>未冻结</em></div>";
    }

    CardQuery date(boolean today) {
        Calendar c = Calendar.getInstance();
        CardQuery q = new CardQuery();
        q.setYear(today ? c.get(Calendar.YEAR) : 2020);
        q.setMonth(today ? c.get(Calendar.MONTH) + 1 : 1);
        q.setDate(today ? c.get(Calendar.DAY_OF_MONTH) : 1);
        return q;
    }

    String trades(boolean today, boolean empty) {
        return "<table class='table_show'><tr><th>header</th></tr><tr><td>"
                + (empty ? "当前查询条件内没有流水记录" : today ? "a b total:11" : "a b c d total:11")
                + "</td></tr>"
                + (empty
                        ? ""
                        : "<tr><td>12:00</td><td>Shop</td><td></td><td>Payment</td><td>2.50</td><td>18.00</td></tr>")
                + "</table>";
    }

    @Test
    void cardInfoParsesFieldsAndClosesSession() throws Exception {
        login();
        when(cards.fetchCardBasicInfoDocument("session")).thenReturn(Jsoup.parse(info()));
        var r = service.cardInfoQuery("session");
        assertEquals("Synthetic Student", r.getName());
        assertEquals("100001", r.getCardNumber());
        assertEquals("20.50", r.getCardBalance());
        assertEquals("1.00", r.getCardInterimBalance());
        assertEquals("正常", r.getCardLostState());
        assertEquals("未冻结", r.getCardFreezeState());
        verify(http).close();
        verify(clients, times(2)).syncHttpClientCookieStore("session", cookies);
    }

    @Test
    void currentDayTradesIncludeAllPages() throws Exception {
        login();
        when(cards.fetchCardBasicInfoDocument("session")).thenReturn(Jsoup.parse(info()));
        when(cards.fetchCardTrjnListDocument(eq("session"), eq(0), anyInt()))
                .thenReturn(Jsoup.parse(trades(true, false)));
        var r = service.cardQuery("session", date(true));
        assertEquals(2, r.getCardList().size());
        assertEquals("Shop", r.getCardList().get(0).getMerchantName());
        assertEquals("2.50", r.getCardList().get(0).getTradePrice());
        assertEquals("18.00", r.getCardList().get(0).getAccountBalance());
        verify(cards).fetchCardTrjnListDocument("session", 0, 2);
        verify(http).close();
    }

    @Test
    void historicalTradesUseRequestedDateAndPagination() throws Exception {
        login();
        when(cards.fetchCardBasicInfoDocument("session")).thenReturn(Jsoup.parse(info()));
        when(cards.fetchCardTrjnListByDateDocument(eq("session"), eq(2020), eq(1), eq(1), anyInt()))
                .thenReturn(Jsoup.parse(trades(false, false)));
        assertEquals(2, service.cardQuery("session", date(false)).getCardList().size());
        verify(cards).fetchCardTrjnListByDateDocument("session", 2020, 1, 1, 2);
    }

    @Test
    void emptyCurrentAndHistoricalRecordsAreEmptyLists() throws Exception {
        login();
        when(cards.fetchCardBasicInfoDocument("session")).thenReturn(Jsoup.parse(info()));
        when(cards.fetchCardTrjnListDocument("session", 0, 1))
                .thenReturn(Jsoup.parse(trades(true, true)));
        assertTrue(service.cardQuery("session", date(true)).getCardList().isEmpty());
        reset(http);
        login();
        when(cards.fetchCardTrjnListByDateDocument("session", 2020, 1, 1, 1))
                .thenReturn(Jsoup.parse(trades(false, true)));
        assertTrue(service.cardQuery("session", date(false)).getCardList().isEmpty());
    }

    @Test
    void passwordFailureDoesNotInvokeCardMutationAndAlwaysCloses() throws Exception {
        respond(
                response(200, "<div class='pcclient'></div><input id='tokens'><input id='stamp'>"),
                response(200, "<div class='pcclient'></div>"));
        assertThrows(PasswordIncorrectException.class, () -> service.cardLost("session", "012"));
        verifyNoInteractions(cards);
        verify(http).close();
    }

    @Test
    void networkAndMalformedResponsesProduceTypedErrors() throws Exception {
        when(http.execute(any(HttpUriRequest.class))).thenThrow(new java.io.IOException());
        assertThrows(NetWorkTimeoutException.class, () -> service.cardInfoQuery("session"));
        assertThrows(
                NetWorkTimeoutException.class, () -> service.cardQuery("session", date(false)));
        assertThrows(NetWorkTimeoutException.class, () -> service.cardLost("session", "012"));
        reset(http);
        respond(response(503, "unavailable"));
        assertThrows(ServerErrorException.class, () -> service.cardInfoQuery("session"));
    }

    void loss() throws Exception {
        login();
        when(cards.fetchLossCardPageDocument("session"))
                .thenReturn(Jsoup.parse("<img id='imgCheckCode' src='synthetic.png'>"));
        when(cards.fetchKeyPadImage("session")).thenReturn(new byte[] {1});
        java.io.ByteArrayOutputStream png = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(
                new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB),
                "png",
                png);
        lenient()
                .when(cards.fetchCheckcodeImage("session", "synthetic.png"))
                .thenReturn(png.toByteArray());
        lenient().when(recognition.characterNumberRecognize(anyString())).thenReturn("0123456789");
        lenient()
                .when(
                        recognition.checkCodeRecognize(
                                anyString(), eq(CheckCodeTypeEnum.NUMBER), eq(4)))
                .thenReturn("1234");
    }

    @Test
    void simulatedLossSubmissionMapsKeyboardAndChecksServerResult() throws Exception {
        loss();
        when(cards.submitSetCardLost("session", "210", "1234"))
                .thenReturn("{\"ret\":true,\"msg\":\"ok\"}");
        service.cardLost("session", "012");
        verify(cards).submitSetCardLost("session", "210", "1234");
        verify(http).close();
    }

    @Test
    void invalidKeyboardRecognitionIsBoundedAndNeverSubmits() throws Exception {
        loss();
        when(recognition.characterNumberRecognize(anyString())).thenReturn("invalid");
        assertThrows(RecognitionException.class, () -> service.cardLost("session", "012"));
        verify(cards, times(3)).fetchKeyPadImage("session");
        verify(cards, never()).submitSetCardLost(anyString(), anyString(), anyString());
    }

    @Test
    void invalidCheckcodeRecognitionIsBoundedAndNeverSubmits() throws Exception {
        loss();
        when(recognition.checkCodeRecognize(anyString(), any(), anyInt())).thenReturn("bad");
        assertThrows(RecognitionException.class, () -> service.cardLost("session", "012"));
        verify(cards, times(3)).fetchCheckcodeImage("session", "synthetic.png");
        verify(cards, never()).submitSetCardLost(anyString(), anyString(), anyString());
    }

    @Test
    void upstreamLossRejectionRemainsFailure() throws Exception {
        loss();
        when(cards.submitSetCardLost(anyString(), anyString(), anyString()))
                .thenReturn("{\"ret\":false,\"msg\":\"查询密码错误\"}");
        assertThrows(PasswordIncorrectException.class, () -> service.cardLost("session", "012"));
    }
}
