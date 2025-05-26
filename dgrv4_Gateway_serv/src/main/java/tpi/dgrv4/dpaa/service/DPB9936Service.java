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

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9936Resp;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.escape.CheckmarxUtils;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9936Service {
	private TPILogger logger = TPILogger.tl;

	private TsmpRtnCodeDao tsmpRtnCodeDao;
	
	@Autowired
	public DPB9936Service(TsmpRtnCodeDao tsmpRtnCodeDao) {
		super();
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
	}

	@Transactional
	public DPB9936Resp importTsmpRtnCode(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

		try {
			if(mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}
			
			checkParam(mFile.getOriginalFilename());

     		importData(tsmpAuthorization, mFile.getInputStream());
		    
     		return new DPB9936Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
	}
	
	protected void importData(TsmpAuthorization tsmpAuthorization, InputStream inputStream) throws Exception {
		try (Workbook workbook = new XSSFWorkbook(inputStream);){
	
			 Sheet sheet = workbook.getSheetAt(0);
			 boolean isFirst = true;
			 DataFormatter formatter = new DataFormatter();
		     Iterator<Row> rows = sheet.iterator();
		     Map<TsmpRtnCodeId, TsmpRtnCode> fileDataList = new HashMap<TsmpRtnCodeId, TsmpRtnCode>();
		     Map<TsmpRtnCodeId, TsmpRtnCode> tsmpRtnCodeMap = new HashMap<TsmpRtnCodeId, TsmpRtnCode>();

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
		    		 if(!"TSMP_RTN_CODE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"LOCALE".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"TSMP_RTN_MSG".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"TSMP_RTN_DESC".equalsIgnoreCase(formatter.formatCellValue(row.getCell(3)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    	 }else {
		    		 Row row = rows.next();
		    		 //空白列不處理
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(0))) && 
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(1))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(2))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(3)))) {
		    			 continue;
		    		 }
		    		 
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("TSMP_RTN_CODE");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("LOCALE");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("TSMP_RTN_MSG");
		    		 }
		    		 TsmpRtnCode vo = parseRowToEntity(row);
		    		 fileDataList.put(new TsmpRtnCodeId(vo.getTsmpRtnCode(),vo.getLocale()), vo);
		    	 }
		     }
		     
		     List<TsmpRtnCode> rtnCodeList = getTsmpRtnCodeDao().findAll();
		     for(TsmpRtnCode rtnCode : rtnCodeList) {
		    	 tsmpRtnCodeMap.put(new TsmpRtnCodeId(rtnCode.getTsmpRtnCode(),rtnCode.getLocale()), rtnCode);
		     }
		     
		     List<TsmpRtnCode> changeRtnCodeList = new ArrayList<TsmpRtnCode>();
		     for (Map.Entry<TsmpRtnCodeId, TsmpRtnCode> entry : fileDataList.entrySet()) {
		    	TsmpRtnCodeId k = entry.getKey();
		    	TsmpRtnCode v = entry.getValue();
		    	
	            if(tsmpRtnCodeMap.containsKey(k)) {
	            	TsmpRtnCode rtnCode =  tsmpRtnCodeMap.get(k);
	            	rtnCode.setTsmpRtnMsg(v.getTsmpRtnMsg());
	            	rtnCode.setTsmpRtnDesc(v.getTsmpRtnDesc());
	            	changeRtnCodeList.add(rtnCode);
	            }else {
	            	changeRtnCodeList.add(v);
	            }
		     }
		     getTsmpRtnCodeDao().saveAll(changeRtnCodeList);
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
	
	private TsmpRtnCode parseRowToEntity(Row row) {
		TsmpRtnCode rtnCode = new TsmpRtnCode();
		DataFormatter formatter = new DataFormatter();
		rtnCode.setTsmpRtnCode(formatter.formatCellValue(row.getCell(0)));
		rtnCode.setLocale(formatter.formatCellValue(row.getCell(1)));
		rtnCode.setTsmpRtnMsg(formatter.formatCellValue(row.getCell(2)));
		rtnCode.setTsmpRtnDesc(formatter.formatCellValue(row.getCell(3)));
		return rtnCode;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return tsmpRtnCodeDao;
	}
}
