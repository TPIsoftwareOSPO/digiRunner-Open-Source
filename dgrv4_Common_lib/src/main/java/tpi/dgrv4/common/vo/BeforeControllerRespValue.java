package tpi.dgrv4.common.vo;

public class BeforeControllerRespValue<T> {

	private final String msg;
	private final T value;	

	public BeforeControllerRespValue(T value, String msg) {
		this.value = value;
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
	
	public T getValue() {
		return value;
	}

}