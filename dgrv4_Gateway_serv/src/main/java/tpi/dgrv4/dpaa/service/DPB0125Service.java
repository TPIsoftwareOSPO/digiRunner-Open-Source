package tpi.dgrv4.dpaa.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.es.DgrESService;
import tpi.dgrv4.dpaa.vo.DPB0125Req;
import tpi.dgrv4.dpaa.vo.DPB0125Resp;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0125Service {

	private TPILogger logger = TPILogger.tl;
	
	private DgrESService dgrESService;
	private ObjectMapper objectMapper;
	private ServiceConfig serviceConfig;
	private TsmpSettingService tsmpSettingService;

	private final String DT_FORMAT = "yyyyMMdd'T'HHmmssZ";
	
	@Autowired
	public DPB0125Service(DgrESService dgrESService, ObjectMapper objectMapper, ServiceConfig serviceConfig,
			TsmpSettingService tsmpSettingService) {
		super();
		this.dgrESService = dgrESService;
		this.objectMapper = objectMapper;
		this.serviceConfig = serviceConfig;
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB0125Resp getIndex(TsmpAuthorization auth, DPB0125Req req) {
		Map<String, Date> dates = checkDates(req);
		
		List<Object> listObject = new ArrayList<>();
	
		try {
			String indexName = req.getIdxName();
			String queryString = "h=health,status,index,pri,rep,docs.count,store.sizes,creation.date.string";
			if (getDgrESService().isConnected()) {
			String json = getDgrESService().cat_indices(indexName, queryString);
			List<Map<String, Object>> dataList = getObjectMapper().readValue(json, //
				new TypeReference<List<Map<String, Object>>>() {
			});
			
			String cTime = null;
			for (Map<String, Object> data : dataList) {
				cTime = String.valueOf(data.get("creation.date.string"));
				if (isInDateRange(cTime, dates)) {
					listObject.add(data);
				}
			}
			
			if(CollectionUtils.isEmpty(listObject)) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error("DPB0125 Error!: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		DPB0125Resp resp = new DPB0125Resp();
		resp.setListObject(listObject);
		return resp;
	}

	public Map<String, Date> checkDates(DPB0125Req req) {
		String timeS = req.getTimeS();
		Date dt_timeS = checkDatePattern(timeS, "timeS");
		String timeE = req.getTimeE();
		Date dt_timeE = checkDatePattern(timeE, "timeE");
		// 檢查查詢日期範圍是否超過上限
		this.checkDate(dt_timeS, dt_timeE);
		Map<String, Date> map = new HashMap<>();
		map.put("timeS", dt_timeS);
		map.put("timeE", dt_timeE);
		return map;
	}

	public Date checkDatePattern(String dtStr, String fieldName) {
		try {
			ZonedDateTime zdt =  ZonedDateTime.parse(dtStr, DateTimeFormatter.ofPattern(this.DT_FORMAT));
			return Date.from(zdt.toInstant());
		} catch (Exception e) {
			this.logger.debug(String.format("Cannot parse %s with %s format.", dtStr, this.DT_FORMAT));
			throw TsmpDpAaRtnCode._1352.throwing("{{" + fieldName + "}}");
		}
	}

	public boolean isInDateRange(String input, Map<String, Date> dateRange) throws ParseException {
		Date inputDt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(input);
		Date dateS = dateRange.get("timeS");
		Date dateE = dateRange.get("timeE");
		return inputDt.after(dateS) && inputDt.before(dateE);
	}
	
	/**
	 * 檢查ES查詢日期區間是否超過上限
	 * @param timeS
	 * @param timeE
	 */
	private void checkDate(Date timeS, Date timeE) {
		int DURATION = getEsQueryDuration();

		Date exeTimeS = timeS;
		Date exeTimeE = timeE;
		if (exeTimeS != null && exeTimeE == null) {
			this.logger.debug("結束日期為Null");
			throw TsmpDpAaRtnCode._1366.throwing();
		}
		if (exeTimeS == null && exeTimeE != null) {
			this.logger.debug("開始日期為Null");
			throw TsmpDpAaRtnCode._1366.throwing();
		}
		if (exeTimeS != null && exeTimeE != null) {
			long diff = exeTimeE.getTime() - exeTimeS.getTime();
			long days = diff / (1000 * 60 * 60 * 24);
			long secs = diff / 1000;
			// exeTimeS大於exeTimeE時 ErrorCode 9904
			if (secs <= 0) {
				this.logger.error("起日大於迄日");
				throw TsmpDpAaRtnCode._1368.throwing();
			}
			// 檢查日期區間是否超過30天 ErrorCode 0150
			if (days > DURATION) {
				this.logger.error("檢查日期區間是否超過設定值");
				throw TsmpDpAaRtnCode._1416.throwing(String.valueOf(DURATION));
			}
		} else {
			// throw TsmpaaError.ParameterError.throwing();
			// 2018/12/21 更改為全文檢索 , 當 exeTimeS && exeTimeE == null 時, 進行全文檢索
		}
	}
	
	/**
	 * 取得ES查詢日期區間上限
	 * @return
	 */
	public int getEsQueryDuration() {
		int duration = 30;
		try {
			String val = getTsmpSettingService().getVal_QUERY_DURATION();
			duration = Integer.valueOf(val);
		} catch (Exception e) {
			this.logger.warn(String.format("Cannot cast 'es.query.duration' value, set to default %d.", duration));
		}
		return duration;
	}

	protected DgrESService  getDgrESService() {
		return dgrESService;
	}


	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

}