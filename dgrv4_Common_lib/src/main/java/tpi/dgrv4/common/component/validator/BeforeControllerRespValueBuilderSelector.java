package tpi.dgrv4.common.component.validator;

import tpi.dgrv4.common.component.validator.BeforeControllerRespValueRtnCodeBuilder;

public class BeforeControllerRespValueBuilderSelector<V> {

	public BeforeControllerRespValueMsgBuilder<V> buildByMsg() {
		return new BeforeControllerRespValueMsgBuilder<>();
	}

	public BeforeControllerRespValueRtnCodeBuilder<V> buildByRtnCode(String locale) {
		return new BeforeControllerRespValueRtnCodeBuilder<>(locale);
	}

}