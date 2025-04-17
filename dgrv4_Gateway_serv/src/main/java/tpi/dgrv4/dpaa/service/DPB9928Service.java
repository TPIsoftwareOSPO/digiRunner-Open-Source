package tpi.dgrv4.dpaa.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB9928Req;
import tpi.dgrv4.dpaa.vo.DPB9928Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpFuncId;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrlId;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpReportUrlDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9928Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpFuncDao tsmpFuncDao;
	private TsmpReportUrlDao tsmpReportUrlDao;
	private BcryptParamHelper bcryptParamHelper;
	
	@Autowired
	public DPB9928Service(TsmpFuncDao tsmpFuncDao, TsmpReportUrlDao tsmpReportUrlDao,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpReportUrlDao = tsmpReportUrlDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	@Transactional
	public DPB9928Resp importTsmpFunc(TsmpAuthorization tsmpAuthorization, MultipartFile mFile, DPB9928Req req, ReqHeader reqHeader) {

		try {
			if(mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}
			
			checkParam(mFile.getOriginalFilename());

     		importDataFunc(tsmpAuthorization, mFile.getInputStream(), req.getFuncType(), reqHeader.getLocale());
     		importDataReportUrl(tsmpAuthorization, mFile.getInputStream());
		    
     		return new DPB9928Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
	}
	
	protected void importDataFunc(TsmpAuthorization tsmpAuthorization, InputStream inputStream, String encodeFuncType, String locale) throws Exception {
		try (Workbook workbook = new XSSFWorkbook(inputStream);){
			 if (workbook.getNumberOfSheets() != 2) {
				 throw TsmpDpAaRtnCode._1291.throwing();
			 }
			 
			 String funcType = getFuncTypeByBcryptParamHelper(encodeFuncType, locale);
			
			 Sheet sheet = workbook.getSheetAt(0);
			 boolean isFirst = true;
			 DataFormatter formatter = new DataFormatter();
		     Iterator<Row> rows = sheet.iterator();
		     Map<TsmpFuncId, TsmpFunc> fileDataList = new HashMap<TsmpFuncId, TsmpFunc>();
		     Map<TsmpFuncId, TsmpFunc> tsmpFuncMap = new HashMap<TsmpFuncId, TsmpFunc>();
		     
		   //checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
		     int maxValue = Integer.MAX_VALUE;
		     int loopIndex = 0;
		     while (rows.hasNext() && loopIndex <= maxValue) {
		    	 if(loopIndex == maxValue) {
		    		 throw TsmpDpAaRtnCode._1559.throwing("Exceed " + maxValue + " row");
		    	 }
		    	 loopIndex++;
		    	 if(isFirst) {
		    		 isFirst = false;
		    		 Row row = rows.next();
		    		 if(!"FUNC_CODE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"FUNC_NAME".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"FUNC_NAME_EN".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"FUNC_DESC".equalsIgnoreCase(formatter.formatCellValue(row.getCell(3)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"LOCALE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(4)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"FUNC_TYPE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(5)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    	 }else {
		    		 Row row = rows.next();
		    		 //空白列不處理
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(0))) && 
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(1))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(2))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(3))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(4))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(5)))) {
		    			 continue;
		    		 }
		    		 
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("FUNC_CODE");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("FUNC_NAME");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(4)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("LOCALE");
		    		 }
		    		 String fileFuncType = formatter.formatCellValue(row.getCell(5));
		    		 if(!funcType.equals(fileFuncType)) {
		    			 throw TsmpDpAaRtnCode._1290.throwing();
		    		 }
		    		 
		    		 TsmpFunc vo = parseRowToTsmpFunc(row);
		    		 fileDataList.put(new TsmpFuncId(vo.getFuncCode(), vo.getLocale()), vo);
		    	 }
		     }
		     
		     List<TsmpFunc> funcList = getTsmpFuncDao().findAll();
		     for(TsmpFunc tsmpFunc : funcList) {
		    	 tsmpFuncMap.put(new TsmpFuncId(tsmpFunc.getFuncCode(), tsmpFunc.getLocale()), tsmpFunc);
		     }
		     
		     List<TsmpFunc> changeFuncList = new ArrayList<TsmpFunc>();
		     for (Map.Entry<TsmpFuncId, TsmpFunc> entry : fileDataList.entrySet()) {
		    	TsmpFuncId k = entry.getKey();
		    	TsmpFunc v = entry.getValue();
		    	
	            if(tsmpFuncMap.containsKey(k)) {
	            	TsmpFunc func =  tsmpFuncMap.get(k);
	            	func.setFuncName(v.getFuncName());
	            	func.setFuncNameEn(v.getFuncNameEn());
	            	func.setFuncDesc(v.getFuncDesc());
	            	func.setFuncType(v.getFuncType());
	            	func.setUpdateTime(DateTimeUtil.now());
	            	func.setUpdateUser(tsmpAuthorization.getUserName());
	            	changeFuncList.add(func);
	            }else {
	            	v.setUpdateTime(DateTimeUtil.now());
	            	v.setUpdateUser(tsmpAuthorization.getUserName());
	            	changeFuncList.add(v);
	            }
		     }
		     getTsmpFuncDao().saveAll(changeFuncList);
		}
	}
	
	protected void importDataReportUrl(TsmpAuthorization tsmpAuthorization, InputStream inputStream) throws Exception {
		try (Workbook workbook = new XSSFWorkbook(inputStream);){
			 if (workbook.getNumberOfSheets() != 2) {
				 throw TsmpDpAaRtnCode._1291.throwing();
			 }
	
			 Sheet sheet = workbook.getSheetAt(1);
			 boolean isFirst = true;
			 DataFormatter formatter = new DataFormatter();
		     Iterator<Row> rows = sheet.iterator();
		     Map<TsmpReportUrlId, TsmpReportUrl> fileDataList = new HashMap<TsmpReportUrlId, TsmpReportUrl>();
		     Map<TsmpReportUrlId, TsmpReportUrl> tsmpReportUrlMap = new HashMap<TsmpReportUrlId, TsmpReportUrl>();
		   //checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
		     int maxValue = Integer.MAX_VALUE;
		     int loopIndex = 0;
		     while (rows.hasNext() && loopIndex <= maxValue) {
		    	 if(loopIndex == maxValue) {
		    		 throw TsmpDpAaRtnCode._1559.throwing("Exceed " + maxValue + " row");
		    	 }
		    	 loopIndex++;
		    	 if(isFirst) {
		    		 isFirst = false;
		    		 Row row = rows.next();
		    		 if(!"REPORT_ID".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"TIME_RANGE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    	 }else {
		    		 Row row = rows.next();
		    		 //空白列不處理
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(0))) && 
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(1))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(2)))) {
		    			 continue;
		    		 }
		    		 
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("REPORT_ID");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("TIME_RANGE");
		    		 }
		    		 
		    		 TsmpReportUrl vo = parseRowToTsmpReportUrl(row);
		    		 fileDataList.put(new TsmpReportUrlId(vo.getReportId(), vo.getTimeRange()), vo);
		    	 }
		     }
		     
		     List<TsmpReportUrl> reportUrlList = getTsmpReportUrlDao().findAll();
		     for(TsmpReportUrl reportUrl : reportUrlList) {
		    	 tsmpReportUrlMap.put(new TsmpReportUrlId(reportUrl.getReportId(), reportUrl.getTimeRange()), reportUrl);
		     }
		     
		     List<TsmpReportUrl> changeReportUrlList = new ArrayList<TsmpReportUrl>();
		     for (Map.Entry<TsmpReportUrlId, TsmpReportUrl> entry : fileDataList.entrySet()) {
		    	TsmpReportUrlId k = entry.getKey();
		    	TsmpReportUrl v = entry.getValue();
		    	
	            if(tsmpReportUrlMap.containsKey(k)) {
	            	TsmpReportUrl reportUrl =  tsmpReportUrlMap.get(k);
	            	reportUrl.setReportUrl(v.getReportUrl());
	            	changeReportUrlList.add(reportUrl);
	            }else {
	            	changeReportUrlList.add(v);
	            }
		     }
		     getTsmpReportUrlDao().saveAll(changeReportUrlList);
		}
	}
	
	protected void checkParam(String fileName) {
		
		int fileNameIndex = fileName.lastIndexOf(".");
		if(fileNameIndex == -1) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}
		
		String fileExtension = fileName.substring(fileNameIndex + 1);
		if(!"xlsx".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
	}
	
	private TsmpFunc parseRowToTsmpFunc(Row row) {
		TsmpFunc func = new TsmpFunc();
		DataFormatter formatter = new DataFormatter();
		func.setFuncCode(formatter.formatCellValue(row.getCell(0)));
		func.setFuncName(formatter.formatCellValue(row.getCell(1)));
		func.setFuncNameEn(formatter.formatCellValue(row.getCell(2)));
		func.setFuncDesc(formatter.formatCellValue(row.getCell(3)));
		func.setLocale(formatter.formatCellValue(row.getCell(4)));
		func.setFuncType(formatter.formatCellValue(row.getCell(5)));
		return func;
	}
	
	private TsmpReportUrl parseRowToTsmpReportUrl(Row row) {
		TsmpReportUrl reportUrl = new TsmpReportUrl();
		DataFormatter formatter = new DataFormatter();
		reportUrl.setReportId(formatter.formatCellValue(row.getCell(0)));
		reportUrl.setTimeRange(formatter.formatCellValue(row.getCell(1)));
		reportUrl.setReportUrl(formatter.formatCellValue(row.getCell(2)));
		return reportUrl;
	}
	
	/**
	 * 後台-tsmpaa( v3.8) API
	 * 
	 * 使用BcryptParam, 
	 * ITEM_NO='FUNC_TYPE' , DB儲存值對應代碼如下:
	 * DB值 (SUBITEM_NO) = 功能類型; 
	 * 0=原功能, 1=嵌入功能
	 * @param encodeFuncType
	 * @return
	 */
	public String getFuncTypeByBcryptParamHelper(String encodeFuncType, String locale) {
		String funcType = null;
		try {
			funcType = getBcryptParamHelper().decode(encodeFuncType, "FUNC_TYPE", BcryptFieldValueEnum.SUBITEM_NO, locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return funcType;
	}

	protected TsmpFuncDao getTsmpFuncDao() {
		return tsmpFuncDao;
	}
	
	protected TsmpReportUrlDao getTsmpReportUrlDao() {
		return tsmpReportUrlDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
}
