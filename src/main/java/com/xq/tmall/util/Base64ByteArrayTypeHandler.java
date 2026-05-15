package com.xq.tmall.util;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class Base64ByteArrayTypeHandler extends BaseTypeHandler<byte[]> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, byte[] parameter, JdbcType jdbcType) throws SQLException {
        ps.setBytes(i, parameter); // 将 byte[] 直接设置到数据库
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String base64String = rs.getString(columnName);
        return base64String != null ? Base64.getDecoder().decode(base64String) : null; // 将 Base64 字符串转换为 byte[]
    }

    @Override
    public byte[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String base64String = rs.getString(columnIndex);
        return base64String != null ? Base64.getDecoder().decode(base64String) : null; // 将 Base64 字符串转换为 byte[]
    }

    @Override
    public byte[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String base64String = cs.getString(columnIndex);
        return base64String != null ? Base64.getDecoder().decode(base64String) : null; // 将 Base64 字符串转换为 byte[]
    }
}
