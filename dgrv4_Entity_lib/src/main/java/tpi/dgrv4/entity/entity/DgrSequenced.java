package tpi.dgrv4.entity.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeqEntityHelper;

public interface DgrSequenced {
	
	@JsonIgnore
	public default long getLongId() {
		return getPrimaryKey().longValue();
	}
	
	@JsonIgnore
	public default String getHexId() {
		RandomLongTypeEnum strategy = DgrSeqEntityHelper.getStrategy(getClass());
		return RandomSeqLongUtil.toHexString(getPrimaryKey(), strategy);
	}
	
	@JsonIgnore
	public Long getPrimaryKey();

}
