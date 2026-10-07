package cn.gdeiassistant.core.objectstorage.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface StoredAssetMapper {
    @Insert("INSERT INTO stored_asset(bucket,object_key,owner_id,status,content_type,byte_length,updated_at) VALUES(#{bucket},#{key},#{owner},'PENDING',#{type},#{length},CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE owner_id=VALUES(owner_id),content_type=VALUES(content_type),byte_length=VALUES(byte_length),status=IF(status='READY','READY','PENDING'),updated_at=CURRENT_TIMESTAMP")
    void pending(@Param("bucket") String bucket, @Param("key") String key, @Param("owner") String owner, @Param("type") String type, @Param("length") long length);
    @Update("UPDATE stored_asset SET status='READY',updated_at=CURRENT_TIMESTAMP WHERE bucket=#{bucket} AND object_key=#{key} AND status IN ('PENDING','READY')")
    int ready(@Param("bucket") String bucket, @Param("key") String key);
    @Insert("INSERT IGNORE INTO stored_asset(bucket,object_key,status,content_type,byte_length,updated_at) VALUES(#{bucket},#{key},#{status},#{type},#{length},CURRENT_TIMESTAMP)")
    void discovered(@Param("bucket") String bucket, @Param("key") String key, @Param("status") String status, @Param("type") String type, @Param("length") long length);
    @Select("SELECT status,content_type,byte_length,updated_at FROM stored_asset WHERE bucket=#{bucket} AND object_key=#{key}")
    Map<String,Object> find(@Param("bucket") String bucket, @Param("key") String key);
    @Update("INSERT INTO stored_asset(bucket,object_key,status,byte_length,updated_at) VALUES(#{bucket},#{key},'DELETING',0,CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE status='DELETING',updated_at=CURRENT_TIMESTAMP")
    void deleting(@Param("bucket") String bucket, @Param("key") String key);
    @Delete("DELETE FROM stored_asset WHERE bucket=#{bucket} AND object_key=#{key} AND status='MISSING' AND updated_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 HOUR)")
    int expireMissing(@Param("bucket") String bucket, @Param("key") String key);
    @Delete("DELETE FROM stored_asset WHERE bucket=#{bucket} AND object_key=#{key}")
    void remove(@Param("bucket") String bucket, @Param("key") String key);
    @Select("SELECT bucket,object_key FROM stored_asset WHERE status IN ('PENDING','DELETING') AND updated_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 MINUTE) ORDER BY updated_at LIMIT 50")
    List<Map<String,Object>> cleanupCandidates();
    @Select("SELECT status FROM stored_asset WHERE bucket=#{bucket} AND object_key=#{key} FOR UPDATE")
    Map<String,Object> lock(@Param("bucket") String bucket, @Param("key") String key);
    @Select("SELECT status FROM stored_asset WHERE bucket=#{bucket} AND object_key=#{key} AND status IN ('PENDING','DELETING') AND updated_at < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 20 MINUTE) FOR UPDATE")
    Map<String,Object> lockAbandoned(@Param("bucket") String bucket, @Param("key") String key);
    @Select("<script>SELECT object_key FROM stored_asset WHERE bucket=#{bucket} AND status='READY' AND object_key IN <foreach collection='keys' item='key' open='(' separator=',' close=')'>#{key}</foreach> ORDER BY updated_at DESC LIMIT 1</script>")
    String firstReadyKey(@Param("bucket") String bucket, @Param("keys") java.util.List<String> keys);

}
