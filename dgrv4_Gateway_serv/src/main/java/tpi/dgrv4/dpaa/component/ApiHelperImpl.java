package tpi.dgrv4.dpaa.component;

import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class ApiHelperImpl implements ApiHelper {

	private TPILogger logger = TPILogger.tl;

	@Override
	public String call(String reqUrl, Map<String, Object> params, HttpMethod method) throws Exception {
		// REVIEW TODO 待實作
		this.logger.info("尚未實作呼叫外部API...");
		return "{\"result\":\"fail\"}";
	}

}
