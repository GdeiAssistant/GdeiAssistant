package cn.gdeiassistant.common.redis.ExportData;

import cn.gdeiassistant.common.tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class ExportDataDaoImpl implements ExportDataDao {

    private final String EXPORT_PREFIX = "EXPORT_DATA_";

    private final String EXPORTING_PREFIX = "EXPORTING_DATA_";

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    @Override
    public String QueryExportingDataToken(String username) {
        return redisDaoUtils.get(StringEncryptUtils.sha256HexString(EXPORTING_PREFIX + username));
    }

    @Override
    public void RemoveExportingDataToken(String username) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(EXPORTING_PREFIX + username));
    }

    @Override
    public void SaveExportingDataToken(String username, String token) {
        redisDaoUtils.set(StringEncryptUtils.sha256HexString(EXPORTING_PREFIX + username)
                , token);
        //一小时后以任务超时处理
        redisDaoUtils.expire(StringEncryptUtils.sha256HexString(EXPORTING_PREFIX + username)
                , 1, TimeUnit.HOURS);
    }

    @Override
    public String QueryExportDataToken(String username) {
        return redisDaoUtils.get(StringEncryptUtils.sha256HexString(EXPORT_PREFIX + username));
    }

    @Override
    public void SaveExportDataToken(String username, String token) {
        redisDaoUtils.set(StringEncryptUtils.sha256HexString(EXPORT_PREFIX + username), token);
        redisDaoUtils.expire(StringEncryptUtils.sha256HexString(EXPORT_PREFIX + username)
                , 24, TimeUnit.HOURS);
    }
}
