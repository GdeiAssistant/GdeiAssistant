package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Data;

import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.SQLBuilder.DataSQLBuilder;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface AppDataMapper {

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserProfile")
    @Results(id = "Profile", value = {
            @Result(property = "kickname", column = "kickname"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "customGenderName", column = "custom_gender"),
            @Result(property = "birthday", column = "birthday"),
            @Result(property = "degree", column = "degree"),
            @Result(property = "faculty", column = "faculty"),
            @Result(property = "major", column = "major"),
            @Result(property = "region", column = "region"),
            @Result(property = "state", column = "state"),
            @Result(property = "city", column = "city")
    })
    public Profile selectUserProfile(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserIntroduction")
    @Results(id = "introduction", value = {
            @Result(property = "introductionContent", column = "introduction")
    })
    public Introduction selectUserIntroduction(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserPrivacy")
    @Results(id = "Privacy", value = {
            @Result(property = "genderOpen", column = "is_gender_open"),
            @Result(property = "regionOpen", column = "is_region_open"),
            @Result(property = "introductionOpen", column = "is_introduction_open"),
            @Result(property = "facultyOpen", column = "is_faculty_open"),
            @Result(property = "majorOpen", column = "is_major_open"),
            @Result(property = "cacheAllow", column = "is_cache_allow")
    })
    public Privacy selectUserPrivacy(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserGraduation")
    @Results(id = "Graduation", value = {
            @Result(property = "program", column = "program")
    })
    public Graduation selectUserGraduation(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserCetNumber")
    @Results(id = "CetNumber", value = {
            @Result(property = "number", column = "number")
    })
    public CetNumber selectUserCetNumber(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserAuthentication")
    @Results(id = "Authentication", value = {
            @Result(property = "identityCode", column = "identity_code"),
            @Result(property = "realname", column = "realname"),
            @Result(property = "identityNumber", column = "identity_number"),
            @Result(property = "schoolNumber", column = "school_number")
    })
    public Authentication selectUserAuthentication(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryOrderList")
    @Results(id = "DeliveryOrder", value = {
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "orderTime", column = "order_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "name", column = "name"),
            @Result(property = "number", column = "number"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "price", column = "price"),
            @Result(property = "company", column = "company"),
            @Result(property = "address", column = "address"),
            @Result(property = "state", column = "state"),
            @Result(property = "remarks", column = "remarks")
    })
    public List<DeliveryOrder> selectUserDeliveryOrderList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserDeliveryTradeList")
    @Results(id = "DeliveryTrade", value = {
            @Result(property = "tradeId", column = "trade_id"),
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "createTime", column = "create_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "state", column = "state")
    })
    public List<DeliveryTrade> selectUserDeliveryTradeList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserErshouItemList")
    @Results(id = "Ershou", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "price", column = "price"),
            @Result(property = "location", column = "location"),
            @Result(property = "type", column = "type"),
            @Result(property = "qq", column = "qq"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "state", column = "state"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    public List<ErshouItem> selectUserErshouItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserLostAndFoundItemList")
    @Results(id = "LostAndFound", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "itemType", column = "item_type"),
            @Result(property = "lostType", column = "lost_type"),
            @Result(property = "state", column = "state"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    public List<LostAndFoundItem> selectUserLostAndFoundItemList(String username);

    @SelectProvider(type = DataSQLBuilder.class, method = "selectUserSecretItemList")
    @Results(id = "Secret", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "theme", column = "theme"),
            @Result(property = "content", column = "content"),
            @Result(property = "type", column = "type"),
            @Result(property = "timer", column = "timer"),
            @Result(property = "state", column = "state"),
            @Result(property = "publishTime", column = "publish_time"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "secretCommentList", column = "id", javaType = List.class
                    , many = @Many(select = "edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Secret.SecretMapper.selectSecretComment"))
    })
    public List<Secret> selectUserSecretItemList(String username);
}
