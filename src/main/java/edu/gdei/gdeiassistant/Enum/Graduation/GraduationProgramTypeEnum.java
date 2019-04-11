package edu.gdei.gdeiassistant.Enum.Graduation;

public enum GraduationProgramTypeEnum {

    UPGRADE_TO_GRADUATED_ACCOUNT(0),

    DELETE_ACCOUNT_AND_DATA(1);

    private Integer type;

    public Integer getType() {
        return type;
    }

    GraduationProgramTypeEnum(Integer type) {
        this.type = type;
    }
}
