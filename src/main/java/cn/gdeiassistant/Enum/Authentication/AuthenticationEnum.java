package cn.gdeiassistant.Enum.Authentication;

public enum AuthenticationEnum {

    //中国居民身份证
    MAINLAND_CHINESE_RESIDENT_ID_CARD(0),

    //港澳居民往来内地通行证
    MAINLAND_TRAVEL_PERMIT_FOR_HONGKONG_AND_MACAU_RESIDENTS(1),

    //台湾居民来往大陆通行证
    MAINLAND_TRAVEL_PERMIT_FOR_TAIWAN_RESIDENTS(2),

    //港澳台居民居住证
    HONGKONG_MACAU_TAIWAN_RESIDENCE_PERMIT(3),

    //护照
    PASSPORT(4),

    //外国人永久居留证
    PERMANENT_RESIDENCE_PERMIT_FOR_FOREIGNERS(5),

    //其他
    OTHER(6);

    private final Integer value;

    public Integer getValue() {
        return value;
    }

    AuthenticationEnum(Integer value) {
        this.value = value;
    }
}
