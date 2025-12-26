package tpi.dgrv4.dpaa.config;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;
import org.hibernate.type.descriptor.jdbc.LongVarcharJdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

import java.sql.Types;

public class CustomPostgreSQLDialect extends PostgreSQLDialect {

    public CustomPostgreSQLDialect() {
        super();
    }

    @Override
    protected String columnType(int sqlTypeCode) {
        if (SqlTypes.BLOB == sqlTypeCode) {
            return "bytea";
        }
        if (SqlTypes.CLOB == sqlTypeCode) {
            return "text";
        }
        return super.columnType(sqlTypeCode);
    }

    @Override
    protected String castType(int sqlTypeCode) {
        if (SqlTypes.BLOB == sqlTypeCode) {
            return "bytea";
        }
        if (SqlTypes.CLOB == sqlTypeCode) {
            return "text";
        }
        return super.castType(sqlTypeCode);
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        JdbcTypeRegistry jdbcTypeRegistry = typeContributions.getTypeConfiguration().getJdbcTypeRegistry();
        jdbcTypeRegistry.addDescriptor(Types.BLOB, BinaryJdbcType.INSTANCE);
        jdbcTypeRegistry.addDescriptor(Types.CLOB, LongVarcharJdbcType.INSTANCE);
    }
}