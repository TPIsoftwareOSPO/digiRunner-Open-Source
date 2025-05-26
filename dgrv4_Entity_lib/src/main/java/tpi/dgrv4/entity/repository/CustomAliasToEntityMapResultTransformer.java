package tpi.dgrv4.entity.repository;

import org.hibernate.transform.ResultTransformer;

import java.util.HashMap;
import java.util.Map;

public class CustomAliasToEntityMapResultTransformer implements ResultTransformer {

	public static final CustomAliasToEntityMapResultTransformer INSTANCE = new CustomAliasToEntityMapResultTransformer();

	private CustomAliasToEntityMapResultTransformer() {}

	@Override
	public Object transformTuple(Object[] tuple, String[] aliases) {
		Map result = new HashMap(tuple.length);
		for ( int i=0; i<tuple.length; i++ ) {
			String alias = aliases[i];
			if ( alias!=null ) {
				result.put( alias.toLowerCase(), tuple[i] );
			}
		}
		return result;
	}

	public boolean isTransformedValueATupleElement(String[] aliases, int tupleLength) {
		return false;
	}

	private Object readResolve() {
		return INSTANCE;
	}
}
