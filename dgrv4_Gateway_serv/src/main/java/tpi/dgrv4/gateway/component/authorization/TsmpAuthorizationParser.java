package tpi.dgrv4.gateway.component.authorization;

import tpi.dgrv4.gateway.vo.TsmpAuthorization;

public interface TsmpAuthorizationParser {

	public TsmpAuthorization parse() throws Exception;

}