package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.es.DgrESService;
import tpi.dgrv4.dpaa.es.ESQuerytype;
import tpi.dgrv4.dpaa.es.PrecisionEnum;
import tpi.dgrv4.dpaa.es.ResponseHit;
import tpi.dgrv4.dpaa.vo.DashboardEsData;
import tpi.dgrv4.dpaa.vo.DashobardNotMatchItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardEsLog;
import tpi.dgrv4.entity.repository.DgrDashboardEsLogDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class HandleESApiDataService {
	private ObjectMapper objectMapper;
	private DgrDashboardEsLogDao dgrDashboardEsLogDao;
	private DgrESService dgrESService;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpApptJobDao tsmpDpApptJobDao;

	List<Object> searchAfter = new ArrayList<>();

	public final static String indexPrefix = "tsmp_api_log_"; // index前綴
	private final static String createTimestemp = "createTimestamp";
	private final static String notMatchListStr = "notMatchList";

	@Autowired
	public HandleESApiDataService(ObjectMapper objectMapper, DgrDashboardEsLogDao dgrDashboardEsLogDao,
			DgrESService dgrESService, TsmpDpFileDao tsmpDpFileDao, TsmpApiDao tsmpApiDao,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super();
		this.objectMapper = objectMapper;
		this.dgrDashboardEsLogDao = dgrDashboardEsLogDao;
		this.dgrESService = dgrESService;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}

	// 為了UT可以驗證起訖時間
	public Date getStartDate(Date execDate) {
		// 如果無資料(第一次)，則只撈現在時間往前10分鐘的值
		// 1. 先判斷資料庫內是否有值
		// 此 order by 超級耗時, 使用 Dbeaver 連線, 70w 筆即會耗時
		DgrDashboardEsLog lastData = getDgrDashboardEsLogDao().findTopByOrderByRtimeDesc().orElse(null);
		Date startDate;
		Calendar c = Calendar.getInstance();
		if (lastData != null) {
			startDate = lastData.getRtime();
			c.setTime(startDate);
			searchAfter.add(startDate.getTime());
			searchAfter.add(lastData.getId());
			searchAfter.add("4");
			TPILogger.tl.trace("searchAfter = " + searchAfter);
		} else { // 如果有則以ts作為起始時間，現在時間為結束時間，撈取資料
			c.setTime(execDate);
			c.add(Calendar.MINUTE, -10);
			startDate = c.getTime();
		}

		return startDate;
	}
	
	private void sleepforColddownCPU(long ms) {
//		try {
//			Thread.sleep(ms);
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		}
	}

	private String[] getIndicesSet(Date startDate, Date execDate) {
		Calendar c = Calendar.getInstance();
		Set<String> set = new HashSet<>();
		// 因為有可能無index或 index太多，所以要改成使用通配符，但又不能直接範圍太大，所以 每10天一個
		while (!startDate.after(execDate)) {
			c.setTime(startDate);

			DateTimeUtil.dateTimeToString(startDate, DateTimeFormatEnum.西元年月日_4).ifPresent(index -> {
				String subIndex = index.substring(0, 7);
				set.add(indexPrefix + subIndex + "*");
			});

			c.add(Calendar.DATE, 1);
			startDate = c.getTime();
			sleepforColddownCPU(10); //冷卻 CPU
		}
		return set.toArray(new String[0]);
	}

	@Transactional
	public void exec(Date execDate, Long jobId) throws Exception {
		searchAfter = new ArrayList<>();
		// 取得起訖時間
		Date startDate = getStartDate(execDate);

		boolean isConnected = checkConnection();
		if (!isConnected) {
			return;
		}
		Calendar c = Calendar.getInstance();

		// for 起訖間
		Map<String, Object> range = getDgrESService().getRangeMap(createTimestemp, startDate.getTime(),
				execDate.getTime(), true, true);

		String[] indicesArray = getIndicesSet(startDate, execDate);
		// 先到 TSMP_DP_FILE 取得未匹配ID
		List<TsmpDpFile> dpfileList = getTsmpDpFileDao().findByFileNameAndRefIdAndRefFileCateCode(notMatchListStr, 1l,
				TsmpDpFileType.DASHBOARD_TEMP_DATA.code());
		TsmpDpFile dpFile = null;
		byte[] getBlob = null;

		if (dpfileList.size() == 1) {
			dpFile = dpfileList.get(0);
			getBlob = dpFile.getBlobData();
		}

		List<DashobardNotMatchItem> notMatchList = new ArrayList<>();
		List<String> notMatchIdList = new ArrayList<>();
		List<ResponseHit> hitList = new ArrayList<>();
		List<Long> notMatchTimeList = new ArrayList<>();
		List<String> source = chooseSource();
		c.setTime(execDate);
		c.add(Calendar.HOUR, -1);
		Date onehourBefore = c.getTime();

		Map<String, Object> queryPart = getDgrESService().getSearchMap(ESQuerytype.terms, "type.keyword",
				Arrays.asList(1, 4));
		// 如果有不匹配list 有值才進行這個搜尋
		if (getBlob != null) {

			try {
				notMatchList = getObjectMapper().readValue(getBlob, new TypeReference<List<DashobardNotMatchItem>>() {
				});
			} catch (Exception e) {
				TPILogger.tl.error(
						String.format("Cannot convert json to notMatchList: %s", StackTraceUtil.logStackTrace(e)));
			}

			if (notMatchList.size() > 0) {

				notMatchList.forEach(n -> {
					notMatchIdList.add(n.getId());
					notMatchTimeList.add(n.getCreateTimestamp());
				});
				Collections.sort(notMatchTimeList);

				Map<String, Object> queryIDPart = getDgrESService().getSearchMap(ESQuerytype.terms, "id.keyword",
						notMatchIdList);
				TPILogger.tl.debug("notMatchIdList : " + String.join(", ", notMatchIdList));
				// 以未匹配的資料中最早的一筆當作起時
				Map<String, Object> range4OneHr = getDgrESService().getRangeMap(createTimestemp,
						notMatchTimeList.get(0), execDate.getTime(), true, true);
				List<Object> filter = new ArrayList<>();
				filter.add(queryIDPart);
				filter.add(queryPart);
				filter.add(range4OneHr);
				Map<String, List<Object>> filterMap = getDgrESService().getPrecisionMap(PrecisionEnum.filter, filter);
				Map<String, Object> bool = getDgrESService().getBoolMap(filterMap);

				Map<String, Object> finalMap = getDgrESService().getfinalMap(0, 10000, bool, null, null, source);
				String json = getObjectMapper().writeValueAsString(finalMap);
				TPILogger.tl.debug("json: " + json);
				String[] noMatchindicesArray = getIndicesSet(new Date(notMatchTimeList.get(0)), execDate);

				hitList.addAll(search(finalMap, noMatchindicesArray));
			}

		}
		// 只撈1、4道，若沒有第四道，則寫入notMatchList，notMatchList最後存進
		// TSMP_DP_FILE，其餘寫入DgrDashboardEsLog
		List<Object> filter2 = new ArrayList<>();
		filter2.add(queryPart);
		filter2.add(range);
		Map<String, List<Object>> filterMap2 = getDgrESService().getPrecisionMap(PrecisionEnum.filter, filter2);
		Map<String, Object> bool2 = getDgrESService().getBoolMap(filterMap2);
		List<Map<String, Object>> sortList2 = getDgrESService().getSortList(null, createTimestemp, "asc");
		getDgrESService().getSortList(sortList2, "id.keyword", "asc");
		getDgrESService().getSortList(sortList2, "type.keyword", "asc");
		
		TPILogger.tl.info("...start ES search() ");
		while (true) {

			Map<String, Object> finalMap2 = getDgrESService().getfinalMap(0, 10000, bool2, sortList2, searchAfter,
					source);

			// ES 這裡似乎也會 search 很久
			List<ResponseHit> hitList2 = search(finalMap2, indicesArray);
			if (CollectionUtils.isEmpty(hitList2)) {
				break;
			}
			searchAfter = hitList2.get(hitList2.size() - 1).getSort();
			TPILogger.tl.trace("searchAfter = " + searchAfter);
			hitList.addAll(hitList2);
//			sleepforColddownCPU(10); //冷卻 CPU
		}

		TPILogger.tl.info("...start classifyData() ");
		Map<String, Map<String, DashboardEsData>> map = classifyData(hitList);
		// 判斷是否有沒有匹配
		List<DashobardNotMatchItem> notMatchListFinal = new ArrayList<>();
		
		TPILogger.tl.info("...start classifyData map.forEach() ");
		map.forEach((k, vMap) -> {
			if (vMap.size() < 2 && vMap.get("req") != null) { // 未匹配且未超過一小時，則繼續保存在TSMP_DP_FILE等待下次判斷
				Date ts = new Date(vMap.get("req").getCreateTimestamp());
				if (ts.after(onehourBefore)) {

					notMatchListFinal.add(new DashobardNotMatchItem(k, vMap.get("req").getCreateTimestamp()));
				}
			}
//			sleepforColddownCPU(10); //冷卻 CPU
		});

		// 寫入DgrDashboardEsLog
		TPILogger.tl.info("...start inserData() ");
		List<DgrDashboardEsLog> inserList = inserData(map);

		// 新增/更新 notMatchList
		String json = getObjectMapper().writeValueAsString(notMatchListFinal);

		saveDpFile(dpFile, notMatchListStr, 1l, json);

		// tsmpApi 計算與寫入
		TPILogger.tl.info("...start calculateAPIUsage(inserList) ");
		calculateAPIUsage(inserList);

		checkJobStatus(jobId);
	}

	protected void checkJobStatus(Long id) {
		// 因為在換版可能REPORT_BATCH還在執行中,啟動後會被被AutoInitSQL改成取消,若這種情況就rollback
		boolean isExists = getTsmpDpApptJobDao().existsByApptJobIdAndStatus(id, "C");
		if (isExists) {
			TPILogger.tl.info("REPORT_BATCH job status changed cancel id:" + id);
			throw new CancellationException();
		}
	}

	private void saveDpFile(TsmpDpFile dpFile, String fileName, long refid, String json) {
		if (dpFile == null) {
			dpFile = new TsmpDpFile();
		} else {
			dpFile.setFileId(dpFile.getFileId());
			dpFile.setUpdateDateTime(DateTimeUtil.now());
		}

		dpFile.setRefId(refid);
		dpFile.setFileName(notMatchListStr);
		dpFile.setIsTmpfile("N");
		dpFile.setFilePath("");
		dpFile.setRefFileCateCode(TsmpDpFileType.DASHBOARD_TEMP_DATA.code());
		dpFile.setIsBlob("Y");
		dpFile.setCreateUser("SYSTEM");

		dpFile.setBlobData(json.getBytes());
		getTsmpDpFileDao().save(dpFile);
	}

	protected boolean checkConnection() {
		boolean isConnection = getDgrESService().isConnected();
		TPILogger.tl.info("ES Service is Connected::" + isConnection);
		return isConnection;
	}

	private void calculateAPIUsage(List<DgrDashboardEsLog> list) {
		// modulName -> txid(apikey) -> exeStatus

		Map<String, Map<String, Map<String, LongSummaryStatistics>>> getCollecte = list.stream()
				.collect(Collectors.groupingBy(DgrDashboardEsLog::getModuleName,
						Collectors.groupingBy(DgrDashboardEsLog::getTxid,
								Collectors.groupingBy(DgrDashboardEsLog::getExeStatus,
										Collectors.summarizingLong(DgrDashboardEsLog::getElapse)))));
		List<TsmpApi> apis = new ArrayList<>();
		for (Entry<String, Map<String, Map<String, LongSummaryStatistics>>> moduleEntry : getCollecte.entrySet()) {

			for (Entry<String, Map<String, LongSummaryStatistics>> txidEntry : moduleEntry.getValue().entrySet()) {
				TsmpApi api = getTsmpApiDao().findByModuleNameAndApiKey(moduleEntry.getKey(), txidEntry.getKey());
				if (api != null) {
					LongSummaryStatistics y = txidEntry.getValue().get("Y");
					long success = 0;
					long elapse = 0;
					if (y != null) {
						success = y.getCount();
						elapse = y.getSum();
					}

					LongSummaryStatistics n = txidEntry.getValue().get("N");
					long fail = n != null ? n.getCount() : 0;

					api.setSuccess(api.getSuccess() + success);
					api.setFail(api.getFail() + fail);
					api.setTotal(api.getTotal() + success + fail);

					api.setElapse(api.getElapse() + elapse);
					apis.add(api);
				}

			}
		}
		getTsmpApiDao().saveAll(apis);
	}

	protected List<ResponseHit> search(Map<String, Object> finalMap, String... indicesArray) throws Exception {
		return getDgrESService().search(finalMap, indicesArray);
	}

	/***
	 * 只 返回需要的欄位
	 * 
	 * @return
	 */
	private List<String> chooseSource() {
		List<String> source = new ArrayList<>();
		source.add("id");
		source.add("elapse");
		source.add("cid");
		source.add("moduleName");
		source.add("type");
		source.add(createTimestemp);
		source.add("httpStatus");
		source.add("txid");
		source.add("orgId");
		return source;
	}

	/***
	 * 把資料寫進DgrDashboardEsLog
	 * 
	 * @param map
	 * @return
	 */
	private List<DgrDashboardEsLog> inserData(Map<String, Map<String, DashboardEsData>> map) {
		List<DgrDashboardEsLog> list = new ArrayList<>();
		for (Entry<String, Map<String, DashboardEsData>> entry : map.entrySet()) {

			DashboardEsData req = entry.getValue().get("req");
			DashboardEsData res = entry.getValue().get("res");
			if (req != null && res != null) {
				DgrDashboardEsLog vo = new DgrDashboardEsLog();
				vo.setCid(req.getCid());
				vo.setElapse(res.getElapse());
				String exeStatus = "Y";
				Integer status = res.getHttpStatus();
				if (status <= 0 || status >= 400)
					exeStatus = "N";
				vo.setExeStatus(exeStatus);
				vo.setId(req.getId());
				vo.setModuleName(req.getModuleName());
				vo.setOrgid(req.getOrgId());
				Date ts = new Date(req.getCreateTimestamp());
				vo.setRtime(ts);
				vo.setTxid(req.getTxid());
				vo.setHttpStatus(status);
				vo.setRtimeYearMonth(DateTimeUtil.dateTimeToString(ts, DateTimeFormatEnum.西元年月_2).orElse(null));
				list.add(vo);
			}
			sleepforColddownCPU(10); //冷卻 CPU
		}
		if (list.size() > 0) {
			getDgrDashboardEsLogDao().saveAll(list);
		}
		return list;
	}

	/***
	 * 匹配資料
	 * 
	 * @param hitList
	 * @return
	 */
	private Map<String, Map<String, DashboardEsData>> classifyData(List<ResponseHit> hitList) {

		Map<String, Map<String, DashboardEsData>> map = new HashMap<>();

		for (ResponseHit hit : hitList) {
			Map<String, Object> source = hit.get_source();
			DashboardEsData data = getObjectMapper().convertValue(source, DashboardEsData.class);
			String id = (String) source.get("id");
			for (Entry<String, Object> entry : source.entrySet()) {

				map.putIfAbsent(id, new HashMap<>());

				Map<String, DashboardEsData> typeMap = map.get(id);

				if ("type".equals(entry.getKey())) {

					if (Integer.valueOf((String) entry.getValue()) == 1) {
						typeMap.put("req", data);
					}
					if (Integer.valueOf((String) entry.getValue()) == 4) {
						typeMap.put("res", data);
					}

				}

			}
			sleepforColddownCPU(10); //冷卻 CPU
		}

		return map;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected DgrDashboardEsLogDao getDgrDashboardEsLogDao() {
		return dgrDashboardEsLogDao;
	}

	protected DgrESService getDgrESService() {
		return dgrESService;

	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return tsmpDpApptJobDao;
	}

}
