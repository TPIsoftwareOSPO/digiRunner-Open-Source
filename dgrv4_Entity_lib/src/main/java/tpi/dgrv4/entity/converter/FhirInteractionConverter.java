package tpi.dgrv4.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tpi.dgrv4.entity.constant.CodeEnums;
import tpi.dgrv4.entity.constant.FhirInteraction;

/**
 * FhirInteraction 與資料庫欄位的轉換器
 * 資料庫儲存 code (如 "read", "search-type")
 */
@Converter(autoApply = true)
public class FhirInteractionConverter implements AttributeConverter<FhirInteraction, String> {
	
	@Override
	public String convertToDatabaseColumn(FhirInteraction attribute) {
		return attribute == null ? null : attribute.getCode();
	}

	@Override
	public FhirInteraction convertToEntityAttribute(String dbData) {
		return dbData == null ? null : 
			CodeEnums.fromCode(FhirInteraction.class, dbData);
	}
}
