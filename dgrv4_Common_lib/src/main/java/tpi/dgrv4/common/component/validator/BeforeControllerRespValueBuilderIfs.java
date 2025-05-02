package tpi.dgrv4.common.component.validator;

import tpi.dgrv4.common.vo.BeforeControllerRespValue;

public interface BeforeControllerRespValueBuilderIfs<V> {

	BeforeControllerRespValue<V> build() throws IllegalArgumentException;

}