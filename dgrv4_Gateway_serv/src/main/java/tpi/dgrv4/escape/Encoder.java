package tpi.dgrv4.escape;

public class Encoder {
	public String encodeForSQL(Codec codec, String input) {
//		return ESAPI.encoder().encodeForSQL(new OracleCodec(), input);
		return input;
	}
	
	public String encodeForHTML(String input) {
//		return ESAPI.encoder().encodeForHTML(input);
		return input;
	}
}
