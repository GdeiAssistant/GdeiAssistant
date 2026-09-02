package cn.gdeiassistant.common.typehandler;

import cn.gdeiassistant.common.pojo.Alias.DataEncryption;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(DataEncryption.class)
public class MybatisEncryptionTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, StringEncryptUtils.encryptString(parameter));
        } catch (Exception e) {
            throw new SQLException("MyBatis encrypted field write failed", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String r = rs.getString(columnName);
        try {
            return r == null ? null : StringEncryptUtils.decryptString(r);
        } catch (Exception e) {
            throw new SQLException("MyBatis encrypted field read failed", e);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String r = rs.getString(columnIndex);
        try {
            return r == null ? null : StringEncryptUtils.decryptString(r);
        } catch (Exception e) {
            throw new SQLException("MyBatis encrypted field read failed", e);
        }
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String r = cs.getString(columnIndex);
        try {
            return r == null ? null : StringEncryptUtils.decryptString(r);
        } catch (Exception e) {
            throw new SQLException("MyBatis encrypted field read failed", e);
        }
    }
}
