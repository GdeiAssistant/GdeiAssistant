package cn.gdeiassistant.core.ipaddress.service;
import cn.gdeiassistant.core.ipaddress.mapper.IPAddressMapper;
import cn.gdeiassistant.core.capability.ip.IPLocationResolver;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.common.enums.ipaddress.IPAddressEnum;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class IPAddressServiceFlowTest {
    @Test void historyOwnershipAndPublicAreaCoarseningRemainDistinct(){
        var db=mock(IPAddressMapper.class);var login=mock(UserCertificateService.class);var resolver=mock(IPLocationResolver.class);var service=new IPAddressService();
        ReflectionTestUtils.setField(service,"ipAddressMapper",db);ReflectionTestUtils.setField(service,"userCertificateService",login);ReflectionTestUtils.setField(service,"ipLocationResolver",resolver);
        when(login.getUserLoginCertificate("sid")).thenReturn(new User("synthetic-owner"));
        assertTrue(service.getSelfUserAddressRecord("sid",0,0,10).isEmpty());
        var record=new IPAddressRecord();record.setIp("192.0.2.1");when(db.selectIPAddressRecordByType("synthetic-owner",0,0,10)).thenReturn(List.of(record));assertSame(record,service.getSelfUserAddressRecord("sid",0,0,10).get(0));
        assertEquals("-",service.getOtherUserLatestPostTypeIPAddress("peer").getArea());
        when(db.selectLatestIPAddressRecordByType("peer",IPAddressEnum.POST.getValue())).thenReturn(record);
        record.setCountry("中国");record.setProvince("广东");assertEquals("广东",service.getOtherUserLatestPostTypeIPAddress("peer").getArea());
        for(String province:List.of("香港","澳门","台湾")){record.setProvince(province);assertEquals("中国"+province,service.getOtherUserLatestPostTypeIPAddress("peer").getArea());}
        record.setProvince(null);assertEquals("-",service.getOtherUserLatestPostTypeIPAddress("peer").getArea());
        record.setCountry("synthetic foreign country");assertEquals("synthetic foreign country",service.getOtherUserLatestPostTypeIPAddress("peer").getArea());
        when(db.selectLatestIPAddressRecordByType("synthetic-owner",IPAddressEnum.POST.getValue())).thenReturn(record);assertSame(record,service.getSelfUserLatestPostTypeIPAddress("sid"));
        when(resolver.resolve("192.0.2.1")).thenReturn(record);assertSame(record,service.getInfoByIPAddress("192.0.2.1"));service.saveIPAddress(record);verify(db).insertIPAddressRecord(record);
    }
}
