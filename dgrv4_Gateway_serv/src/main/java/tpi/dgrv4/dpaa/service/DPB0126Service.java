package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.es.DgrESService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0126Req;
import tpi.dgrv4.dpaa.vo.DPB0126Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0126Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private DgrESService dgrESService;
	private ObjectMapper objectMapper;
	
	@Autowired
	public DPB0126Service(TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, DgrESService dgrESService,
			ObjectMapper objectMapper) {
		super();
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.dgrESService = dgrESService;
		this.objectMapper = objectMapper;
	}

	public DPB0126Resp queryAllIndex(TsmpAuthorization auth, DPB0126Req req, ReqHeader reqHeader) {
		DPB0126Resp resp = new DPB0126Resp();
		try {
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			StringBuffer strBuf = new StringBuffer();
			// http://10.20.30.216:9200/_cat/indices/*pb*,*eb*?v&s=index:asc
			// http://10.20.30.216:9200/_cat/indices/*pb*?v&s=index:asc
			// http://10.20.30.216:9200/_cat/indices/*?v&s=index:asc
			if (words != null) {
				for (String string : words) {
					String str = String.format("*%s*,", string);
					strBuf.append(str);
				}
			}

			if (strBuf.length() > 0) {
				if (strBuf.toString().endsWith(",")) {
					strBuf.deleteCharAt(strBuf.length() - 1);
				}
			} else {
				strBuf.append("*");
			}
			if (getDgrESService().isConnected()) {
			String json = getDgrESService().cat_indices(strBuf.toString(), "v&s=index:asc");
			List<LinkedHashMap<String, Object>> dataList = getObjectMapper().readValue(json,
					new TypeReference<List<LinkedHashMap<String, Object>>>() {
					});

			HashSet<String> indexList = new LinkedHashSet<String>();
			for (LinkedHashMap<String, Object> data : dataList) {
				String index = getIndexName(String.valueOf(data.get("index")));
				indexList.add(index);
			}

			String patternStr = getParam1ByItemsNoAndItemsSbNo("ES_INDEX_EXCLUDER", "DPB0126", reqHeader.getLocale());
			if (StringUtils.hasLength(patternStr)) {
				patternStr = new String(ServiceUtil.base64Decode(patternStr), "utf-8");
				Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
				List<String> list = new ArrayList<>();
				indexList.forEach(index -> {
					Matcher matcher = pattern.matcher(index);
					if (!matcher.matches()) {
						list.add(index);
					}

				});
				resp.setIndexList(checkIndexReult(list, req));
			} else {
				List<String> list = new ArrayList<String>(indexList);
				resp.setIndexList(checkIndexReult(list, req));

			}
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error("DPB0126 Error: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected String getIndexName(String indexStr) {
		if (indexStr.contains("_")) {
			int indexof = indexStr.lastIndexOf("_");
			String spIndex = indexStr.substring(0, indexof + 1) + "*";
			return spIndex;
		} else if (indexStr.contains("-")) {
			int indexof = indexStr.lastIndexOf("-");
			String spIndex = indexStr.substring(0, indexof + 1) + "*";
			return spIndex;
		} else {
			String spIndex = indexStr + "*";
			return spIndex;
		}

	}

	private String getParam1ByItemsNoAndItemsSbNo(String itemNo, String subitemNo, String locale) {
		String subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(itemNo, subitemNo, locale);
		
		if (dpItem == null) {
			return null;
		}
		subitemName = dpItem.getParam1();

		return subitemName;
	}

	private List<String> checkIndexReult(List<String> indexList, DPB0126Req req) {
		if (CollectionUtils.isEmpty(indexList)) {
			throw TsmpDpAaRtnCode._1298.throwing(); // 查無資料
		}

		// 排除原先以選擇的清單
		List<String> list = req.getRoleIndexList();
		if (!CollectionUtils.isEmpty(list)) {
			indexList.removeAll(list);
		}
		return indexList;
	}


	protected DgrESService  getDgrESService() {
		return dgrESService;
	}
	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

}