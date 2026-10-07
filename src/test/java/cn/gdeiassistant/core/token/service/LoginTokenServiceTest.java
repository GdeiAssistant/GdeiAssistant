package cn.gdeiassistant.core.token.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.exception.tokenvalidexception.*;
import cn.gdeiassistant.common.pojo.config.JWTConfig;
import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.common.redis.logintoken.LoginTokenDao;
import cn.gdeiassistant.common.tools.utils.StringEncryptUtils;
import cn.gdeiassistant.core.ipaddress.service.IPAddressService;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoginTokenServiceTest {
    @Mock LoginTokenDao dao;
    @Mock IPAddressService locations;
    @InjectMocks LoginTokenService service;

    @BeforeEach
    void config() {
        JWTConfig c = new JWTConfig();
        c.setSecret("synthetic-test-signing-key");
        ReflectionTestUtils.setField(service, "jwtConfig", c);
    }

    @Test
    void issuedAccessAndRefreshTokensHaveCorrectClaimsAndLifetimes() throws Exception {
        AccessToken a = service.getAccessToken("session", "owner");
        service.validToken(a.getSignature());
        var c = service.parseToken(a.getSignature());
        assertEquals("owner", c.get("username").asString());
        assertEquals("session", c.get("sessionId").asString());
        assertEquals(7 * 86400000L, a.getExpireTime() - a.getCreateTime());
        verify(dao).insertAccessToken(a);
        RefreshToken r = service.getRefreshToken(a);
        assertEquals(StringEncryptUtils.sha256HexString(a.getSignature()), r.getSignature());
        assertEquals(30 * 86400000L, r.getExpireTime() - r.getCreateTime());
        verify(dao).insertRefreshToken(r);
    }

    @Test
    void refreshKeepsOwnerAndUsesNewSession() throws Exception {
        AccessToken a = service.getAccessToken("old-session", "owner");
        RefreshToken r = service.getRefreshToken(a);
        when(dao.queryRefreshToken(r.getSignature())).thenReturn(r);
        var result = service.refreshToken("new-session", r.getSignature());
        var c = service.parseToken(result.getAccessToken().getSignature());
        assertEquals("owner", c.get("username").asString());
        assertEquals("new-session", c.get("sessionId").asString());
        verify(dao).deleteAccessToken(a.getSignature());
        verify(dao).deleteRefreshToken(r.getSignature());
    }

    @Test
    void missingRefreshCannotIssueTokens() {
        assertThrows(
                TokenNotMatchingException.class, () -> service.refreshToken("session", "missing"));
        verify(dao, never()).insertAccessToken(any());
    }

    @Test
    void expiredAccessMayRefreshButTamperedAccessCannot() throws Exception {
        String expired =
                JWT.create()
                        .withIssuer("gdeiassistant")
                        .withClaim("username", "owner")
                        .withClaim("expireTime", 1L)
                        .sign(Algorithm.HMAC256("synthetic-test-signing-key"));
        RefreshToken r = new RefreshToken();
        r.setAccessTokenSignature(expired);
        when(dao.queryRefreshToken("refresh")).thenReturn(r);
        assertThrows(TokenExpiredException.class, () -> service.validToken(expired));
        assertEquals(
                "owner",
                service.refreshToken("new-session", "refresh").getAccessToken().getUsername());
        r.setAccessTokenSignature(
                JWT.create()
                        .withIssuer("gdeiassistant")
                        .withClaim("expireTime", Long.MAX_VALUE)
                        .sign(Algorithm.HMAC256("other-key")));
        assertThrows(
                TokenNotMatchingException.class, () -> service.refreshToken("session", "refresh"));
    }

    @Test
    void expirationRevokesBothTokenKinds() throws Exception {
        AccessToken a = service.getAccessToken("session", "owner");
        when(dao.queryAccessToken(a.getSignature())).thenReturn(a);
        service.expireToken(a.getSignature());
        verify(dao).deleteAccessToken(a.getSignature());
        verify(dao).deleteRefreshToken(StringEncryptUtils.sha256HexString(a.getSignature()));
        assertThrows(TokenNotMatchingException.class, () -> service.expireToken("missing"));
    }

    Device device(String id, String ip) {
        Device d = new Device();
        d.setUnionID(id);
        d.setIP(ip);
        return d;
    }

    IPAddressRecord location(String province) {
        IPAddressRecord r = new IPAddressRecord();
        r.setCountry("synthetic");
        r.setProvince(province);
        return r;
    }

    @Test
    void knownDeviceOrSameIpIsAcceptedWithoutGeolocation() throws Exception {
        when(dao.queryDeviceData("token")).thenReturn(device("known", "192.0.2.1"));
        service.validDevice("token", "192.0.2.2", device("known", "192.0.2.2"));
        service.validDevice("token", "192.0.2.1", device("other", "192.0.2.1"));
        verifyNoInteractions(locations);
    }

    @Test
    void newDeviceRequiresMatchingStoredIpLocation() throws Exception {
        when(dao.queryDeviceData("token")).thenReturn(device("old", "192.0.2.1"));
        when(locations.getInfoByIPAddress("192.0.2.1")).thenReturn(location("A"));
        when(locations.getInfoByIPAddress("192.0.2.2")).thenReturn(location("B"));
        assertThrows(
                SuspiciouseRequestException.class,
                () -> service.validDevice("token", "192.0.2.2", device("new", "192.0.2.2")));
        verify(dao).deleteAccessToken("token");
    }

    @Test
    void sameProvinceIsAccepted() throws Exception {
        when(dao.queryDeviceData("token")).thenReturn(device("old", "192.0.2.1"));
        when(locations.getInfoByIPAddress(anyString())).thenReturn(location("A"));
        service.validDevice("token", "192.0.2.2", device("new", "192.0.2.2"));
        verify(dao, never()).deleteAccessToken(anyString());
    }

    @Test
    void missingLocationRevokesMismatchingDeviceAndMissingDeviceIsExpired() {
        when(dao.queryDeviceData("token")).thenReturn(device("old", "192.0.2.1"));
        assertThrows(
                SuspiciouseRequestException.class,
                () -> service.validDevice("token", "192.0.2.2", device("new", "192.0.2.2")));
        verify(dao).deleteRefreshToken(StringEncryptUtils.sha256HexString("token"));
        assertThrows(
                TokenExpiredException.class,
                () -> service.validDevice("missing", "192.0.2.2", device("new", "192.0.2.2")));
    }

    @Test
    void deviceStorageDelegatesToTokenDao() {
        Device d = device("id", "192.0.2.1");
        service.saveDevice("token", d);
        verify(dao).saveDeviceData("token", d);
    }
}
