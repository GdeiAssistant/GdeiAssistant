package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Data;

import cn.gdeiassistant.Pojo.Entity.*;
import cn.gdeiassistant.Repository.SQL.Mysql.SQLBuilder.GdeiAssistant.DataSQLBuilder;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface AppDataMapper {

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserProfile")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper.Profile")
    Profile selectUserProfile(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserIntroduction")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper.Introduction")
    Introduction selectUserIntroduction(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPrivacy")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy.PrivacyMapper.Privacy")
    Privacy selectUserPrivacy(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserCetNumber")
    @ResultType(CetNumber.class)
    CetNumber selectUserCetNumber(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryOrderList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Delivery.DeliveryMapper.DeliveryOrderDetail")
    List<DeliveryOrder> selectUserDeliveryOrderList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryTradeList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Delivery.DeliveryMapper.DeliveryTrade")
    List<DeliveryTrade> selectUserDeliveryTradeList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserErshouItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Ershou.ErshouMapper.ErshouItem")
    List<ErshouItem> selectUserErshouItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserLostAndFoundItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.LostAndFound.LostAndFoundMapper.LostAndFoundItem")
    List<LostAndFoundItem> selectUserLostAndFoundItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserSecretItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Secret.SecretMapper.SecretContent")
    List<Secret> selectUserSecretItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPhotographItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Photograph.PhotographMapper.PhotographContent")
    List<Photograph> selectUserPhotographItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserExpressItemList")
    @ResultMap("cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Express.ExpressMapper.expressContent")
    List<Express> selectUserExpresssItemList(String username);
}
