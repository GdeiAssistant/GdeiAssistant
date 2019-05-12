package edu.gdei.gdeiassistant.Enum.UserGroup;

public enum UserGroupEnum {

    ADMIN(1), STUDENT(2), TEST(3), TEACHER(4), SERVICE(5), GRADUATED(6), TRIAL(7);

    private Integer value;

    public Integer getValue() {
        return value;
    }

    UserGroupEnum(Integer value) {
        this.value = value;
    }
}
