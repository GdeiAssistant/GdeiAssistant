package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Data;

import cn.gdeiassistant.Pojo.Entity.*;
import cn.gdeiassistant.Pojo.Entity.*;
import cn.gdeiassistant.Repository.SQL.Mysql.SQLBuilder.GdeiAssistant.DataSQLBuilder;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AppDataMapper {

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserProfile")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper.Profile")
    public Profile selectUserProfile(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserIntroduction")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper.Introduction")
    public Introduction selectUserIntroduction(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPrivacy")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy.PrivacyMapper.Privacy")
    public Privacy selectUserPrivacy(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserGraduation")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Graduation.GraduationMapper.Graduation")
    public Graduation selectUserGraduation(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserCetNumber")
    @ResultType(CetNumber.class)
    public CetNumber selectUserCetNumber(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserAuthentication")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Authentication.AuthenticationMapper.Authentication")
    public Authentication selectUserAuthentication(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryOrderList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Delivery.DeliveryMapper.DeliveryOrderDetail")
    public List<DeliveryOrder> selectUserDeliveryOrderList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryTradeList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Delivery.DeliveryMapper.DeliveryTrade")
    public List<DeliveryTrade> selectUserDeliveryTradeList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserErshouItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Ershou.ErshouMapper.ErshouItem")
    public List<ErshouItem> selectUserErshouItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserLostAndFoundItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.LostAndFound.LostAndFoundMapper.LostAndFoundItem")
    public List<LostAndFoundItem> selectUserLostAndFoundItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserSecretItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Secret.SecretMapper.SecretContent")
    public List<Secret> selectUserSecretItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPhotographItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Photograph.PhotographMapper.PhotographContent")
    public List<Photograph> selectUserPhotographItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserExpressItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Express.ExpressMapper.expressContent")
    public List<Express> selectUserExpresssItemList(String username);
}
