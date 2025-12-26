package tpi.dgrv4.common.exceptions;

@SuppressWarnings("serial")
public class BcryptParamDecodeException extends Exception {

	public BcryptParamDecodeException(String message, String paramString) {
		super("Bcrypt decoding failed! " + message + " -> " + paramString);
	}

}
