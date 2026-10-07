package cn.gdeiassistant.integration;

import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.integration.edu.pojo.EduSessionCredential;
import cn.gdeiassistant.integration.card.CardClient;
import cn.gdeiassistant.integration.library.LibraryClient;
import cn.gdeiassistant.integration.cas.CasClient;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import cn.gdeiassistant.common.redis.cookiestore.CookieStoreDao;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.queryexception.TimeStampIncorrectException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Real HTTP clients consume only synthetic loopback responses, never campus endpoints. */
class CampusHttpFixtureTest {
    record Reply(int status,String text,Map<String,String> headers) {}
    record Seen(String method,String uri,String body,String cookie) {}
    HttpServer server; String base; HttpClientUtils pool; CookieStoreDao cookies;
    Map<String,Reply> replies; List<Seen> seen;
    EduSystemClient edu;CardClient card;LibraryClient library;CasClient cas;
    @BeforeEach void setup() throws Exception {
        replies=new ConcurrentHashMap<>();seen=new CopyOnWriteArrayList<>();server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{
            seen.add(new Seen(exchange.getRequestMethod(),exchange.getRequestURI().toString(),new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8),exchange.getRequestHeaders().getFirst("Cookie")));
            Reply reply=replies.getOrDefault(exchange.getRequestMethod()+" "+exchange.getRequestURI().getPath(),new Reply(404,"synthetic missing fixture",Map.of()));
            exchange.getResponseHeaders().set("Content-Type","text/html; charset=UTF-8");reply.headers().forEach((key,value)->exchange.getResponseHeaders().set(key,value));
            byte[] body=reply.text().getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(reply.status(),body.length);try(var output=exchange.getResponseBody()){output.write(body);}
        });server.start();base="http://127.0.0.1:"+server.getAddress().getPort();
        cookies=mock(CookieStoreDao.class);pool=new HttpClientUtils();pool.setCookieStoreDao(cookies);
        edu=new EduSystemClient();card=new CardClient();library=new LibraryClient();cas=new CasClient();
        for(var client:List.of(edu,card,library))ReflectionTestUtils.setField(client,"httpClientUtils",pool);
        ReflectionTestUtils.setField(edu,"JWGL_BASE",base);ReflectionTestUtils.setField(card,"ECARD_BASE",base);
        for(String field:List.of("OPAC_RENEW_BASE","OPAC_SEARCH_BASE","FIVEREAD_M","FIVEREAD_MC"))ReflectionTestUtils.setField(library,field,base);
        ReflectionTestUtils.setField(cas,"CAS_LOGIN_URL",base+"/cas/login");
    }
    @AfterEach void close(){server.stop(0);pool.closePool();}
    void reply(String method,String path,int status,String text){replies.put(method+" "+path,new Reply(status,text,Map.of()));}
    void redirect(String method,String path,String location){replies.put(method+" "+path,new Reply(302,"redirect",Map.of("Location",location)));}
    EduSessionCredential credential(){var c=new EduSessionCredential();c.setUsername("synthetic");c.setNumber("synthetic-number");c.setKeycode("synthetic-key");c.setTimestamp(1L);return c;}
    @Test void gradeFlowPreservesCookiesAndSubmitsExactAcademicYearAsFormField() throws Exception {
        replies.put("GET /cas_verify.aspx",new Reply(200,"verified",Map.of("Set-Cookie","synthetic=1; Path=/")));
        reply("GET","/xs_main.aspx",200,"<title>student</title>");reply("GET","/xscj_gc.aspx",200,"<select name='ddlXN'><option>2024-2025</option></select><input name='__VIEWSTATE' value='synthetic state'>");
        assertEquals("2024-2025",edu.fetchGradeListPage("session",credential()).select("option").text());
        assertTrue(seen.stream().filter(s->s.uri().contains("xs_main")).findFirst().orElseThrow().cookie().contains("synthetic=1"));
        reply("POST","/xscj_gc.aspx",200,"<table id='datelist'><tr><td>synthetic course</td></tr></table>");
        assertEquals("synthetic course",edu.fetchGradeByYear("session",credential(),"state & token","2024-2025").getElementById("datelist").text());
        Seen posted=seen.get(seen.size()-1);assertTrue(posted.body().contains("__VIEWSTATE=state+%26+token"));assertTrue(posted.body().contains("ddlXN=2024-2025"));
        verify(cookies,times(2)).saveCookieStore(eq("session"),any());
    }
    @Test void gradeRedirectAndStatusFailuresAreClassifiedWithoutTryingLaterPages() {
        redirect("GET","/cas_verify.aspx","/loginTs/loginTs_yzsb.html");assertThrows(TimeStampIncorrectException.class,()->edu.fetchGradeListPage("session",credential()));assertEquals(1,seen.size());
        redirect("GET","/cas_verify.aspx","/login");assertThrows(PasswordIncorrectException.class,()->edu.fetchGradeListPage("session",credential()));
        reply("GET","/cas_verify.aspx",200,"您登陆的系统已经很长时间没有操作了，为安全起见请重新登录后再进行操作！");assertThrows(TimeStampIncorrectException.class,()->edu.fetchGradeListPage("session",credential()));
        reply("GET","/cas_verify.aspx",503,"unavailable");assertThrows(ServerErrorException.class,()->edu.fetchGradeListPage("session",credential()));
        verify(cookies,times(4)).saveCookieStore(eq("session"),any());
        var manager=(org.apache.http.impl.conn.PoolingHttpClientConnectionManager)ReflectionTestUtils.getField(pool,"connectionManager");
        assertEquals(0,manager.getTotalStats().getLeased(),"failed responses must release pooled connections");
    }
    @Test void scheduleRejectsMissingFieldsAndSwitchesTermWithViewState() throws Exception {
        reply("GET","/cas_verify.aspx",200,"verified");reply("GET","/xs_main.aspx",200,"student");reply("GET","/xskbcx.aspx",200,"<table id='Table1'></table>");
        assertThrows(ServerErrorException.class,()->edu.fetchScheduleDocument("session",credential()));
        var week=new cn.gdeiassistant.common.tools.utils.WeekUtils();week.setYear(2026);week.setTerm(1);
        reply("GET","/xskbcx.aspx",200,"<input name='__VIEWSTATE' value='synthetic'><select name='xnd'><option value='2026-2027'>current</option><option selected value='2025-2026'>old</option></select><select name='xqd'><option value='1'>one</option><option selected value='2'>two</option></select>");
        reply("POST","/xskbcx.aspx",200,"<table id='Table1'><tr><td>course<br>room</td></tr></table>");
        assertEquals("course$info$room",edu.fetchScheduleDocument("session",credential()).getElementById("Table1").text());assertTrue(seen.get(seen.size()-1).body().contains("xqd=1"));
    }
    @Test void cardRequestsHandleDatesPaginationBytesAndFailureResponses() throws Exception {
        for(String path:List.of("/CardManage/CardInfo/BasicInfo","/CardManage/CardInfo/TrjnList","/CardManage/CardInfo/LossCard","/Account/GetNumKeyPadImg","/Account/GetCheckCodeImg"))reply(path.endsWith("LossCard")?"POST":"GET",path,200,"<title>synthetic</title>");
        reply("POST","/CardManage/CardInfo/SetCardLost",200,"{\"success\":true}");
        assertEquals("synthetic",card.fetchCardBasicInfoDocument("session").title());assertEquals("synthetic",card.fetchCardTrjnListDocument("session",1,2).title());assertTrue(seen.get(seen.size()-1).uri().contains("pageindex=2"));
        assertEquals("synthetic",card.fetchCardTrjnListByDateDocument("session",2026,10,7,2).title());assertEquals("synthetic",card.fetchLossCardPageDocument("session").title());
        assertArrayEquals("<title>synthetic</title>".getBytes(StandardCharsets.UTF_8),card.fetchKeyPadImage("session"));assertTrue(card.fetchCheckcodeImage("session","/Account/GetCheckCodeImg").length>0);
        assertTrue(card.submitSetCardLost("session","synthetic","abcd").contains("success"));assertTrue(seen.get(seen.size()-1).body().contains("checkCode=abcd"));
        reply("GET","/CardManage/CardInfo/BasicInfo",503,"outage");assertThrows(ServerErrorException.class,()->card.fetchCardBasicInfoDocument("session"));verify(cookies,times(8)).saveCookieStore(eq("session"),any());
    }
    @Test void librarySearchEncodesInputAndBorrowAuthenticationClassifiesPasswordFailure() throws Exception {
        reply("GET","/showhome/searchlist/opacSearchList",200,"<title>synthetic catalog</title>");reply("GET","/showhome/searchdetail/opacSearchDetail",200,"<title>synthetic detail</title>");reply("GET","/showhome/searchrenew/opacSearchRenew",200,"{\"result\":1,\"msg\":\"synthetic renewed\"}");
        assertEquals("synthetic catalog",library.fetchCollectionListPage(2,"a & b").title());assertTrue(seen.get(seen.size()-1).uri().contains("search=a+%26+b"));assertTrue(seen.get(seen.size()-1).uri().contains("page=2"));
        assertEquals("synthetic detail",library.fetchCollectionDetailPage("synthetic & path","book",null,null,null,null).title());assertEquals(1,library.renewBook("session","synthetic","code").getResult());
        reply("GET","/705",200,"mobile");reply("GET","/user/login/showLogin.jspx",200,"<title>移动图书馆服务登录</title>");redirect("POST","/irdUser/login/opac/opacLogin.jspx",base+"/center");reply("GET","/center",200,"<title>个人中心</title>");reply("GET","/cmpt/opac/opacLink.jspx",200,"<table id='books'><tr><td>synthetic book</td></tr></table>");
        assertEquals("synthetic book",library.fetchBorrowedBooksPage("session","synthetic","synthetic").getElementById("books").text());
        reply("GET","/center",200,"<title>移动图书馆服务登录</title>");assertThrows(PasswordIncorrectException.class,()->library.fetchBorrowedBooksPage("session","synthetic","synthetic"));
        reply("GET","/showhome/searchlist/opacSearchList",503,"outage");assertThrows(ServerErrorException.class,()->library.fetchCollectionListPage(1,"synthetic"));
    }
    @Test void casKeepsSchoolRedirectBoundaryAndRejectsMissingTokensOrUnsafeRedirects() throws Exception {
        reply("GET","/cas/login",200,"<input id='tokens' value='synthetic'><input id='stamp' value='synthetic'>");reply("POST","/cas/login",200,"<a href='https://jwgl.gdei.edu.cn/synthetic'>next</a>");
        assertEquals("https://jwgl.gdei.edu.cn/synthetic",cas.login("synthetic","synthetic",base+"/service").getServiceUrl());assertTrue(seen.get(seen.size()-1).body().contains("tokens=synthetic"));
        reply("POST","/cas/login",200,"<a href='https://untrusted.invalid/steal'>next</a>");assertThrows(ServerErrorException.class,()->cas.login("synthetic","synthetic",base+"/service"));
        reply("GET","/cas/login",200,"<title>unexpected page</title>");assertThrows(ServerErrorException.class,()->cas.login("synthetic","synthetic",base+"/service"));
        assertTrue(seen.stream().allMatch(s->s.uri().startsWith("/cas/login")));
    }
}
