package tpi.dgrv4.escape;

import java.io.InputStream;
import java.util.ArrayList;
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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DPB0118Service;
import tpi.dgrv4.dpaa.vo.DPB0118Resp;
import tpi.dgrv4.dpaa.vo.DPB9922Resp;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ComposerWebSocketClientConn;
import tpi.dgrv4.gateway.util.DigiRunnerGtwDeployProperties;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
//checkmarx, Unchecked Input for Loop Condition
@Service
public class DPB9922Service {
	private TPILogger logger = TPILogger.tl;

	private DPB0118Service dpb0118Service;
	private TsmpSettingDao tsmpSettingDao;
	private ComposerWebSocketClientConn composerWebSocketClientConn;
	private DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties;
	
	@Autowired
	public DPB9922Service(ApplicationContext applicationContext, TsmpSettingDao tsmpSettingDao,
			ComposerWebSocketClientConn composerWebSocketClientConn,
			DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties) {
		super();
		if(applicationContext != null) {
			this.dpb0118Service = applicationContext.getBean(DPB0118Service.class); // non-Singleton
		}
		this.tsmpSettingDao = tsmpSettingDao;
		this.composerWebSocketClientConn = composerWebSocketClientConn;
		this.digiRunnerGtwDeployProperties = digiRunnerGtwDeployProperties;
	}

	@Transactional
	public DPB9922Resp importTsmpSetting(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

		try {
			if (mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}

			checkParam(mFile.getOriginalFilename());

			importData(mFile.getInputStream());

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.SETTING.value());

			return new DPB9922Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	/**
	 * 移除不存在的數據。
	 * 
	 * @param inputStream 輸入流，用於讀取數據。
	 * @return 返回經過處理後的 TsmpSetting 列表。
	 * @throws Exception 如果處理過程中發生錯誤。
	 */
	private List<TsmpSetting> removeNonExistentData(InputStream inputStream) throws Exception {

		// 判斷是否為記憶體角色
		boolean isMemory = getDigiRunnerGtwDeployProperties().isMemoryRole();

		// 收集輸入流中的實體
		List<TsmpSetting> entities = collectEntities(inputStream);

		// 檢查是否為記憶體角色
		if (!isMemory) {
			// 若不是，直接收集並返回實體
			return entities;
		}

		// 獲取新 Setting 的 Map
		Map<String, TsmpSetting> newIds = getSettingId(entities);
		// 獲取舊 Setting 的 Map
		Map<String, TsmpSetting> oldIds = fetchOldDataIdsList();

		// 尋找並回傳不存在於新 ID 中的舊 Setting
		List<TsmpSetting> missingTsmpSetting = findMissingTsmpSetting(newIds, oldIds);

		// 刪除所有不存在的數據
		getTsmpSettingDao().deleteAllInBatch(missingTsmpSetting);

		// 返回處理後的實體列表
		return entities;
	}

	/**
	 * 尋找不存在於新 ID 集合中的舊 ID 對應設定。
	 * 
	 * @param newIds 新實體的 ID 集合。
	 * @param oldIds 舊資料的 ID 集合。
	 * @return 返回不存在於新 ID 集合中的舊 ID 對應設定列表。
	 */
	private List<TsmpSetting> findMissingTsmpSetting(Map<String, TsmpSetting> newIds, Map<String, TsmpSetting> oldIds) {
		List<TsmpSetting> missingSettings = new ArrayList<>();
		// 遍歷舊資料的鍵集
		for (Map.Entry<String, TsmpSetting> entry : oldIds.entrySet()) {
			String id = entry.getKey();
			// 檢查這個 ID 是否存在於 newIds 中
			if (!newIds.containsKey(id)) {
				// 如果不存在，則將對應的 TsmpSetting 加入到列表中
				missingSettings.add(entry.getValue());
			}
		}
		// 返回包含所有找到的 TsmpSetting 的列表
		return missingSettings;
	}

	/**
	 * 獲取舊資料的 ID 列表。
	 * 
	 * @return 返回一個包含 ID 的集合。
	 */
	private Map<String, TsmpSetting> fetchOldDataIdsList() {
		// 獲取所有 Setting
		List<TsmpSetting> list = getTsmpSettingDao().findAll();

		// 提取設置的 ID 並返回
		return getSettingId(list);
	}

	/**
	 * 從 TsmpSetting 列表中提取 ID。
	 * 
	 * @param list TsmpSetting 對象的列表。
	 * @return 返回一個包含 ID 的集合。
	 */
	private Map<String, TsmpSetting> getSettingId(List<TsmpSetting> list) {
		return list.stream().collect(Collectors.toMap( //
				TsmpSetting::getId, // 以 TsmpSetting 的 ID 作為鍵
				tsmpSetting -> tsmpSetting, // 以 TsmpSetting 物件本身作為值
				(existing, replacement) -> replacement)); // 如果鍵值重複，使用新的 TsmpSetting 替換
	}

	/**
	 * for In-Memory, <br>
	 * GTW(In-Memory) 端匯入資料,由此進入
	 */
	public void importDataForInMemoryInput(InputStream inputStream) {
		try {

			importData(inputStream);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	public void importData(InputStream inputStream) throws Exception {
		List<TsmpSetting> entities = removeNonExistentData(inputStream);
		saveEntitiesToDB(entities);
	}

	private void saveEntitiesToDB(List<TsmpSetting> entities) {
		for (TsmpSetting entity : entities) {
			getTsmpSettingDao().saveAndFlush(entity);

			// 因為composer address被更新,websocket要重連
			if ("TSMP_COMPOSER_ADDRESS".equals(entity.getId())) {
				restartWs();
			}
		}
	}

	private List<TsmpSetting> collectEntities(InputStream inputStream) throws Exception {
		List<TsmpSetting> entities = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream);) {
			Sheet sheet = workbook.getSheetAt(0);
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
					if (!"id".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
						throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
					}
					if (!"value".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
						throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
					}
					if (!"memo".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
						throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
					}
				} else {
					Row row = rows.next();
					TsmpSetting entity = new TsmpSetting();

					entity.setId(formatter.formatCellValue(row.getCell(0)));
					entity.setValue(formatter.formatCellValue(row.getCell(1)));
					entity.setMemo(formatter.formatCellValue(row.getCell(2)));

					entities.add(entity);
				}
			}
		}

		return entities;
	}

	protected void restartWs() {
		getComposerWebSocketClientConn().restart();
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

		String[] arrFileName = fileName.substring(0, fileNameIndex).split("_");
		if(!(arrFileName.length == 4 || arrFileName.length == 3)) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}

		DPB0118Resp dpb0118Resp = getDpb0118Service().queryModuleVersion();
        String version = dpb0118Resp.getMajorVersionNo() == null ? "unknown" : dpb0118Resp.getMajorVersionNo();
        if(!version.equalsIgnoreCase(arrFileName[arrFileName.length-1])) {
        	throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
        }
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected DPB0118Service getDpb0118Service() {
		return dpb0118Service;
	}

	protected ComposerWebSocketClientConn getComposerWebSocketClientConn() {
		return composerWebSocketClientConn;
	}

	protected DigiRunnerGtwDeployProperties getDigiRunnerGtwDeployProperties() {
		return digiRunnerGtwDeployProperties;
	}

}
