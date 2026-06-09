package tpi.dgrv4.entity.converter;

import jakarta.persistence.AttributeConverter;
import tpi.dgrv4.entity.constant.CodeEnum;
import tpi.dgrv4.entity.constant.CodeEnums;

public abstract class AbstractCodeEnumConverter<E extends Enum<E> & CodeEnum<String>>
        implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    protected AbstractCodeEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CodeEnums.fromCode(enumClass, dbData);
    }
}
