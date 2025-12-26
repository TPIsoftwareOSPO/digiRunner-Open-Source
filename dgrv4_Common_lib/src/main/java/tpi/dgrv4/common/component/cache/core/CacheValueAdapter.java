package tpi.dgrv4.common.component.cache.core;

public interface CacheValueAdapter {

	public String getIdentifier();

	public byte[] serialize(Object obj);

	public Object deserialize(byte[] data);

}
