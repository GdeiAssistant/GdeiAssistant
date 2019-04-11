package edu.gdei.gdeiassistant.Enum.UserGroup;

public enum UserGroupEnum {

    ADMIN(0), STUDENT(1), TEST(2), TEACHER(3), SERVICE(4), GRADUATED(5);

    private Integer value;

    public Integer getValue() {
        return value;
    }

    UserGroupEnum(Integer value) {
        this.value = value;
    }
}
