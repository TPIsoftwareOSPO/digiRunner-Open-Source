package tpi.dgrv4.dpaa.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Setter;
import tpi.dgrv4.common.constant.MultilingualSheetNameEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9940Req;
import tpi.dgrv4.dpaa.vo.EmptyBodyResp;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.repository.TsmpDpItemsDao;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.escape.DPB9922Service;
import tpi.dgrv4.escape.DPB9924Service;
import tpi.dgrv4.escape.DPB9926Service;
import tpi.dgrv4.escape.DPB9928Service;
import tpi.dgrv4.escape.DPB9936Service;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class MultilingualManagementService {

	@Setter(onMethod_ = @Autowired)
	private TsmpFuncDao tsmpFuncDao;
	
	@Setter(onMethod_ = @Autowired)
	private TsmpReportUrlDao tsmpReportUrlDao;
	
	@Setter(onMethod_ = @Autowired)
	private TsmpDpItemsDao tsmpDpItemsDao;
	
	@Setter(onMethod_ = @Autowired)
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9921Service dpb9921Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9922Service dpb9922Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9923Service dpb9923Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9924Service dpb9924Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9925Service dpb9925Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9926Service dpb9926Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9927Service dpb9927Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9928Service dpb9928Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9935Service dpb9935Service;
	
	@Setter(onMethod_ = @Autowired)
	private DPB9936Service dpb9936Service;
	
	private EmptyBodyResp emptyBodyResp = new EmptyBodyResp();
	
	@Transactional
	public EmptyBodyResp importMultilingual(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {
		try {
			if(mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}
	
			checkImportParam(mFile.getOriginalFilename());
			
			importData(tsmpAuthorization, mFile.getInputStream());

			return emptyBodyResp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	public void importData(TsmpAuthorization tsmpAuthorization, InputStream inputStream) throws Exception {
		// [EN] Read stream into memory first to allow multiple reads without exhaustion
		// [ZH] 先將 stream 讀入記憶體，避免後續多次讀取時 stream 已耗盡
		byte[] bytes = inputStream.readAllBytes();

		//Get all sheet names
		ArrayList<String> sheetNameList = new ArrayList<>();
		try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));) {
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				sheetNameList.add(workbook.getSheetName(i));
			}
			
		}
		if(sheetNameList.size() == 0) {
			throw TsmpDpAaRtnCode._1559.throwing("No sheet");
		}


		for(String sheetName : sheetNameList) {
			if(MultilingualSheetNameEnum.SETTING.value().equals(sheetName)) {
				this.getDpb9922Service().importData(new ByteArrayInputStream(bytes));
				TPILogger.tl.info("setting import finish");
			}else if(MultilingualSheetNameEnum.ITEMS.value().equals(sheetName)) {
				this.getDpb9924Service().importData(tsmpAuthorization, new ByteArrayInputStream(bytes));
				TPILogger.tl.info("items import finish");
			}else if(MultilingualSheetNameEnum.MAIL_TPLT.value().equals(sheetName)) {
				this.getDpb9926Service().importData(tsmpAuthorization, new ByteArrayInputStream(bytes));
				TPILogger.tl.info("mailTplt import finish");
			}else if(MultilingualSheetNameEnum.FUNC.value().equals(sheetName)) {
				this.getDpb9928Service().importDataFunc(tsmpAuthorization, new ByteArrayInputStream(bytes), "0");
				this.getDpb9928Service().importDataReportUrl(tsmpAuthorization, new ByteArrayInputStream(bytes), "0");
				TPILogger.tl.info("func import finish");
			}else if(MultilingualSheetNameEnum.EMBEDDED_FUNC.value().equals(sheetName)) {
				this.getDpb9928Service().importDataFunc(tsmpAuthorization, new ByteArrayInputStream(bytes), "1");
				this.getDpb9928Service().importDataReportUrl(tsmpAuthorization, new ByteArrayInputStream(bytes), "1");
				TPILogger.tl.info("embeddedFunc import finish");
			}else if(MultilingualSheetNameEnum.RTNCODE.value().equals(sheetName)) {
				this.getDpb9936Service().importData(tsmpAuthorization, new ByteArrayInputStream(bytes));
				TPILogger.tl.info("rtnCode import finish");
			}
		}
	}
	
	public void checkImportParam(String fileName) {
		
		int fileNameIndex = fileName.lastIndexOf(".");
		if(fileNameIndex == -1) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}
		
		String fileExtension = fileName.substring(fileNameIndex + 1);
		if(!"xlsx".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
	}

	public void exportMultilingual(TsmpAuthorization tsmpAuthorization, DPB9940Req req, HttpServletResponse response) {

		try (XSSFWorkbook workbook = new XSSFWorkbook();
				OutputStream outputStream = response.getOutputStream();){
			
			if(req.getExportItemList().contains(MultilingualSheetNameEnum.SETTING.value())) {
				generateSetting(workbook);
				TPILogger.tl.info("setting export finish");
			}
			if(req.getExportItemList().contains(MultilingualSheetNameEnum.ITEMS.value())) {
				generateItems(workbook, req);
				TPILogger.tl.info("items export finish");
			}
			if(req.getExportItemList().contains(MultilingualSheetNameEnum.MAIL_TPLT.value())) {
				generateMail(workbook);
				TPILogger.tl.info("mailTplt export finish");
			}
			if(req.getExportItemList().contains(MultilingualSheetNameEnum.FUNC.value())) {
				generateFunc(workbook, req);
				TPILogger.tl.info("func export finish");
			}
			if(req.getExportItemList().contains(MultilingualSheetNameEnum.EMBEDDED_FUNC.value())) {
				generateEmbeddedFunc(workbook, req);
				TPILogger.tl.info("embeddedFunc export finish");
			}
			if(req.getExportItemList().contains(MultilingualSheetNameEnum.RTNCODE.value())) {
				generateRtnCode(workbook, req);
				TPILogger.tl.info("rtnCode export finish");
			}
			
			
			doExportMultilingual(response);
			//寫入串流
			workbook.write(outputStream);
		    
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	private void generateSetting(XSSFWorkbook workbook) {
		try {
			XSSFSheet settingSheet = workbook.createSheet(MultilingualSheetNameEnum.SETTING.value());
			this.getDpb9921Service().writeHeader(workbook, settingSheet);
			this.getDpb9921Service().writeData(settingSheet);
		}catch(TsmpDpAaException e) {
			if(!TsmpDpAaRtnCode._1298.getCode().equals(e.getError().getCode())) {
				throw e;
			}
		}
	}
	
	private void generateMail(XSSFWorkbook workbook) {
		try {
			XSSFSheet mailSheet = workbook.createSheet(MultilingualSheetNameEnum.MAIL_TPLT.value());
			this.getDpb9925Service().writeHeader(workbook, mailSheet);
			this.getDpb9925Service().writeData(mailSheet);
		}catch(TsmpDpAaException e) {
			if(!TsmpDpAaRtnCode._1298.getCode().equals(e.getError().getCode())) {
				throw e;
			}
		}
	}
	
	private void generateItems(XSSFWorkbook workbook, DPB9940Req req) {
		try {
			XSSFSheet itemsSheet = workbook.createSheet(MultilingualSheetNameEnum.ITEMS.value());
			List<TsmpDpItems> itemsDataList = getTsmpDpItemsDao().findByLocaleInOrderByItemIdAsc(req.getExportLocaleList());
			this.getDpb9923Service().writeHeader(workbook, itemsSheet);
			this.getDpb9923Service().writeData(itemsSheet, itemsDataList);
		}catch(TsmpDpAaException e) {
			if(!TsmpDpAaRtnCode._1298.getCode().equals(e.getError().getCode())) {
				throw e;
			}
		}
	}
	
	private void generateFunc(XSSFWorkbook workbook, DPB9940Req req) {
		
		List<TsmpFunc> funcDataList = new ArrayList<>();
		try {
			//func
			XSSFSheet funcSheet = workbook.createSheet(MultilingualSheetNameEnum.FUNC.value());
			funcDataList = getTsmpFuncDao().findByFuncTypeAndLocaleIn("0", req.getExportLocaleList());
			this.getDpb9927Service().writeHeaderFunc(workbook, funcSheet);
			this.getDpb9927Service().writeDataFunc(funcSheet, funcDataList);
		}catch(TsmpDpAaException e) {
			if(!TsmpDpAaRtnCode._1298.getCode().equals(e.getError().getCode())) {
				throw e;
			}
		}
		
		try {
			//func_reportUrl
			List<String> funcCodeList = funcDataList.stream().map(TsmpFunc::getFuncCode).collect(Collectors.toList());
			List<TsmpReportUrl> reportUrlDataList = getTsmpReportUrlDao().queryAllByReportId(funcCodeList);
			XSSFSheet reportUrlSheet = workbook.createSheet(MultilingualSheetNameEnum.FUNC_REPORT_URL.value());
			this.getDpb9927Service().writeHeaderReportUrl(workbook, reportUrlSheet);
			this.getDpb9927Service().writeDataReportUrl(reportUrlSheet, reportUrlDataList);
		}catch(TsmpDpAaException e) {
			if(!TsmpDpAaRtnCode._1298.getCode().equals(e.getError().getCode())) {
				throw e;
			}
		}
	}
	
	private void generateEmbeddedFunc(XSSFWorkbook workbook, DPB9940Req req) {
		List<TsmpFunc> embeddedFuncDataList = new ArrayList<>();
		try {
			XSSFSheet embeddedFuncSheet = workbook.createSheet(MultilingualSheetNameEnum.EMBEDDED_FUNC.value());
			embeddedFuncDataList = getTsmpFuncDao().findByFuncTypeAndLocaleIn("1", req.getExportLocaleList());
			this.getDpb9927Service().writeHeaderFunc(workbook, embeddedFuncSheet);
			this.getDpb9927Service().writeDataFunc(embeddedFuncSheet, embeddedFuncDataList);
		}catch(TsmpDpAaException e) {
			if(!TsmpDpAaRtnCode._1298.getCode().equals(e.getError().getCode())) {
				throw e;
			}
		}
		
		try {
			List<String> embeddedFuncCodeList = embeddedFuncDataList.stream().map(TsmpFunc::getFuncCode).collect(Collectors.toList());
			List<TsmpReportUrl> reportUrlDataList = getTsmpReportUrlDao().queryAllByReportId(embeddedFuncCodeList);
			XSSFSheet reportUrlSheet = workbook.createSheet(MultilingualSheetNameEnum.EMBEDDED_FUNC_REPORT_URL.value());
			this.getDpb9927Service().writeHeaderReportUrl(workbook, reportUrlSheet);
			this.getDpb9927Service().writeDataReportUrl(reportUrlSheet, reportUrlDataList);
		}catch(TsmpDpAaException e) {
			if(!TsmpDpAaRtnCode._1298.getCode().equals(e.getError().getCode())) {
				throw e;
			}
		}
	}
	
	private void generateRtnCode(XSSFWorkbook workbook, DPB9940Req req) {
		XSSFSheet rtnCodeSheet = workbook.createSheet("rtnCode");
		List<TsmpRtnCode> rtnCodeDataList = getTsmpRtnCodeDao().findByLocaleInOrderByTsmpRtnCodeAsc(req.getExportLocaleList());
		this.getDpb9935Service().writeHeader(workbook, rtnCodeSheet);
		this.getDpb9935Service().writeData(rtnCodeSheet, rtnCodeDataList);
	}
	
	
	private void doExportMultilingual(HttpServletResponse response) {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd_HHmm");
        String nowDateTime = dateFormatter.format(new Date());
        
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=multilingual_" + nowDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //checkmarx, Missing HSTS Header
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload"); 
        
	}

	
	protected TsmpFuncDao getTsmpFuncDao() {
		return tsmpFuncDao;
	}

	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}

	protected TsmpDpItemsDao getTsmpDpItemsDao() {
		return tsmpDpItemsDao;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return tsmpRtnCodeDao;
	}

	protected DPB9921Service getDpb9921Service() {
		return dpb9921Service;
	}

	protected DPB9923Service getDpb9923Service() {
		return dpb9923Service;
	}

	protected DPB9925Service getDpb9925Service() {
		return dpb9925Service;
	}

	protected DPB9927Service getDpb9927Service() {
		return dpb9927Service;
	}

	protected DPB9935Service getDpb9935Service() {
		return dpb9935Service;
	}

	protected DPB9922Service getDpb9922Service() {
		return dpb9922Service;
	}

	protected DPB9924Service getDpb9924Service() {
		return dpb9924Service;
	}

	protected DPB9926Service getDpb9926Service() {
		return dpb9926Service;
	}

	protected DPB9928Service getDpb9928Service() {
		return dpb9928Service;
	}

	protected DPB9936Service getDpb9936Service() {
		return dpb9936Service;
	}
	
	
}
