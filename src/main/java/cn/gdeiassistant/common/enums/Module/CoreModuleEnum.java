package cn.gdeiassistant.common.enums.Module;

public enum CoreModuleEnum {

    MYSQL("Mysql", "application.yml (spring.datasource.*)"),
    MONGODB("Mongodb", "application.yml (spring.data.mongodb.*)"),
    REDIS("Redis", "application.yml (spring.redis.*)");

    CoreModuleEnum(String name, String location) {
        this.name = name;
        this.location = location;
    }

    private final String name;

    public String getName() {
        return name;
    }

    private final String location;

    public String getLocation() {
        return location;
    }
}
