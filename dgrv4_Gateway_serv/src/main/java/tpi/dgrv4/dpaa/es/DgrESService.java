package tpi.dgrv4.dpaa.es;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;
/**
 * 請在使用其他method之前 先使用isConnected() 一定要!!!一定!!!
 * @author zoele
 *
 */
@Service
public class DgrESService {
	private TsmpSettingService tsmpSettingService;
	private ObjectMapper objectMapper;
	
	int index = 0;

	@Autowired
	public DgrESService(TsmpSettingService tsmpSettingService, ObjectMapper objectMapper) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.objectMapper = objectMapper;
	}

	/**
	 * 請在使用其他method之前 先使用此方法確認連線
	 * 
	 * @return
	 */
	
	public boolean isConnected() {
		// 測試連線
		String[] arrEsUrl = getEsConnUrl();
		String[] arrIdPwd = getEsConnIDPWD();
		boolean isConnection = false;
		for (; index < arrEsUrl.length; index++) {
			isConnection = this.checkConnection(arrEsUrl[index]);
			if (isConnection) {
				break;
			}
		}
		if (!isConnection) {
			TPILogger.tl.info("es connection is fail");
			index = 0;
			return false;
		}
		return true;
	}

	public Map<String, String> getHeader() {
		Map<String, String> header = new HashMap<>();
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("Authorization", "Basic " + getEsConnIDPWD()[index]);
		return header;
	}

	public String[] getEsConnIDPWD() {
		// 檢查是否有ES_ID_PWD參數
		if (!StringUtils.hasText(getTsmpSettingService().getVal_ES_ID_PWD())) {
			TPILogger.tl.info("tsmp_setting ES_ID_PWD no value");
		}
		// 檢查ES_ID_PWD參數內容值個數和URL個數有沒有吻合

		String[] arrIdPwd = getTsmpSettingService().getVal_ES_ID_PWD().split(",");
		if (arrIdPwd.length != getEsConnUrl().length) {
			TPILogger.tl.info("ES connection info and id pwd is not mapping");
		}
		return arrIdPwd;
	}

	public String[] getEsConnUrl() {
		// 檢查是否有ES_URL參數
		if (!StringUtils.hasText(getTsmpSettingService().getVal_ES_URL())) {
			TPILogger.tl.info("tsmp_setting ES_URL no value");
		}
		String[] arrEsUrl = getTsmpSettingService().getVal_ES_URL().split(",");
		return arrEsUrl;

	}

	public boolean checkConnection(String strUrl) {
		try (Socket socket = new Socket()) {
			int timeout = getTsmpSettingService().getVal_ES_TEST_TIMEOUT();
			URL url = new URL(strUrl);
			int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
			socket.connect(new InetSocketAddress(url.getHost(), port), timeout);
			return true;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return false;
		}
	}

	public boolean openIndex(String indexName) {
		try {

			String url = String.format("%s%s/_open", getEsConnUrl()[index], indexName);
			HttpRespData resp = HttpUtil.httpReqByRawData(url, HttpMethod.POST.toString(), null, getHeader(), false);
			TPILogger.tl.trace(resp.getLogStr());
			if (resp != null) {
				ObjectNode node = objectMapper.readValue(resp.respStr, ObjectNode.class);
				boolean acknowledged = node.get("acknowledged").asBoolean();
				boolean shardsAcked = node.get("shards_acknowledged").asBoolean();
				return acknowledged && shardsAcked;
			}
		} catch (Exception e) {
			TPILogger.tl.error(String.format("Error opening indices: %s\n%s", indexName, e.getMessage()));
		}
		return false;
	}

	public boolean closeIndex(String indexName) {
		try {
			String url = String.format("%s%s/_close", getEsConnUrl()[index], indexName);
			HttpRespData resp = HttpUtil.httpReqByRawData(url, HttpMethod.POST.toString(), null, getHeader(), false);
			TPILogger.tl.trace(resp.getLogStr());

			if (resp != null) {
				ObjectNode node;

				node = objectMapper.readValue(resp.respStr, ObjectNode.class);

				boolean acknowledged = node.get("acknowledged").asBoolean();
				return acknowledged;
			}
		} catch (Exception e) {
			TPILogger.tl.error(String.format("Error closing indices: %s\n%s", indexName, e.getMessage()));
		}
		return false;
	}

	public boolean createIndex(String source, String indexName) {
		String url = String.format("%s%s", getEsConnUrl()[index], indexName);
		try {
			HttpRespData resp = HttpUtil.httpReqByRawData(url, "PUT", source, getHeader(), false);
			TPILogger.tl.trace(resp.getLogStr());

			ObjectNode node;

			node = objectMapper.readValue(resp.respStr, ObjectNode.class);

			boolean acknowledged = node.get("acknowledged").asBoolean();
			return acknowledged;
		} catch (IOException e) {
			TPILogger.tl.error("Create index error: " + e.getMessage());
			throw TsmpDpAaRtnCode._1288.throwing();
		}
	}

	public boolean deleteIndex(String indices) {
		try {
			String url = String.format("%s%s", getEsConnUrl()[index], indices);
			HttpRespData resp = HttpUtil.httpReqByRawData(url, "DELETE", null, getHeader(), false, false);
			TPILogger.tl.trace(resp.getLogStr());
			ObjectNode node = objectMapper.readValue(resp.respStr, ObjectNode.class);
			boolean acknowledged = node.get("acknowledged").asBoolean();
			return acknowledged;

		} catch (Exception e) {
			TPILogger.tl.error("Delete index error: " + e.getMessage());
			throw TsmpDpAaRtnCode._1287.throwing();
		}
	}

	public String cat_indices(String indexName, String queryString) {
		String api = "";
		String url[] = getEsConnUrl();
		for (int i = 0; i < url.length; i++) {
			api = url[i] + "_cat/indices/" + indexName + "?" + queryString;
			TPILogger.tl.trace("Call ES REST api:" + api);

			try {
				HttpRespData resp = HttpUtil.httpReqByGet(api, getHeader(), false);
				TPILogger.tl.trace(resp.getLogStr());
				if (HttpStatus.OK.value() == resp.statusCode) {
					return resp.respStr;
				}
			} catch (Exception e) {
				TPILogger.tl.info(StackTraceUtil.logStackTrace(e));
				continue;
			}
		}
		throw TsmpDpAaRtnCode._1298.throwing();
	}

	public List<ResponseHit> search(Object object, String... indices) throws Exception {
		String json = objectMapper.writeValueAsString(object);
		return search(json, indices);

	}

	public List<ResponseHit> search(String json, String... indices) throws Exception {
		isConnected();
		TPILogger.tl.trace(
				String.format("\n═════════ [BEGIN] ES Search ═════════\n%s\n%s\n------------------------------------", //
						String.join(", ", indices), json));

		String url = String.format("%s%s/_search", getEsConnUrl()[index], String.join(",", indices));
		HttpRespData resp = HttpUtil.httpReqByRawData(url, HttpMethod.POST.toString(), json, getHeader(), false);
		TPILogger.tl.trace(resp.getLogStr());
		ESSearchHit searchResponse = objectMapper.readValue(resp.respStr, new TypeReference<ESSearchHit>() {
		});
		List<ResponseHit> searchHits = new ArrayList<>();
		int totalShards = 0;
		int successfulShards = 0;
		String hitsTotalValue = "";
		double took = 0;
		if (searchResponse.getError() != null) {
			totalShards = 0;
			successfulShards = 0;
			hitsTotalValue = "";
			took = 0;
		} else {
			totalShards = searchResponse.get_shards().getTotal();
			successfulShards = searchResponse.get_shards().getSuccessful();
			hitsTotalValue = getHitsTotalValue(searchResponse);
			took = searchResponse.getTook();
		}
		if (searchResponse.getStatus() < 400) {
			searchHits = searchResponse.getHits().getHits();
		}
		TPILogger.tl
				.trace(String.format("\n------------------------------------\n%s\n═════════ [END] ES Search ══════════", //
						String.format("Search %d shards of total %d successfully. %s hits. Took %.3f secs.",
								successfulShards, totalShards, //
								hitsTotalValue, took)));

		return searchHits;
	}

	private String getHitsTotalValue(ESSearchHit searchResponse) {
		Hits hits = searchResponse.getHits();
		Total totalHits = hits.getTotal();
		if (totalHits != null) {
			String relation = totalHits.getRelation();
			long numHits = totalHits.getValue();
			return numHits + ("eq".equals(relation) ? "" : "+");
		}
		return "0";
	}

	@SuppressWarnings("unchecked")
	public int countAlertData(Date startTime, Date endTime, String alertType, String esSearchPayload, int size,
			String timeField, List<String> indices) throws Exception {
		Map<String, Object> timeRangeMap = getRangeMap(timeField, startTime, endTime, true, false);
//		Map<String, Object> RangeMap = new HashMap<>();
//		RangeMap.put("range", timeRangeMap);
		Map<String, String> esSearchPayloadmap = objectMapper.readValue(esSearchPayload, HashMap.class);
		Map<String, List<Object>> mustMap = new HashMap<>();
		Map<String, Object> Map = new HashMap<>();
		// search payload
		try {
			Map<String, Object> overElapse = new HashMap<>();
			Map<String, Object> mQuery = new HashMap<>();
			List<Object> precisionList = new ArrayList<>();
			precisionList.add(timeRangeMap);
			for (String key : esSearchPayloadmap.keySet()) {
				Object value = esSearchPayloadmap.get(key);

				if ("responseTime".equals(alertType)) {
					if ("elapse".equals(key)) {
						overElapse = getRangeMap(key, value, null, false);
						precisionList.add(overElapse);
					} else {
						if (!"ALL".equals(value)) {
							mQuery = getSearchMap(ESQuerytype.match, key, value);
							precisionList.add(mQuery);
						}
					}
				} else if ("keyword".equals(alertType)) {
					mQuery = getSearchMap(ESQuerytype.match, key, value);
					precisionList.add(mQuery);
				}

			}
			mustMap = getPrecisionMap(PrecisionEnum.must, precisionList);
		} catch (Exception e) {
			TPILogger.tl.error("Set ES search payload error." + StackTraceUtil.logStackTrace(e));
		}

		Map = getfinalMap(0, size, getBoolMap(mustMap));
		String json = objectMapper.writeValueAsString(Map);

		List<?> result = search(json, indices.toArray(new String[indices.size()]));
		return result.size();
	}

	/***
	 * 由於我不知道怎麼取名他 就給他個最後吧 (最外層的那個map)
	 * 
	 * @param from
	 * @param size
	 * @param obj
	 * @return
	 */
	public Map<String, Object> getfinalMap(int from, int size, Object obj) {
		Map<String, Object> Map = new HashMap<>();
		Map.put("from", from);
		Map.put("size", size);
		Map.put("query", obj);
		return Map;
	}

	public Map<String, Object> getfinalMap(int from, int size, Object obj, List<Map<String, Object>> sortList) {
		Map<String, Object> Map = new HashMap<>();
		Map.put("from", from);
		Map.put("size", size);
		Map.put("query", obj);
		Map.put("sort", sortList);
		return Map;
	}

	public Map<String, Object> getfinalMap(int from, int size, Object obj, List<Map<String, Object>> sortList,
			List<Object> searchAfter,List<String> source) {
		Map<String, Object> Map = new HashMap<>();
		Map.put("from", from);
		Map.put("size", size);
		Map.put("query", obj);
		if (!CollectionUtils.isEmpty(sortList))
			Map.put("sort", sortList);
		if (!CollectionUtils.isEmpty(searchAfter))
			Map.put("search_after", searchAfter);
		if (!CollectionUtils.isEmpty(source)) {
			Map.put("_source", source);
		}
		return Map;
	}

	/**
	 * must,should,must_not,filter
	 * 
	 * @param PrecisionEnum
	 * @param obj
	 * @return
	 */
	public Map<String, List<Object>> getPrecisionMap(PrecisionEnum PrecisionEnum, List<Object> obj) {
		Map<String, List<Object>> Map = new HashMap<>();
		Map.put(PrecisionEnum.name(), obj);
		return Map;
	}

	/***
	 * 他就是個 bool.......
	 * 
	 * @param obj
	 * @return
	 */
	public Map<String, Object> getBoolMap(Object obj) {
		Map<String, Object> BooltMap = new HashMap<>();
		BooltMap.put("bool", obj);
		return BooltMap;
	}

	/**
	 * 一般搜尋條件使用 EX id : XXX 欲使用 Wildcard 或 Range 請往下看 getWildcardMap getRangeMap
	 * 
	 * @param ESQuerytype
	 * @param field
	 * @param value
	 * @return
	 */
	public Map<String, Object> getSearchMap(ESQuerytype ESQuerytype, String field, Object value) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> SearchhMap = new HashMap<>();
		SearchhMap.put(field, value);
		map.put(ESQuerytype.name(), SearchhMap);
		return map;
	}

	public Map<String, Object> getSearchMap(ESQuerytype ESQuerytype) {
		Map<String, Object> map = new HashMap<>();
		map.put(ESQuerytype.name(), new HashMap<>());
		return map;
	}

	/***
	 * 我是個 wildcard 少用他啦，效能很差的
	 * 
	 * @param field
	 * @param value
	 * @param case_insensitive
	 * @return
	 */
	public Map<String, Object> getWildcardMap(String field, Object value, boolean case_insensitive) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> SearchhMap = new HashMap<>();
		Map<String, Object> WildcardhMap = new HashMap<>();
		WildcardhMap.put(ESQuerytype.wildcard.name(), value);
		WildcardhMap.put("case_insensitive", case_insensitive);
		SearchhMap.put(field, WildcardhMap);

		map.put(ESQuerytype.wildcard.name(), SearchhMap);
		return map;
	}

	/**
	 * 未知型態的range
	 * 
	 * @param field
	 * @param from
	 * @param to
	 * @param include_lower
	 * @return
	 */
	public Map<String, Object> getRangeMap(String field, Object from, Object to, boolean include_lower) {
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> fieldMap = new HashMap<>();

		fieldMap.put("from", from);

		if (to != null) {
			fieldMap.put("to", to);
		}
		if (!StringUtils.isEmpty(include_lower))
			fieldMap.put("include_lower", include_lower);
		map.put("range", fieldMap);
		return map;

	}
	/**
	 * 未知型態的range
	 * 
	 * @param field
	 * @param from
	 * @param to
	 * @param include_lower
	 * @return
	 */
	public Map<String, Object> getRangeMap(String field, Object from, Object to, boolean include_lower,boolean include_upper) {
		Map<String, Object> rangeMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> fieldMap = new HashMap<>();
		if (include_lower)
			fieldMap.put("gte",from);
		if (!include_lower)
			fieldMap.put("gt", from);

		if (to != null) {
			if (include_upper)
				fieldMap.put("lte", to);
			if (!include_upper)
				fieldMap.put("lt", to);
		}

		map.put(field, fieldMap);
		rangeMap.put("range",map);
		return rangeMap;
	}
	/**
	 * For時間的Range
	 * 
	 * @param field
	 * @param from
	 * @param to
	 * @param include_lower
	 * @param include_upper
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getRangeMap(String field, Date from, Date to, boolean include_lower,
			boolean include_upper) throws Exception {
		ZonedDateTime zTimeS = ZonedDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault());
		DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
		Map<String, Object> rangeMap = new HashMap<>();
		Map<String, Object> map = new HashMap<>();
		Map<String, Object> fieldMap = new HashMap<>();
		if (include_lower)
			fieldMap.put("gte", zTimeS.format(formatter));
		if (!include_lower)
			fieldMap.put("gt", zTimeS.format(formatter));

		if (to != null) {
			ZonedDateTime zTimeE = ZonedDateTime.ofInstant(to.toInstant(), ZoneId.systemDefault());
			if (include_upper)
				fieldMap.put("lte", zTimeE.format(formatter));
			if (!include_upper)
				fieldMap.put("lt", zTimeE.format(formatter));
		}

		map.put(field, fieldMap);
		rangeMap.put("range",map);
		
		return rangeMap;
	}

	public List<Map<String, Object>> getSortList(List<Map<String, Object>> list, String field, String order) {
		if(list == null) {
			list = new ArrayList<>();
		}
		Map<String, String> orderMap = new HashMap<>();
		orderMap.put("order", order);
		list.add(getSortMap(field, orderMap));
		return list;

	}

	/**
	 * 下面的SortMap 的外層 ，但由於暫不確定是否復合查詢會不會直接使用，故 仍為public
	 * 
	 * @param field
	 * @param map
	 * @return
	 */
	public Map<String, Object> getSortMap(String field, Map<String, String> map) {
		Map<String, Object> sortMap = new HashMap<>();
		sortMap.put(field, map);
		return sortMap;
	}

	/**
	 * 
	 * @param field
	 * @param order
	 * @param unmapped_type
	 * @return
	 */
	public Map<String, Object> getSortMap(String field, String order, String unmapped_type) {
		Map<String, String> sortMap = new HashMap<>();
		sortMap.put("order", order);
		if (StringUtils.hasLength(unmapped_type))
			sortMap.put("unmapped_type", unmapped_type);

		return getSortMap(field, sortMap);

	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
