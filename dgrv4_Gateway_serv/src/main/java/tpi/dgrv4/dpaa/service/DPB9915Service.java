package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB9915Item;
import tpi.dgrv4.dpaa.vo.DPB9915Req;
import tpi.dgrv4.dpaa.vo.DPB9915Resp;
import tpi.dgrv4.dpaa.vo.DPB9915Trunc;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;


@Service
public class DPB9915Service  {

	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private BcryptParamHelper bcryptParamHelper;
	private FileHelper fileHelper;
	private ServiceConfig serviceConfig;
	private TsmpSettingService tsmpSettingService;
	
	private Long expMs;

	private Integer pageSize;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB9915Service(TsmpDpFileDao tsmpDpFileDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			BcryptParamHelper bcryptParamHelper, FileHelper fileHelper, ServiceConfig serviceConfig,
			TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.bcryptParamHelper = bcryptParamHelper;
		this.fileHelper = fileHelper;
		this.serviceConfig = serviceConfig;
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB9915Resp queryTsmpDpFileList(TsmpAuthorization tsmpAuthorization, DPB9915Req req, String locale) {
		
		List<TsmpDpFile> tsmpDpFiles = new ArrayList<>();
		DPB9915Resp resp = new DPB9915Resp();
		try {
			// 日期格式檢查
			String reqStartDate = req.getStartDate()+":00";
			String reqEndDate = req.getEndDate()+":59";
			Date startDate = checkDateFormat(reqStartDate);
			Date endDate = checkDateFormat(reqEndDate);
			if (endDate.before(startDate)) {
				throw TsmpDpAaRtnCode._1367.throwing();
			}

			// 有傳入RefFileCateCode
			String reqRefFileCateCode = req.getRefFileCateCode();
			String refFileCateCode = null;
			if (StringUtils.hasLength(reqRefFileCateCode)) {
				refFileCateCode = decodeRefFileCateCode(reqRefFileCateCode, locale);
			}

			// 有傳入fileId
			Long fileId = req.getFileId();
			Date lastDateTime = null;
			Long lastId = null;
			if (fileId != null) {
				TsmpDpFile lastFile = getlastTsmpDpFile(fileId);
				lastDateTime = lastFile.getCreateDateTime();
				if (lastFile.getUpdateDateTime() != null) {
					lastDateTime = lastFile.getUpdateDateTime();
				}
				lastId = lastFile.getFileId();
			}

			// 有傳入kewords
			String keywords = req.getKeyword();
			ArrayList<Long> fileIds = new ArrayList<>();
			List<String> fileNames = new ArrayList<>();

			if (StringUtils.hasLength(keywords)) {
				String[] words = ServiceUtil.getKeywords(keywords, " ");
				fileIds = getFileIds(words);
				fileNames = Arrays.asList(words);
			}

			// 搜尋
			Long refId = req.getRefId();
			String isTempFile = req.getIsTmpfile();
			tsmpDpFiles = getTsmpDpFileList(lastId, lastDateTime, startDate, endDate, fileIds, //
					fileNames, refFileCateCode, refId, isTempFile);
			// 判斷是否為查詢資源回收桶資料，若是則不用拋1298
			if (!"Y".equals(isTempFile)) {
				if (CollectionUtils.isEmpty(tsmpDpFiles)) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
			}

			getResp(resp, req, tsmpDpFiles, locale);
			getFileHelper().fireTsmpDpFileJob(false);			
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;

	}

	private void getResp(DPB9915Resp resp, DPB9915Req req, List<TsmpDpFile> tsmpDpFiles, String locale) {
		String isTmpfile = req.getIsTmpfile();
		if (StringUtils.hasLength(isTmpfile) && isTmpfile.equals("Y")) {
			String hour = getDeleteDays(getExpMs());
			resp.setAutoDeleteDays(hour);
		}
		ArrayList<DPB9915Item> dataList = new ArrayList<>();
		for (TsmpDpFile tsmpDpFile : tsmpDpFiles) {
			DPB9915Item item = new DPB9915Item();
			DPB9915Trunc trunc = new DPB9915Trunc();

			Long fileId = tsmpDpFile.getFileId();
			item.setFileId(fileId);
			String fileName = tsmpDpFile.getFileName();

			trunc = getTrunc(fileName, 30);

			item.setFileName(trunc);
			String refFileCateCode = tsmpDpFile.getRefFileCateCode();
			String refFileCateCodeName = getItemRefFileCateCodeName(refFileCateCode, locale);

			item.setRefFileCateCode(refFileCateCodeName);
			item.setRefId(tsmpDpFile.getRefId());
			Date lastDate = tsmpDpFile.getCreateDateTime();
			String lastUser = tsmpDpFile.getCreateUser();
			Date updDateTime = tsmpDpFile.getUpdateDateTime();
			if (updDateTime != null) {
				lastDate = updDateTime;
				lastUser = tsmpDpFile.getUpdateUser();
			}
			String lastDateString = DateTimeUtil.dateTimeToString(lastDate, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
			item.setLastUpdDateTime(lastDateString);
			item.setLastUpdUser(lastUser);

			if (checkHasBlobData(fileId)) {
				String filePath = tsmpDpFile.getFilePath() + tsmpDpFile.getFileName();
				item.setFilePath(filePath);
			}
			item.setVersion(tsmpDpFile.getVersion());
			dataList.add(item);
		}
		resp.setDataList(dataList);
	}

	private String getItemRefFileCateCodeName(String refFileCateCode, String locale) {
		TsmpDpItems tsmpDpItems = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("FILE_CATE_CODE", //
				refFileCateCode, locale);
		String ItemRefFileCateCodeName = refFileCateCode;
		if (tsmpDpItems != null) {
			String refFileCateCodeName = tsmpDpItems.getSubitemName();
			ItemRefFileCateCodeName = refFileCateCode + " - " + refFileCateCodeName;
		}
		return ItemRefFileCateCodeName;

	}

	private String getDeleteDays(Long expMs) {
		String hour = "*";
		try {
			BigDecimal bigDecimal = new BigDecimal(expMs);
			BigDecimal toHour = new BigDecimal(1000 * 60 * 60);
			bigDecimal = bigDecimal.divide(toHour, 2, RoundingMode.HALF_UP);
			hour = bigDecimal.toString();
		} catch (Exception e) {
			logger.error("time conversion error");
		}
		return hour;
	}

	private boolean checkHasBlobData(Long fileId) {
		boolean state = false;
		TsmpDpFileDao dao = getTsmpDpFileDao();
		List<Integer> list = dao.query_DPB9915Service_02(fileId);

		Integer value = 0;
		if (!list.isEmpty()) {
			value = list.get(0);
		}
		
		if (value == 1) {
			state = true;
		}
		return state;
	}

	private ArrayList<Long> getFileIds(String[] keywords) {
		String pattern = "^\\-[1-9]\\d*$|^(0|[1-9]\\d*)$";
		boolean isMatch;
		ArrayList<Long> fileIds = new ArrayList<>();
		for (String string : keywords) {
			isMatch = Pattern.matches(pattern, string);
			if (isMatch) {
				fileIds.add(Long.valueOf(string));
			}
		}
		return fileIds;
	}

	private List<TsmpDpFile> getTsmpDpFileList(Long lastId, Date lastDateTime, Date startDate, Date endDate,
			ArrayList<Long> fileIds, List<String> fileNames, String refFileCateCode, Long refId, String isTempFile) {

		List<TsmpDpFile> tsmpDpFiles = getTsmpDpFileDao().query_DPB9915Service_01(lastId, lastDateTime, startDate,
				endDate, fileIds, fileNames, refFileCateCode, refId, isTempFile, getPageSize());
		return tsmpDpFiles;
	}

	private TsmpDpFile getlastTsmpDpFile(Long fileId) {
		Optional<TsmpDpFile> tsmpDpFile = getTsmpDpFileDao().findById(fileId);
		if (!tsmpDpFile.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return tsmpDpFile.get();
	}

	protected String decodeRefFileCateCode(String refFileCateCode, String locale) {
		if (StringUtils.hasLength(refFileCateCode)) {
			try {
				refFileCateCode = getBcryptParamHelper().decode(refFileCateCode, "FILE_CATE_CODE", locale);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
		return refFileCateCode;
	}

	private Date checkDateFormat(String dateString) {
		Date date;
		try {
			date = DateTimeUtil.stringToDateTime(dateString, DateTimeFormatEnum.西元年月日時分秒_2).orElseThrow();
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		return date;
	}

	private DPB9915Trunc getTrunc(String value, int maxLength) {
		DPB9915Trunc dpb9915Trunc = new DPB9915Trunc();
		dpb9915Trunc.setT(Boolean.FALSE);
		dpb9915Trunc.setVal(value);
		if (StringUtils.hasLength(value) && value.length() > maxLength) {
			dpb9915Trunc.setT(Boolean.TRUE);
			dpb9915Trunc.setOri(value);
			dpb9915Trunc.setVal(value.substring(0, maxLength) + "...");
		}
		return dpb9915Trunc;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb9915");
		return this.pageSize;
	}

	protected Long getExpMs() {
		this.expMs = tsmpSettingService.getVal_FILE_TEMP_EXP_TIME();
		return this.expMs;
	}
}
