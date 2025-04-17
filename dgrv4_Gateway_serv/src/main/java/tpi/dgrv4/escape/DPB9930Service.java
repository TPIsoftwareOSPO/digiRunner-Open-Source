package tpi.dgrv4.escape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9930Resp;
import tpi.dgrv4.entity.entity.DgrWebsite;
import tpi.dgrv4.entity.entity.DgrWebsiteDetail;
import tpi.dgrv4.entity.repository.DgrWebsiteDao;
import tpi.dgrv4.entity.repository.DgrWebsiteDetailDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.DigiRunnerGtwDeployProperties;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
//checkmarx, Unchecked Input for Loop Condition
@Service
public class DPB9930Service {
	private TPILogger logger = TPILogger.tl;

	private DgrWebsiteDao dgrWebsiteDao;
	private DgrWebsiteDetailDao dgrWebsiteDetailDao;
	private DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties;

	@Autowired
	public DPB9930Service(DgrWebsiteDao dgrWebsiteDao, DgrWebsiteDetailDao dgrWebsiteDetailDao,
			DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties) {
		super();
		this.dgrWebsiteDao = dgrWebsiteDao;
		this.dgrWebsiteDetailDao = dgrWebsiteDetailDao;
		this.digiRunnerGtwDeployProperties = digiRunnerGtwDeployProperties;
	}

