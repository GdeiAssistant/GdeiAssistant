package cn.gdeiassistant.common.typehandler;

import cn.gdeiassistant.common.pojo.Alias.DataEncryption;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(DataEncryption.class)
public class MybatisEncryptionTypeHandler extends BaseTypeHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(MybatisEncryptionTypeHandler.class);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, StringEncryptUtils.encryptString(parameter));
        } catch (Exception e) {
            logger.error("MyBatis 加密字段写入失败", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String r = rs.getString(columnName);
        try {
            return r == null ? null : StringEncryptUtils.decryptString(r);
        } catch (Exception e) {
            logger.error("MyBatis 解密字段失败，columnName={}", columnName, e);
        }
        return null;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String r = rs.getString(columnIndex);
        try {
            return r == null ? null : StringEncryptUtils.decryptString(r);
        } catch (Exception e) {
            logger.error("MyBatis 解密字段失败，columnIndex={}", columnIndex, e);
        }
        return null;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String r = cs.getString(columnIndex);
        try {
            return r == null ? null : StringEncryptUtils.decryptString(r);
        } catch (Exception e) {
            logger.error("MyBatis 解密 CallableStatement 字段失败，columnIndex={}", columnIndex, e);
        }
        return null;
    }
}
