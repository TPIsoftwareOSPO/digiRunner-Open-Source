package tpi.dgrv4.common.component.validator;

import java.util.Objects;

import tpi.dgrv4.common.vo.BeforeControllerRespValue;

public abstract class BeforeControllerRespValueBaseBuilder<V, T extends BeforeControllerRespValueBaseBuilder<V, T>> //
	implements BeforeControllerRespValueBuilderIfs<V> {

	private V value;

	@SuppressWarnings("unchecked")
	public T value(V value) {
		this.value = value;
		return (T) this;
	}
	
	@Override
	public BeforeControllerRespValue<V> build() throws IllegalArgumentException {
		if (Objects.isNull(this.value)) {
			throw new IllegalArgumentException("value is empty");
		}
		return doBuild();
	}

	protected abstract BeforeControllerRespValue<V> doBuild();

	protected V getValue() {
		return this.value;
	}

}