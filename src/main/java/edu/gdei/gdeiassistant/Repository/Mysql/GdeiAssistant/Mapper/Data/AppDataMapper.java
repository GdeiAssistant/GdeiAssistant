package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Data;

import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.SQLBuilder.DataSQLBuilder;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AppDataMapper {

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserProfile")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Profile.ProfileMapper.Profile")
    public Profile selectUserProfile(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserIntroduction")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Profile.ProfileMapper.Introduction")
    public Introduction selectUserIntroduction(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPrivacy")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Privacy.PrivacyMapper.Privacy")
    public Privacy selectUserPrivacy(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserGraduation")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Graduation.GraduationMapper.Graduation")
    public Graduation selectUserGraduation(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserCetNumber")
    @ResultType(CetNumber.class)
    public CetNumber selectUserCetNumber(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserAuthentication")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Authentication.AuthenticationMapper.Authentication")
    public Authentication selectUserAuthentication(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryOrderList")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Delivery.DeliveryMapper.DeliveryOrderDetail")
    public List<DeliveryOrder> selectUserDeliveryOrderList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryTradeList")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Delivery.DeliveryMapper.DeliveryTrade")
    public List<DeliveryTrade> selectUserDeliveryTradeList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserErshouItemList")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Ershou.ErshouMapper.ErshouItem")
    public List<ErshouItem> selectUserErshouItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserLostAndFoundItemList")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.LostAndFound.LostAndFoundMapper.LostAndFoundItem")
    public List<LostAndFoundItem> selectUserLostAndFoundItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserSecretItemList")
    @ResultMap("edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Secret.SecretMapper.SecretContent")
    public List<Secret> selectUserSecretItemList(String username);
}
