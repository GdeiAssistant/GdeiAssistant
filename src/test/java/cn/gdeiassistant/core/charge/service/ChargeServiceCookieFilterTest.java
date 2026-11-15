package cn.gdeiassistant.core.charge.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChargeServiceCookieFilterTest {

    @Test
    void filtersCookiesToPaymentGatewayHostOnly() {
        String alipayUrl = "https://unitradeprod.alipay.com/acq/cashier.htm";
        String fallbackGateway = "https://mapi.alipay.com/gateway.do?_input_charset=utf-8";

        assertTrue(ChargeService.matchesPaymentGatewayHost(".alipay.com", alipayUrl, fallbackGateway));
        assertTrue(ChargeService.matchesPaymentGatewayHost("unitradeprod.alipay.com", alipayUrl, fallbackGateway));
        assertFalse(ChargeService.matchesPaymentGatewayHost(".gdei.edu.cn", alipayUrl, fallbackGateway));
        assertFalse(ChargeService.matchesPaymentGatewayHost("epay.gdei.edu.cn", alipayUrl, fallbackGateway));
        assertFalse(ChargeService.matchesPaymentGatewayHost("evilalipay.com", alipayUrl, fallbackGateway));
        assertFalse(ChargeService.matchesPaymentGatewayHost("", alipayUrl, fallbackGateway));
        assertFalse(ChargeService.matchesPaymentGatewayHost(null, alipayUrl, fallbackGateway));
        assertTrue(ChargeService.matchesPaymentGatewayHost("mapi.alipay.com", null, fallbackGateway));
    }
}
