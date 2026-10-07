package cn.gdeiassistant.core.phone.service;

import cn.gdeiassistant.common.exception.verificationexception.*;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.verificationcode.VerificationCodeDao;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.phone.pojo.dto.PhoneBindDTO;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.verificationcode.service.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhoneServiceTest {
    private final PhoneMapper mapper = mock(PhoneMapper.class);
    private final VerificationCodeDao codes = mock(VerificationCodeDao.class);
    private final VerificationCodeService sms = mock(VerificationCodeService.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final PhoneService service = new PhoneService();
    private static final String PHONE = "13800000000";
    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "phoneMapper", mapper);
        ReflectionTestUtils.setField(service, "verificationCodeDao", codes);
        ReflectionTestUtils.setField(service, "verificationCodeService", sms);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("owner"));
    }
    @Test void boundPhoneIsMaskedAndAbsentPhoneIsNull() {
        assertNull(service.queryUserPhone("session"));
        PhoneEntity entity = new PhoneEntity(); entity.setUsername("owner"); entity.setPhone(PHONE);
        when(mapper.selectPhone("owner")).thenReturn(entity);
        var view = service.queryUserPhone("session");
        assertEquals("138********", view.getPhone()); assertEquals("owner", view.getUsername());
        entity.setPhone(null); assertEquals("", service.queryUserPhone("session").getPhone());
    }
    @Test void domesticAndInternationalCodesUseCorrectSenderAndAreOneTime() throws Exception {
        for (int country : new int[]{86, 852}) {
            service.getPhoneVerificationCode(country, PHONE);
            ArgumentCaptor<Integer> code = ArgumentCaptor.forClass(Integer.class);
            verify(codes).savePhoneVerificationCode(eq(country), eq(PHONE), code.capture());
            assertTrue(code.getValue() >= 100000 && code.getValue() <= 999999);
            if (country == 86) verify(sms).sendChinaPhoneVerificationCodeSms(code.getValue(), PHONE);
            else verify(sms).sendGlobalPhoneVerificationCodeSms(code.getValue(), country, PHONE);
            when(codes.consumePhoneVerificationCode(country, PHONE, code.getValue())).thenReturn(true, false);
            service.checkVerificationCode(country, PHONE, code.getValue());
            assertThrows(VerificationCodeInvalidException.class,
                    () -> service.checkVerificationCode(country, PHONE, code.getValue()));
        }
    }
    @Test void deliveryFailureInvalidatesPendingCode() throws Exception {
        doThrow(new SendSMSException("synthetic outage")).when(sms).sendChinaPhoneVerificationCodeSms(anyInt(), eq(PHONE));
        assertThrows(SendSMSException.class, () -> service.getPhoneVerificationCode(86, PHONE));
        ArgumentCaptor<Integer> code = ArgumentCaptor.forClass(Integer.class);
        verify(codes).savePhoneVerificationCode(eq(86), eq(PHONE), code.capture());
        verify(codes).consumePhoneVerificationCode(86, PHONE, code.getValue());
    }
    @Test void bindingUsesAuthenticatedOwnerAndUpdatesOrInsertsOnlyItsRecord() {
        PhoneBindDTO dto = new PhoneBindDTO(); dto.setCode(86); dto.setPhone(PHONE);
        service.attachUserPhone("session", dto); verify(mapper).insertPhone("owner", 86, PHONE);
        PhoneEntity existing = new PhoneEntity(); existing.setUsername("owner");
        when(mapper.selectPhone("owner")).thenReturn(existing);
        dto.setCode(852); service.attachUserPhone("session", dto);
        assertEquals(852, existing.getCode()); assertEquals(PHONE, existing.getPhone());
        verify(mapper).updatePhone(existing); service.unAttachUserPhone("session"); verify(mapper).deletePhone("owner");
        when(mapper.selectPhone("owner")).thenReturn(null); service.unAttachUserPhone("session");
        verify(mapper, times(1)).deletePhone("owner");
    }
}
