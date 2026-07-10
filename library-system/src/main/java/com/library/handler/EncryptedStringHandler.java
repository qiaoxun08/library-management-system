package com.library.handler;

import com.library.util.AesUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler：敏感字段自动加解密
 *
 * 使用方式：在 Entity 字段上加 @TypeHandler(type = EncryptedStringHandler.class)
 * 或在 Mapper XML 中指定 typeHandler
 *
 * 数据库存储加密后的 Base64 字符串，读取时自动解密
 */
@MappedTypes(String.class)
@MappedJdbcTypes({JdbcType.VARCHAR, JdbcType.CHAR})
public class EncryptedStringHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        // 写入时加密
        String encrypted = AesUtil.encrypt(parameter);
        ps.setString(i, encrypted);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 读取时解密
        String value = rs.getString(columnName);
        return decryptValue(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decryptValue(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decryptValue(value);
    }

    private String decryptValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            return AesUtil.decrypt(value);
        } catch (Exception e) {
            // 如果解密失败，可能是未加密的旧数据，直接返回原值
            return value;
        }
    }
}
