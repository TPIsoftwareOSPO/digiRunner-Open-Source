package tpi.dgrv4.gateway.vo;

public class BeforeControllerRespValue<T> {

	private final T value;

	private final String msg;

	public BeforeControllerRespValue(T value, String msg) {
		this.value = value;
		this.msg = msg;
	}

	public T getValue() {
		return value;
	}

	public String getMsg() {
		return msg;
	}

}