	@Transactional
	public DPB9930Resp importWebsiteProxy(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {
		try {
			if (mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}

			checkParam(mFile.getOriginalFilename());

			importData(tsmpAuthorization, mFile.getInputStream());
			return new DPB9930Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

	}

	protected boolean isMemory() {
		return getDigiRunnerGtwDeployProperties().isMemoryRole();
	}

	public void importData(TsmpAuthorization tsmpAuthorization, InputStream inputStream) throws Exception {
		// 判斷是否為記憶體角色
		boolean isMemory = isMemory();

		// 取得excel資料
		List<DgrWebsite> excelDgrWebsiteList = new ArrayList<>();
		Map<String, List<DgrWebsiteDetail>> excelDgrWebsiteDetailMap = new HashMap<>();
		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			if (workbook.getNumberOfSheets() != 2) {
				throw TsmpDpAaRtnCode._1291.throwing();
			}
			Sheet sheet = workbook.getSheetAt(0);
			excelDgrWebsiteList = getExcelWSList(sheet);
			Sheet sheet2 = workbook.getSheetAt(1);

			excelDgrWebsiteDetailMap = getExcelWSDList(sheet2);
		}

//			// 如果是InMemory 就直接刪掉重建
//			getDgrWebsiteDao().deleteAll();
//			getDgrWebsiteDetailDao().deleteAll();
//			for (DgrWebsite dgrWebsite : excelDgrWebsiteList) {
//				DgrWebsite ws = getDgrWebsiteDao().save(dgrWebsite);
//				List<DgrWebsiteDetail> detailList = excelDgrWebsiteDetailMap.get(ws.getWebsiteName());
//
//				for (DgrWebsiteDetail dl : detailList) {
//					dl.setDgrWebsiteId(ws.getDgrWebsiteId());
//					getDgrWebsiteDetailDao().save(dl);
//				}
//			}

		// 找出原有的資料
		List<DgrWebsite> wsList = getDgrWebsiteDao().findAll();
		List<DgrWebsiteDetail> insertDetails = new ArrayList<>();
		Map<String, DgrWebsite> oldDgrWebsiteMap = new HashMap<>();
		wsList.forEach(w -> {
			oldDgrWebsiteMap.put(w.getWebsiteName(), w);
		});
		DgrWebsite ws = new DgrWebsite();
		// 新資料的detail Map
		for (DgrWebsite e_ws : excelDgrWebsiteList) {
			DgrWebsite oldws = oldDgrWebsiteMap.get(e_ws.getWebsiteName());
			if (oldws != null) {
				// 找到舊的DgrWebsiteId
				Long oldDgrWebsiteId = oldws.getDgrWebsiteId();
				// 刪除舊的對應的DgrWebsiteDetail
				getDgrWebsiteDetailDao().deleteByDgrWebsiteId(oldDgrWebsiteId);
			} else {
				oldws = new DgrWebsite();

			}
			// detail 直接都是新增
			ws = setData(oldws, e_ws, tsmpAuthorization);
			List<DgrWebsiteDetail> excelDWSDList = excelDgrWebsiteDetailMap.get(ws.getWebsiteName());
			List<DgrWebsiteDetail> details = insertDgrWebsiteDetail(ws.getDgrWebsiteId(), excelDWSDList);
			insertDetails.addAll(details);
		}

		if (isMemory) {
			List<String> wsNameList = excelDgrWebsiteList.stream().map(DgrWebsite::getWebsiteName)
					.collect(Collectors.toList());
			getDgrWebsiteDao().deleteByWebsiteNameNotIn(wsNameList);
			
			List<Long> wsIdList =	insertDetails.stream().map(DgrWebsiteDetail::getDgrWebsiteId).collect(Collectors.toList());
			getDgrWebsiteDetailDao().deleteByDgrWebsiteIdNotIn(wsIdList);

		}
	}

	// 因目前使用DgrSequenced 尚不支援 saveAll() 所以只能單筆單筆存， 待未來優化
	private List<DgrWebsiteDetail> insertDgrWebsiteDetail(Long dgrWebsiteId, List<DgrWebsiteDetail> list) {

		List<DgrWebsiteDetail> list1 = new ArrayList<>();
		for (DgrWebsiteDetail dgrWebsiteDetail : list) {
			DgrWebsiteDetail detail = new DgrWebsiteDetail();
			detail.setDgrWebsiteId(dgrWebsiteId);
			detail.setProbability(dgrWebsiteDetail.getProbability());
			detail.setUrl(dgrWebsiteDetail.getUrl());
			detail = getDgrWebsiteDetailDao().save(detail);
			list1.add(detail);
		}
		return list1;
	}

	private DgrWebsite setData(DgrWebsite oldEntity, DgrWebsite newEntity, TsmpAuthorization tsmpAuthorization) {
		oldEntity.setAuth(newEntity.getAuth());
		oldEntity.setIgnoreApi(newEntity.getIgnoreApi());
		oldEntity.setKeywordSearch(newEntity.getKeywordSearch());
		oldEntity.setRemark(newEntity.getRemark());
		oldEntity.setShowLog(newEntity.getShowLog());
		oldEntity.setSqlInjection(newEntity.getSqlInjection());
		oldEntity.setTps(newEntity.getTps());
		oldEntity.setTraffic(newEntity.getTraffic());
		oldEntity.setUpdateDateTime(DateTimeUtil.now());
		oldEntity.setUpdateUser(tsmpAuthorization.getUserName());
		oldEntity.setWebsiteName(newEntity.getWebsiteName());
		oldEntity.setWebsiteStatus(newEntity.getWebsiteStatus());
		oldEntity.setXss(newEntity.getXss());
		oldEntity.setXxe(newEntity.getXxe());
		oldEntity = getDgrWebsiteDao().save(oldEntity);
		return oldEntity;
	}

	private Map<String, List<DgrWebsiteDetail>> getExcelWSDList(Sheet sheet) {
		Map<String, List<DgrWebsiteDetail>> map = new HashMap<>();
		boolean isFirst = true;
		DataFormatter formatter = new DataFormatter();
		Iterator<Row> rows = sheet.iterator();
		//checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
	     int maxValue = Integer.MAX_VALUE;
	     int loopIndex = 0;
	     while (rows.hasNext() && loopIndex <= maxValue) {
	    	 if(loopIndex == maxValue) {
	    		 throw TsmpDpAaRtnCode._1559.throwing("Exceed " + maxValue + " row");
	    	 }
	    	 loopIndex++;
			if (isFirst) {
				isFirst = false;
				Row row = rows.next();
				if (!"WEBSITE_NAME".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"PROBABILITY".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"URL".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}

			} else {
				Row row = rows.next();

				// 空白列不處理

				if (!StringUtils.hasText(formatter.formatCellValue(row.getCell(0)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(1)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(2)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(3)))) {
					continue;
				}

				DgrWebsiteDetail wsd = new DgrWebsiteDetail();

				String webSitName = formatter.formatCellValue(row.getCell(0));
				String probability = formatter.formatCellValue(row.getCell(1));
				if (!StringUtils.hasLength(probability)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{probability}}");
				}
				wsd.setProbability(Integer.parseInt(probability));

				String url = formatter.formatCellValue(row.getCell(2));
				if (!StringUtils.hasLength(url)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{url}}");
				}

				wsd.setUrl(url);
				wsd.setKeywordSearch(formatter.formatCellValue(row.getCell(3)));
				if (map.containsKey(webSitName)) {
					List<DgrWebsiteDetail> list = map.get(webSitName);
					list.add(wsd);
					map.put(webSitName, list);
				} else {
					List<DgrWebsiteDetail> list = new ArrayList<>();
					list.add(wsd);
					map.put(webSitName, list);
				}

			}
		}
		return map;
	}

	private List<DgrWebsite> getExcelWSList(Sheet sheet) {
		List<DgrWebsite> excelWSList = new ArrayList<>();
		boolean isFirst = true;
		DataFormatter formatter = new DataFormatter();
		Iterator<Row> rows = sheet.iterator();

		//checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
	     int maxValue = Integer.MAX_VALUE;
	     int loopIndex = 0;
	     while (rows.hasNext() && loopIndex <= maxValue) {
	    	 if(loopIndex == maxValue) {
	    		 throw TsmpDpAaRtnCode._1559.throwing("Exceed " + maxValue + " row");
	    	 }
	    	 loopIndex++;
			if (isFirst) {
				isFirst = false;
				Row row = rows.next();
				if (!"WEBSITE_NAME".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"WEBSITE_STATUS".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"REMARK".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"AUTH".equalsIgnoreCase(formatter.formatCellValue(row.getCell(3)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"SQL_INJECTION".equalsIgnoreCase(formatter.formatCellValue(row.getCell(4)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"TRAFFIC".equalsIgnoreCase(formatter.formatCellValue(row.getCell(5)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"XSS".equalsIgnoreCase(formatter.formatCellValue(row.getCell(6)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"XXE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(7)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"TPS".equalsIgnoreCase(formatter.formatCellValue(row.getCell(8)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"IGNORE_API".equalsIgnoreCase(formatter.formatCellValue(row.getCell(9)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}
				if (!"SHOW_LOG".equalsIgnoreCase(formatter.formatCellValue(row.getCell(10)))) {
					throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
				}

			} else {
				Row row = rows.next();
				// 空白列不處理

				if (!StringUtils.hasText(formatter.formatCellValue(row.getCell(0)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(1)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(2)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(3)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(4)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(5)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(6)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(7)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(8)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(9)))
						&& !StringUtils.hasText(formatter.formatCellValue(row.getCell(10)))) {
					continue;
				}
				DgrWebsite ws = new DgrWebsite();

				String websiteName = formatter.formatCellValue(row.getCell(0));
				if (!StringUtils.hasLength(websiteName)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{websietName}}");
				}

				ws.setWebsiteName(websiteName);
				String websieStatus = formatter.formatCellValue(row.getCell(1));
				if (!StringUtils.hasLength(websieStatus)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{websieStatus}}");
				}
				ws.setWebsiteStatus(websieStatus);
				ws.setRemark(formatter.formatCellValue(row.getCell(2)));

				String auth = formatter.formatCellValue(row.getCell(3));
				if (!StringUtils.hasLength(auth)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{auth}}");
				}
				ws.setAuth(auth);

				String sqlInjection = formatter.formatCellValue(row.getCell(4));
				if (!StringUtils.hasLength(sqlInjection)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{sqlInjection}}");
				}
				ws.setSqlInjection(sqlInjection);

				String traffic = formatter.formatCellValue(row.getCell(5));
				if (!StringUtils.hasLength(traffic)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{traffic}}");
				}
				ws.setTraffic(traffic);

				String xss = formatter.formatCellValue(row.getCell(6));
				if (!StringUtils.hasLength(xss)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{xss}}");
				}
				ws.setXss(xss);

				String xxe = formatter.formatCellValue(row.getCell(7));
				if (!StringUtils.hasLength(xxe)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{xxe}}");
				}
				ws.setXxe(xxe);

				String tps = formatter.formatCellValue(row.getCell(8));
				if (!StringUtils.hasLength(tps)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{tps}}");
				}
				ws.setTps(Integer.parseInt(tps));
				ws.setIgnoreApi(formatter.formatCellValue(row.getCell(9)));

				String showLog = formatter.formatCellValue(row.getCell(10));
				if (!StringUtils.hasLength(showLog)) {
					throw TsmpDpAaRtnCode._1350.throwing("{{showLog}}");
				}
				ws.setShowLog(showLog);
				excelWSList.add(ws);

			}

		}
		return excelWSList;

	}

	public void checkParam(String fileName) {

		int fileNameIndex = fileName.lastIndexOf(".");
		if (fileNameIndex == -1) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}

		String fileExtension = fileName.substring(fileNameIndex + 1);
		if (!"xlsx".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}

	}

	protected DgrWebsiteDao getDgrWebsiteDao() {
		return dgrWebsiteDao;
	}

	protected DgrWebsiteDetailDao getDgrWebsiteDetailDao() {
		return dgrWebsiteDetailDao;
	}

	protected DigiRunnerGtwDeployProperties getDigiRunnerGtwDeployProperties() {
		return digiRunnerGtwDeployProperties;
	}
}
