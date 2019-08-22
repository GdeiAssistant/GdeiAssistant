package edu.gdei.gdeiassistant.Enum.UserGroup;

public enum UserGroupEnum {

    ADMIN(1, "管理员"), STUDENT(2, "在校学生"), TEST(3, "测试用户"), TEACHER(4, "教师用户"),

    SERVICE(5, "客服人员"), GRADUATED(6, "毕业学生"), TRIAL(7, "体验用户");

    private Integer value;

    private String name;

    public Integer getValue() {
        return value;
    }

    UserGroupEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static UserGroupEnum getEnumByName(String name) {
        for (UserGroupEnum userGroupEnum : values()) {
            if (userGroupEnum.name().equalsIgnoreCase(name)) {
                return userGroupEnum;
            }
        }
        return null;
    }
}
