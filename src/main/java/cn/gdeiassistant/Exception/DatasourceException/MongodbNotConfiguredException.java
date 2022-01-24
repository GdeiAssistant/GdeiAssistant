package cn.gdeiassistant.Exception.DatasourceException;

public class MongodbNotConfiguredException extends DataSourceException{

    public MongodbNotConfiguredException() {
    }

    public MongodbNotConfiguredException(String message) {
        super(message);
    }
}
