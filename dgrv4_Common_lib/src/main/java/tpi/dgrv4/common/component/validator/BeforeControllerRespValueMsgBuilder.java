package tpi.dgrv4.common.component.validator;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.vo.BeforeControllerRespValue;

public class BeforeControllerRespValueMsgBuilder<V> extends //
	BeforeControllerRespValueBaseBuilder<V, BeforeControllerRespValueMsgBuilder<V>> {

	private String msg;

	public BeforeControllerRespValueMsgBuilder<V> msg(String msg) {
		this.msg = msg;
		return this;
	}
	
	@Override
	protected BeforeControllerRespValue<V> doBuild() {
		if (StringUtils.isEmpty(this.msg)) {
			this.msg = new String();
		}
		return new BeforeControllerRespValue<>(getValue(), this.msg);
	}

}