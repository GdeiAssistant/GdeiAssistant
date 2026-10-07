package cn.gdeiassistant.core.email.service;

import cn.gdeiassistant.common.config.redis.RedisConfig;
import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.filter.JwtSessionIdFilter;
import cn.gdeiassistant.common.interceptor.ApiAuthInterceptor;
import cn.gdeiassistant.common.pojo.encryption.AESEncryptConfig;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.verificationcode.VerificationCodeDaoImpl;
import cn.gdeiassistant.common.tools.springutils.EmailUtils;
import cn.gdeiassistant.common.tools.springutils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.utils.JwtUtil;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler;
import cn.gdeiassistant.core.capability.impl.SmtpEmailVerificationSender;
import cn.gdeiassistant.core.email.controller.EmailController;
import cn.gdeiassistant.core.email.mapper.EmailMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import com.auth0.jwt.interfaces.Claim;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Loopback SMTP + isolated MySQL + real Redis, through the production HTTP controllers.
 * Only JWT verification/account lookup are synthetic; no external mail or live accounts.
 */
@EnabledIfEnvironmentVariable(named="GDEI_UPGRADE_TEST_JDBC_BASE", matches="jdbc:mysql://127\\.0\\.0\\.1:.+")
@EnabledIfEnvironmentVariable(named="GDEI_REDIS_TEST_PORT", matches="\\d+")
class EmailIsolatedEndToEndTest {
    private static final String SCHEMA="gdei_email_e2e_test";
    private static final String FIRST="synthetic@example.invalid", SECOND="updated@example.invalid";
    private LettuceConnectionFactory redisFactory;
    private VerificationCodeDaoImpl codes;
    private SqlSession sql;
    private JdbcTemplate database;
    private MockMvc http;
    private LoopbackSmtp smtp;
    private DriverManagerDataSource source(String schema) {
        return new DriverManagerDataSource(System.getenv("GDEI_UPGRADE_TEST_JDBC_BASE")+schema+"?sslMode=DISABLED&allowPublicKeyRetrieval=true", "root", System.getenv("GDEI_UPGRADE_TEST_PASSWORD"));
    }
    @BeforeEach void setup() throws Exception {
        new JdbcTemplate(source("mysql")).execute("CREATE DATABASE IF NOT EXISTS "+SCHEMA);
        database=new JdbcTemplate(source(SCHEMA));
        database.execute("CREATE TABLE IF NOT EXISTS email(username VARCHAR(30) PRIMARY KEY,email VARCHAR(500) NOT NULL,gmt_create DATETIME,gmt_modified DATETIME)");
        database.update("DELETE FROM email");
        var encryption=new AESEncryptConfig(); encryption.setPrivateKey("synthetic-fixture-key-only");
        new StringEncryptUtils().setEncryptConfig(encryption);
        var configuration=new Configuration(new Environment("isolated",new JdbcTransactionFactory(),source(SCHEMA)));
        configuration.getTypeHandlerRegistry().register(MybatisEncryptionTypeHandler.class);
        configuration.addMapper(EmailMapper.class);
        sql=new SqlSessionFactoryBuilder().build(configuration).openSession(true);
        redisFactory=new LettuceConnectionFactory("127.0.0.1",Integer.parseInt(System.getenv("GDEI_REDIS_TEST_PORT")));
        redisFactory.afterPropertiesSet();redisFactory.start();
        var redis=new RedisConfig().redisTemplate(redisFactory);
        var redisUtils=new RedisDaoUtils();ReflectionTestUtils.setField(redisUtils,"redisTemplate",redis);
        codes=new VerificationCodeDaoImpl();ReflectionTestUtils.setField(codes,"redisDaoUtils",redisUtils);
        codes.deleteEmailVerificationCode(FIRST);codes.deleteEmailVerificationCode(SECOND);
        smtp=new LoopbackSmtp();
        var mail=new JavaMailSenderImpl();mail.setHost("127.0.0.1");mail.setPort(smtp.port());mail.setDefaultEncoding("UTF-8");
        mail.getJavaMailProperties().setProperty("mail.smtp.connectiontimeout","3000");
        mail.getJavaMailProperties().setProperty("mail.smtp.timeout","3000");
        var utility=new EmailUtils();ReflectionTestUtils.setField(utility,"javaMailSender",mail);
        var sender=new SmtpEmailVerificationSender();ReflectionTestUtils.setField(sender,"emailUtils",utility);
        sender.setSmtpHost("127.0.0.1");sender.setSenderEmail("sender@example.invalid");sender.setSmtpPassword("synthetic-fixture-only");
        var certificates=mock(UserCertificateService.class);when(certificates.getUserLoginCertificate("synthetic-session")).thenReturn(new User("synthetic-owner"));
        var service=new EmailService();ReflectionTestUtils.setField(service,"emailMapper",sql.getMapper(EmailMapper.class));
        ReflectionTestUtils.setField(service,"verificationCodeDao",codes);ReflectionTestUtils.setField(service,"emailVerificationSender",sender);
        ReflectionTestUtils.setField(service,"userCertificateService",certificates);
        var controller=new EmailController();ReflectionTestUtils.setField(controller,"emailService",service);
        var jwt=mock(JwtUtil.class);var claim=mock(Claim.class);when(claim.asString()).thenReturn("synthetic-session");
        when(jwt.verifyAndParse("synthetic-token")).thenReturn(Map.of("sessionId",claim));
        var users=mock(UserMapper.class);var account=new CampusAccountView();account.setStatus("ACTIVE");
        when(users.selectUser("synthetic-owner")).thenReturn(account);
        var filter=new JwtSessionIdFilter();ReflectionTestUtils.setField(filter,"jwtUtil",jwt);
        ReflectionTestUtils.setField(filter,"userCertificateService",certificates);ReflectionTestUtils.setField(filter,"userMapper",users);
        http=MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalRestExceptionHandler())
                .addFilters(filter).addInterceptors(new ApiAuthInterceptor(List.of())).build();
    }
    @AfterEach void cleanup() throws Exception {
        if(codes!=null){codes.deleteEmailVerificationCode(FIRST);codes.deleteEmailVerificationCode(SECOND);}
        if(redisFactory!=null)redisFactory.destroy();if(sql!=null)sql.close();if(smtp!=null)smtp.close();
        new StringEncryptUtils().setEncryptConfig(null);
        new JdbcTemplate(source("mysql")).execute("DROP DATABASE IF EXISTS "+SCHEMA);
    }
    private MockHttpServletRequestBuilder authenticated(MockHttpServletRequestBuilder request) {return request.header("Authorization","Bearer synthetic-token");}
    private int send(String address) throws Exception {
        http.perform(authenticated(post("/api/email/verification").param("email",address))).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        var message=smtp.messages.poll(3,TimeUnit.SECONDS);assertNotNull(message);
        assertEquals(address,message.getAllRecipients()[0].toString());
        assertEquals("广东二师助手邮箱验证码",message.getSubject());
        var matcher=Pattern.compile("验证码为：(\\d{6})").matcher(body(message));assertTrue(matcher.find());
        int code=Integer.parseInt(matcher.group(1));assertEquals(code,codes.queryEmailVerificationCode(address));return code;
    }
    private String body(Part part) throws Exception {
        Object value=part.getContent();if(value instanceof Multipart multi){StringBuilder result=new StringBuilder();for(int i=0;i<multi.getCount();i++)result.append(body(multi.getBodyPart(i)));return result.toString();}
        return String.valueOf(value);
    }
    @Test void sendBindReadUpdateAndUnbindPersistEncryptedEmail() throws Exception {
        http.perform(authenticated(get("/api/email/status"))).andExpect(jsonPath("$.data").doesNotExist());
        int first=send(FIRST);
        http.perform(authenticated(post("/api/email/bind").param("email",FIRST).param("randomCode",String.valueOf(first)))).andExpect(jsonPath("$.success").value(true));
        assertNull(codes.queryEmailVerificationCode(FIRST));
        String stored=database.queryForObject("SELECT email FROM email WHERE username='synthetic-owner'",String.class);
        assertTrue(stored.startsWith("v1:"));assertFalse(stored.contains(FIRST));assertEquals(FIRST,StringEncryptUtils.decryptString(stored));
        http.perform(authenticated(get("/api/email/status"))).andExpect(jsonPath("$.data").value(FIRST));
        int second=send(SECOND);
        http.perform(authenticated(post("/api/email/bind").param("email",SECOND).param("randomCode",String.valueOf(second)))).andExpect(jsonPath("$.success").value(true));
        assertEquals(1,database.queryForObject("SELECT COUNT(*) FROM email",Integer.class));
        http.perform(authenticated(get("/api/email/status"))).andExpect(jsonPath("$.data").value(SECOND));
        http.perform(authenticated(post("/api/email/unbind"))).andExpect(jsonPath("$.success").value(true));
        http.perform(authenticated(post("/api/email/unbind"))).andExpect(jsonPath("$.success").value(false));
        assertEquals(0,database.queryForObject("SELECT COUNT(*) FROM email",Integer.class));
    }
    @Test void wrongAddressWrongCodeAndReplayCannotChangeBinding() throws Exception {
        int code=send(FIRST),wrong=code==999999?100000:code+1;
        http.perform(authenticated(post("/api/email/bind").param("email",SECOND).param("randomCode",String.valueOf(code)))).andExpect(jsonPath("$.success").value(false));
        http.perform(authenticated(post("/api/email/bind").param("email",FIRST).param("randomCode",String.valueOf(wrong)))).andExpect(jsonPath("$.success").value(false));
        assertEquals(code,codes.queryEmailVerificationCode(FIRST));assertEquals(0,database.queryForObject("SELECT COUNT(*) FROM email",Integer.class));
        http.perform(authenticated(post("/api/email/bind").param("email",FIRST).param("randomCode",String.valueOf(code)))).andExpect(jsonPath("$.success").value(true));
        http.perform(authenticated(post("/api/email/bind").param("email",FIRST).param("randomCode",String.valueOf(code)))).andExpect(jsonPath("$.success").value(false));
        assertEquals(1,database.queryForObject("SELECT COUNT(*) FROM email",Integer.class));
    }
    @Test void unauthenticatedOrInvalidRequestsCannotSendMailOrWrite() throws Exception {
        http.perform(post("/api/email/verification").param("email",FIRST)).andExpect(status().isUnauthorized());
        for(String invalid:List.of("", "bad-address", "x".repeat(55)+"@example.invalid"))
            http.perform(authenticated(post("/api/email/verification").param("email",invalid))).andExpect(jsonPath("$.success").value(false));
        http.perform(authenticated(post("/api/email/bind").param("email",FIRST))).andExpect(jsonPath("$.success").value(false));
        http.perform(authenticated(post("/api/email/bind").param("email",FIRST).param("randomCode","invalid"))).andExpect(jsonPath("$.success").value(false));
        assertTrue(smtp.messages.isEmpty());assertNull(codes.queryEmailVerificationCode(FIRST));
        assertEquals(0,database.queryForObject("SELECT COUNT(*) FROM email",Integer.class));
    }
    @Test void smtpRejectionInvalidatesCodeAndReturnsFailure() throws Exception {
        smtp.reject=true;
        http.perform(authenticated(post("/api/email/verification").param("email",FIRST))).andExpect(jsonPath("$.success").value(false));
        assertNull(codes.queryEmailVerificationCode(FIRST));assertTrue(smtp.messages.isEmpty());
        assertEquals(0,database.queryForObject("SELECT COUNT(*) FROM email",Integer.class));
    }
    /** Minimal protocol fixture, bound exclusively to loopback; stores synthetic MIME in memory. */
    private static class LoopbackSmtp implements AutoCloseable {
        private final ServerSocket server=new ServerSocket(0,10,InetAddress.getLoopbackAddress());
        private final ExecutorService worker=Executors.newSingleThreadExecutor();
        final BlockingQueue<MimeMessage> messages=new LinkedBlockingQueue<>();
        volatile boolean reject;
        LoopbackSmtp() throws IOException {worker.submit(()->{while(!server.isClosed())try(Socket socket=server.accept()){
            socket.setSoTimeout(5000);var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));
            var output=new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),StandardCharsets.UTF_8),true);
            output.print("220 localhost synthetic fixture\r\n");output.flush();String command;
            while((command=reader.readLine())!=null){
                if(command.startsWith("DATA")){
                    output.print("354 End with dot\r\n");output.flush();var content=new StringBuilder();String line;
                    while((line=reader.readLine())!=null&&!line.equals("."))content.append(line.startsWith("..")?line.substring(1):line).append("\r\n");
                    if(reject)output.print("451 Synthetic delivery rejected\r\n");
                    else{messages.add(new MimeMessage(Session.getInstance(new Properties()),new ByteArrayInputStream(content.toString().getBytes(StandardCharsets.UTF_8))));output.print("250 Accepted\r\n");}
                }else if(command.startsWith("QUIT")){output.print("221 Bye\r\n");output.flush();break;}
                else output.print("250 OK\r\n");output.flush();
            }
        }catch(Exception error){if(!server.isClosed())throw new IllegalStateException("Loopback SMTP fixture failed",error);}});}
        int port(){return server.getLocalPort();}
        public void close() throws Exception {server.close();worker.shutdownNow();assertTrue(worker.awaitTermination(5,TimeUnit.SECONDS));}
    }
}
