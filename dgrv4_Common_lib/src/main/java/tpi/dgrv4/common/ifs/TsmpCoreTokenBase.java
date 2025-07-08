package tpi.dgrv4.common.ifs;

import org.springframework.stereotype.Component;

@Component
public class TsmpCoreTokenBase {
	public String encrypt(String originalString) {
		return originalString;
	}
	public String decrypt(String encodedString) {
		return encodedString;
	}
}
