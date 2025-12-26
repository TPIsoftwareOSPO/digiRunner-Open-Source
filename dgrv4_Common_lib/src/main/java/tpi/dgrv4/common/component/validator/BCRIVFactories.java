package tpi.dgrv4.common.component.validator;

import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class BCRIVFactories {

	static BCRIVFactoryIfs getVFactory(BeforeControllerRespItem item) {
		final String type = item.getType();
		if ("string".equalsIgnoreCase(type)) {
			return new BCRIStringVFactory(item);
		} else if ("collection".equalsIgnoreCase(type)) {
			return new BCRICollectionVFactory(item);
		} else if ("int".equalsIgnoreCase(type)) {
			return new BCRIIntVFactory(item);
		} else if ("map".equalsIgnoreCase(type)) {
			return new BCRIMapVFactory(item);
		}
		throw new IllegalArgumentException("Unknown item type: " + type);
	}

}