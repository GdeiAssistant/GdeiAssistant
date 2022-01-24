package cn.gdeiassistant.Exception.DatasourceException;

public class RedisNotConfiguredException extends DataSourceException {

    public RedisNotConfiguredException() {
    }

    public RedisNotConfiguredException(String message) {
        super(message);
    }
}
