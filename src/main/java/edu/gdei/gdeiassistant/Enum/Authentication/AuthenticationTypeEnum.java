package edu.gdei.gdeiassistant.Enum.Authentication;

public enum AuthenticationTypeEnum {

    CAS_SYSTEM(0), MAINLAND_IDENTITY_CARD(1), MAINLAND_PASSPORT(2),

    MAINLAND_MILITARY_OFFICER_CARD(3), MAINLAND_SOLDIER_CARD(4),

    MAINLAND_FOREIGNER_PERMANENT_RESIDENCE_PERMIT(5),

    MAINLAND_HONGKONG_AND_MACAO_AND_TAIWAN_RESIDENT_CARD(6),

    HONGKONG_AND_MACAO_IDENTITY_CARD(7), HONGKONG_AND_MACAO_EXIT_AND_ENTRY_PERMIT(8),

    TAIWAN_EXIT_AND_ENTRY_PERMIT(9), FOREIGN_PASSPORT(10), PHONE(11);

    private Integer type;

    AuthenticationTypeEnum(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
