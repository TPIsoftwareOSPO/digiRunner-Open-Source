package tpi.dgrv4.gateway.vo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomLinkedHashMap<K,V> extends LinkedHashMap<K,V> implements Serializable{
	private static final long serialVersionUID = 1L;
    private int maxSize = 5;
	
	public CustomLinkedHashMap() {
		
	}
	
	public CustomLinkedHashMap(int maxSize) {
		this.maxSize = maxSize;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}
}
