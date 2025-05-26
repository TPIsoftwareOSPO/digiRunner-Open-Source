package tpi.dgrv4.dpaa.component.apptJob;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.ApiHelper_TSMP;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpDpCallapi;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpCallapiDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class CallTsmpApiJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private ApiHelper_TSMP apiHelper;
	private TsmpDpCallapiDao tsmpDpCallapiDao;

	@Autowired
	public CallTsmpApiJob(TsmpDpApptJob tsmpDpApptJob, ApiHelper_TSMP apiHelper, TsmpDpCallapiDao tsmpDpCallapiDao,
			ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.apiHelper = apiHelper;
		this.tsmpDpCallapiDao = tsmpDpCallapiDao;
	}

	@Override
	public String runApptJob() throws Exception {
		TsmpDpCallapi ca = getCallApi();
		step("API_PARAM");//"已取得呼叫API的參數檔"
		
		String reqUrl = ca.getReqUrl();
		Map<String, Object> params = getParamsFromCA(ca);
		// TODO 預設使用POST, 應由參數決定
		HttpMethod method = HttpMethod.POST;
		String resp = apiHelper.call(reqUrl, params, method);
		
		step("END_OF_CALL");//"呼叫結束"
		
		String respMsg = truncateRespMsg(resp, 4000);	// tsmp_dp_callapi.resp_msg : varchar(4000)
		ca.setRespMsg(respMsg);
		tsmpDpCallapiDao.save(ca);
		
		return "SUCCESS";//"成功"
	}

	private TsmpDpCallapi getCallApi() throws Exception {
		String inParams = getTsmpDpApptJob().getInParams();
		try {
			Long callApiId = Long.valueOf(inParams);
			Optional<TsmpDpCallapi> opt = this.tsmpDpCallapiDao.findById(callApiId);
			return opt.orElseThrow(() -> {
				return new Exception();
			});
		} catch (Exception e) {
			throw new Exception("Unable to find TsmpDpCallApi with ID: " + inParams);
		}
	}

	private Map<String, Object> getParamsFromCA(TsmpDpCallapi ca) {
		Map<String, Object> params = new HashMap<>();
		params.put("tokenUrl", ca.getTokenUrl());
		params.put("signBlockUrl", ca.getSignCodeUrl());
		params.put("auth", ca.getAuth());
		params.put("reqBody", ca.getReqMsg());
		return params;
	}

	public static String truncateRespMsg(String respMsg, int maximumBytes) {
		if (!StringUtils.isEmpty(respMsg)) {
			char[] cAry = respMsg.toCharArray();

			int byteCnt = 0;
			for(int i = 0; i < cAry.length; i++) {
				byteCnt += ( ( (int) cAry[i] ) <= 128 ? 1 : 2 );
				if (byteCnt > maximumBytes) { return respMsg.substring(0, i); }
				if (byteCnt == maximumBytes) { return respMsg.substring(0, (i + 1)); }
			}
		}
		return respMsg;
	}

	@Override
	public TsmpDpApptJob set(TsmpDpApptJob job) {
		Long callApiId = null;
		try {
			Map<String, Object> params = parseInParams(job.getInParams());
			String reqUrl = apiHelper.getString("url", params, true);
			String reqMsg = apiHelper.getString("reqBody", params, true);
			String tokenUrl = apiHelper.getString("tokenUrl", params, true);
			String signCodeUrl = apiHelper.getString("signBlockUrl", params, true);
			String auth = apiHelper.getString("auth", params, true);
			
			TsmpDpCallapi ca = new TsmpDpCallapi();
			ca.setReqUrl(reqUrl);
			ca.setReqMsg(reqMsg);
			ca.setTokenUrl(tokenUrl);
			ca.setSignCodeUrl(signCodeUrl);
			ca.setAuth(auth);
			ca.setCreateDateTime(DateTimeUtil.now());
			ca.setCreateUser(job.getCreateUser());
			ca = tsmpDpCallapiDao.save(ca);
			callApiId = ca.getCallapiId();

			if (callApiId != null) {
				job.setInParams(String.valueOf(callApiId));
				return super.set(job);
			}
		} catch (Exception e) {
			logger.debug("" + e);
		}
		return null;
	}

	public Map<String, Object> parseInParams(String inParams) throws Exception {
		Map<String, Object> params = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		ObjectNode node = (ObjectNode) om.readTree(inParams);
		checkAndThrow("url", node, (urlNode) -> {
			params.put("url", urlNode.asText());
		});
		checkAndThrow("reqBody", node, (reqBodyNode) -> {
			params.put("reqBody", reqBodyNode.asText());
		});
		checkAndThrow("auth", node, (authNode) -> {
			params.put("auth", authNode.asText());
		});
		checkAndDo("tokenUrl", node, (tUrlNode) -> {
			params.put("tokenUrl", tUrlNode.asText());
		});
		checkAndDo("signBlockUrl", node, (sUrlNode) -> {
			params.put("signBlockUrl", sUrlNode.asText());
		});
		return params;
	}

	/**
	 * node有key時才執行consumer
	 * @param key
	 * @param node
	 * @param consumer
	 */
	private void checkAndDo(String key, ObjectNode node, Consumer<JsonNode> consumer) {
		try {
			checkOrThrow(key, node, false, consumer);
		} catch (Exception e) {}
	}

	/**
	 * node有key時才執行consumer, 否則拋錯
	 * @param key
	 * @param node
	 * @param consumer
	 */
	private void checkAndThrow(String key, ObjectNode node, Consumer<JsonNode> consumer) throws Exception {
		checkOrThrow(key, node, true, consumer);
	}

	private void checkOrThrow(String key, ObjectNode node, boolean isThrow, Consumer<JsonNode> consumer) throws Exception {
		if (!node.has(key)) {
			if (isThrow) {
				throw new Exception("InParams require '" + key + "' key!");
			}
		} else {
			consumer.accept((JsonNode) node.get(key));
		}
	}
}
