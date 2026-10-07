package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.pojo.entity.Device;
import cn.gdeiassistant.common.exception.tokenvalidexception.*;
import cn.gdeiassistant.common.tools.utils.JwtUtil;
import cn.gdeiassistant.core.token.service.LoginTokenService;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationAspectFlowTest {
    private final LoginTokenAspect aspect=new LoginTokenAspect();
    private final LoginTokenService tokens=mock(LoginTokenService.class);
    private final JwtUtil jwt=mock(JwtUtil.class);
    private final JoinPoint call=mock(JoinPoint.class);
    private final MethodSignature signature=mock(MethodSignature.class);
    private final MockHttpServletRequest request=new MockHttpServletRequest();
    @BeforeEach void setup(){ReflectionTestUtils.setField(aspect,"loginTokenService",tokens);ReflectionTestUtils.setField(aspect,"jwtUtil",jwt);when(call.getSignature()).thenReturn(signature);when(call.getArgs()).thenReturn(new Object[]{request});when(signature.getParameterNames()).thenReturn(new String[]{"request"});}
    private void validClaim(){var claim=mock(Claim.class);when(claim.asString()).thenReturn("synthetic-session");when(jwt.verifyAndParse("synthetic-token")).thenReturn(Map.of("sessionId",claim));}
    @Test void missingOrLegacyHeadersNeverAuthenticate() {
        for(String header:new String[]{"", "Bearer ", "Basic synthetic"}){request.removeHeader("Authorization");request.addHeader("Authorization",header);assertThrows(TokenExpiredException.class,()->aspect.authenticateToken(call));}
        request.removeHeader("Authorization");request.addHeader("token","synthetic-token");assertThrows(TokenExpiredException.class,()->aspect.authenticateToken(call));verifyNoInteractions(tokens,jwt);
    }
    @Test void validTokenCanUseFilterSessionOrParseItsOwnClaim() throws Exception {
        request.addHeader("Authorization","Bearer synthetic-token");validClaim();aspect.authenticateToken(call);
        assertEquals("synthetic-session",request.getAttribute("sessionId"));verify(tokens).validToken("synthetic-token");
        clearInvocations(jwt,tokens);aspect.authenticateToken(call);verifyNoInteractions(jwt);verify(tokens).validToken("synthetic-token");
    }
    @Test void malformedClaimsAndFailedSignaturesKeepTheirFailureClassification(){
        request.addHeader("Authorization","Bearer synthetic-token");when(jwt.verifyAndParse("synthetic-token")).thenReturn(Map.of());assertThrows(TokenExpiredException.class,()->aspect.authenticateToken(call));
        when(jwt.verifyAndParse("synthetic-token")).thenThrow(new JWTVerificationException("synthetic"));assertThrows(TokenNotMatchingException.class,()->aspect.authenticateToken(call));verifyNoInteractions(tokens);
    }
    @Test void deviceValidationUsesProvidedDeviceOrMobileHeaderAndPropagatesRejection() throws Exception {
        request.addHeader("Authorization","Bearer synthetic-token");request.setAttribute("sessionId","synthetic-session");request.setRemoteAddr("192.0.2.4");
        var device=new Device();device.setUnionID("synthetic-device");when(signature.getParameterNames()).thenReturn(new String[]{"request","device"});when(call.getArgs()).thenReturn(new Object[]{request,device});
        aspect.authenticateToken(call);verify(tokens).validDevice("synthetic-token","192.0.2.4",device);
        aspect.getIPAddress(call);assertEquals("192.0.2.4",device.getIP());request.setAttribute("token","synthetic-token");aspect.updateDevice(call);verify(tokens).saveDevice("synthetic-token",device);
        when(call.getArgs()).thenReturn(new Object[]{request,null});request.addHeader("X-Device-ID"," synthetic-mobile ");
        aspect.authenticateToken(call);verify(tokens).validDevice(eq("synthetic-token"),eq("192.0.2.4"),argThat(d->"synthetic-mobile".equals(d.getUnionID())));
        doThrow(new SuspiciouseRequestException("synthetic rejected device")).when(tokens).validDevice(eq("synthetic-token"),anyString(),any());
        assertThrows(SuspiciouseRequestException.class,()->aspect.authenticateToken(call));
    }
}
