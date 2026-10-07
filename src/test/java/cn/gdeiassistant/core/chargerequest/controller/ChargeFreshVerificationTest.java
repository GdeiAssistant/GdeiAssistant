package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.constant.ErrorConstantUtils;
import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.interceptor.ApiAuthInterceptor;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
import cn.gdeiassistant.common.redis.cookiestore.CookieStoreDao;
import cn.gdeiassistant.core.charge.service.*;
import cn.gdeiassistant.core.charge.pojo.entity.ChargeOrderEntity;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeVO;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP CAS is loopback only; payment/order stores are synthetic mocks. */
class ChargeFreshVerificationTest {
    static final String USER="synthetic-user",PASSWORD="synthetic-current-password",SESSION="synthetic-session";
    HttpServer cas;HttpClientUtils clients;MockMvc mvc;
    UserCertificateDao certificates;CookieStoreDao cookies;
    ChargeService charges;ChargeOrderService orders;ChargeIdempotencyService idempotency;
    AtomicInteger gets,posts;volatile String currentPassword;volatile int pageStatus;
    @BeforeEach void setup() throws Exception {
        gets=new AtomicInteger();posts=new AtomicInteger();currentPassword=PASSWORD;pageStatus=200;
        cas=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        cas.createContext("/cas",exchange->{
            String html;
            if(exchange.getRequestMethod().equals("GET")){
                gets.incrementAndGet();html="<input id='tokens' value='synthetic'><input id='stamp' value='synthetic'>";
            }else{
                posts.incrementAndGet();var form=new HashMap<String,String>();
                for(String pair:new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8).split("&")){
                    String[] parts=pair.split("=",2);form.put(URLDecoder.decode(parts[0],StandardCharsets.UTF_8),parts.length==2?URLDecoder.decode(parts[1],StandardCharsets.UTF_8):"");
                }
                html=USER.equals(form.get("username"))&&currentPassword.equals(form.get("password"))?"<body bgcolor='white'>synthetic success</body>":"<body>synthetic rejected</body>";
            }
            byte[] bytes=html.getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(pageStatus,bytes.length);
            try(var output=exchange.getResponseBody()){output.write(bytes);}finally{exchange.close();}
        });cas.start();
        clients=new HttpClientUtils();cookies=mock(CookieStoreDao.class);clients.setCookieStoreDao(cookies);
        certificates=mock(UserCertificateDao.class);when(certificates.queryUserLoginCertificate(SESSION)).thenReturn(new User(USER,(String)null));
        var service=new UserCertificateService();ReflectionTestUtils.setField(service,"httpClientUtils",clients);ReflectionTestUtils.setField(service,"userCertificateDao",certificates);
        String local="http://127.0.0.1:"+cas.getAddress().getPort()+"/cas";
        ReflectionTestUtils.setField(service,"casLoginUrl",local);ReflectionTestUtils.setField(service,"portalLoginUrl",local);
        charges=mock(ChargeService.class);orders=mock(ChargeOrderService.class);idempotency=mock(ChargeIdempotencyService.class);
        var controller=new ChargeRequestController();ReflectionTestUtils.setField(controller,"userCertificateService",service);
        ReflectionTestUtils.setField(controller,"chargeService",charges);ReflectionTestUtils.setField(controller,"chargeOrderService",orders);
        ReflectionTestUtils.setField(controller,"chargeIdempotencyService",idempotency);
        mvc=MockMvcBuilders.standaloneSetup(controller).addInterceptors(new ApiAuthInterceptor(List.of())).setControllerAdvice(new GlobalRestExceptionHandler()).build();
    }
    @AfterEach void close(){if(cas!=null)cas.stop(0);if(clients!=null)clients.closePool();}
    org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request(){return post("/api/card/charge").requestAttr("sessionId",SESSION).header("X-Device-ID","synthetic-device").header("Idempotency-Key","synthetic-key").param("amount","50").param("password",PASSWORD);}
    void assertNoCharge(){verifyNoInteractions(charges,orders,idempotency);verifyNoInteractions(cookies);}
    @Test void noStoredPasswordCanAuthorizeOnlyAfterFreshCasSuccess() throws Exception {
        when(idempotency.begin(eq(USER),anyString(),anyString(),eq(50),anyString())).thenReturn(null);
        when(orders.buildPayloadFingerprint(50,"synthetic-device")).thenReturn("synthetic-fingerprint");
        var order=new ChargeOrderEntity();order.setOrderId("synthetic-order");
        when(orders.createOrder(eq(USER),eq(50),anyString(),anyString(),isNull(),anyString())).thenReturn(order);
        var charge=new ChargeVO();charge.setAlipayURL("https://pay.example.invalid/synthetic");when(charges.chargeRequest(SESSION,50)).thenReturn(charge);
        mvc.perform(request()).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.orderId").value("synthetic-order"));
        assertEquals(1,gets.get());assertEquals(1,posts.get());verifyNoInteractions(cookies);
        verify(certificates).queryUserLoginCertificate(SESSION);verifyNoMoreInteractions(certificates);
        verify(charges).chargeRequest(SESSION,50);
    }
    @Test void noStoredPasswordWithRejectedFreshPasswordCreatesNoOrder() throws Exception {
        currentPassword="synthetic-new-password";
        mvc.perform(request()).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.code").value(ErrorConstantUtils.PASSWORD_INCORRECT));
        assertEquals(1,posts.get());assertNoCharge();
    }
    @Test void matchingCachedPasswordCannotBypassFreshPasswordRejection() throws Exception {
        when(certificates.queryUserLoginCertificate(SESSION)).thenReturn(new User(USER,PASSWORD));currentPassword="synthetic-new-password";
        mvc.perform(request()).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.code").value(ErrorConstantUtils.PASSWORD_INCORRECT));
        assertEquals(1,posts.get());assertNoCharge();
    }
    @Test void casUnavailableFailsBeforeIdempotencyAndOrderCreation() throws Exception {
        pageStatus=503;
        mvc.perform(request()).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.success").value(false));
        assertEquals(1,gets.get());assertEquals(0,posts.get());assertNoCharge();
    }
}
