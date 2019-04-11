package edu.gdei.gdeiassistant.Enum.UserGroup;

public enum UserGroupTypeEnum {

    ADMIN(0), STUDENT(1), TEST(2), TEACHER(3), SERVICE(4), GRADUATED(5);

    private Integer type;

    public Integer getType() {
        return type;
    }

    UserGroupTypeEnum(Integer type) {
        this.type = type;
    }
}
