package tpi.dgrv4.gateway.component.authorization;

public abstract class TsmpAuthorizationParserImpl implements TsmpAuthorizationParser {

	protected final String[] infos;

	public TsmpAuthorizationParserImpl(String authorization) {
		this.infos = split(authorization);
	}

	/**
	 * 切分authorization
	 * @return
	 */
	protected String[] split(String authorization) {
		return authorization.split("\\.");
	}

}