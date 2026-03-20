package cn.gdeiassistant.core.data.mapper;

import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.cetquery.pojo.entity.CetNumberEntity;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.common.pojo.Entity.*;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.common.db.DataSQLBuilder;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

public interface AppDataMapper {

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserProfile")
    @ResultMap("cn.gdeiassistant.core.profile.mapper.ProfileMapper.Profile")
    ProfileEntity selectUserProfile(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserIntroduction")
    @ResultMap("cn.gdeiassistant.core.profile.mapper.ProfileMapper.Introduction")
    Introduction selectUserIntroduction(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPrivacy")
    @ResultMap("cn.gdeiassistant.core.privacy.mapper.PrivacyMapper.Privacy")
    PrivacyEntity selectUserPrivacy(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserCetNumber")
    @ResultMap("cn.gdeiassistant.core.cetquery.mapper.CetQueryMapper.CetNumberEntity")
    CetNumberEntity selectUserCetNumber(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryOrderList")
    @ResultMap("cn.gdeiassistant.core.delivery.mapper.DeliveryMapper.DeliveryOrderDetail")
    List<DeliveryOrderEntity> selectUserDeliveryOrderList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryTradeList")
    @ResultMap("cn.gdeiassistant.core.delivery.mapper.DeliveryMapper.DeliveryTrade")
    List<DeliveryTradeEntity> selectUserDeliveryTradeList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserErshouItemList")
    @ResultMap("cn.gdeiassistant.core.secondhand.mapper.SecondhandMapper.MarketplaceItemEntity")
    List<MarketplaceItemEntity> selectUserErshouItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserLostAndFoundItemList")
    @ResultMap("cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper.LostAndFoundItem")
    List<LostAndFoundItemEntity> selectUserLostAndFoundItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserSecretItemList")
    @ResultMap("cn.gdeiassistant.core.secret.mapper.SecretMapper.SecretContent")
    List<SecretContentEntity> selectUserSecretItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPhotographItemList")
    @ResultMap("cn.gdeiassistant.core.photograph.mapper.PhotographMapper.PhotographContent")
    List<PhotographEntity> selectUserPhotographItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserExpressItemList")
    @ResultMap("cn.gdeiassistant.core.express.mapper.ExpressMapper.expressContent")
    List<ExpressEntity> selectUserExpressItemList(String username);
}
