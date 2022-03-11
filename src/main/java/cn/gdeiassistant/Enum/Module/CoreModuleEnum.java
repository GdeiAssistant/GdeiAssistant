package cn.gdeiassistant.Enum.Module;

public enum CoreModuleEnum {

    MYSQL("Mysql", "/sql/mysql.properties"),
    MONGODB("Mongodb", "/mongodb/mongodb-config.properties"),
    REDIS("Redis", "/redis/redis.properties");

    CoreModuleEnum(String name, String location) {
        this.name = name;
        this.location = location;
    }

    private String name;

    public String getName() {
        return name;
    }

    private String location;

    public String getLocation() {
        return location;
    }
}
