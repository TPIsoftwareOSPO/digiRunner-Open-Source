package tpi.dgrv4.entity.repository;

import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.ResultTransformer;

public class CustomTransformers {
	
	public static final CustomAliasToEntityMapResultTransformer ALIAS_TO_ENTITY_MAP =
			CustomAliasToEntityMapResultTransformer.INSTANCE;
	
	public static ResultTransformer aliasToBean(Class target) {
		return new AliasToBeanResultTransformer(target);
	}

}
