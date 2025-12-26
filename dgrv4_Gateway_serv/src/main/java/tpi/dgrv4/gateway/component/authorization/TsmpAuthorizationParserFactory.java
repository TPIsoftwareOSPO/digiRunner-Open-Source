package tpi.dgrv4.gateway.component.authorization;

import org.springframework.util.StringUtils;

import tpi.dgrv4.gateway.keeper.TPILogger;

public class TsmpAuthorizationParserFactory {

	private static TPILogger logger;

	public TsmpAuthorizationParser getParser(String authorization) {
		if (!StringUtils.hasLength(authorization)) {
			throwException("Authorization is empty");
		}

		String[] sAry = authorization.split("\\s");
		if (sAry.length != 2) {
			throwException("Authorization must be split by a blank");
		}

		String tokenType = sAry[0];
		authorization = sAry[1];

		if ("DGRK".equalsIgnoreCase(tokenType)) {
			return new TsmpAuthorizationDGRKParser(authorization);
		} else if (tokenType.matches("[B|b]earer")) {
			return new TsmpAuthorizationBearerParser(authorization);
		} else {
			throwException("Unknown token type: " + tokenType);
		}

		return null;
	}

	private void throwException(String msg) throws IllegalArgumentException {
		this.logger.error(msg);
		throw new IllegalArgumentException(msg);
	}

	public static void setLogger(TPILogger logger) {
		TsmpAuthorizationParserFactory.logger = logger;
	}

}