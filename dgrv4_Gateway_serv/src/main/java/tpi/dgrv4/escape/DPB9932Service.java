package tpi.dgrv4.escape;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9932Resp;
import tpi.dgrv4.entity.entity.DgrWebSocketMapping;
import tpi.dgrv4.entity.repository.DgrWebSocketMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.DigiRunnerGtwDeployProperties;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB9932Service {
	private TPILogger logger = TPILogger.tl;

	private DgrWebSocketMappingDao dgrWebSocketMappingDao;
	private DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties;
	
	@Autowired
	public DPB9932Service(DgrWebSocketMappingDao dgrWebSocketMappingDao,
			DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties) {
		super();
		this.dgrWebSocketMappingDao = dgrWebSocketMappingDao;
		this.digiRunnerGtwDeployProperties = digiRunnerGtwDeployProperties;
	}

	@Transactional
	public DPB9932Resp importWebsocketProxy(TsmpAuthorization tsmpAuthorization, MultipartFile mFile) {

		try {
			if(mFile == null || mFile.isEmpty() || mFile.getOriginalFilename() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{message.file}}");
			}
			
			checkParam(mFile.getOriginalFilename());

			List<DgrWebSocketMapping> importWebSocketList = importData(tsmpAuthorization, mFile.getInputStream());
			if(isMemory()) {
				deleteNotInImport(importWebSocketList);
			}
		    
     		return new DPB9932Resp();
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
	}
	
	public List<DgrWebSocketMapping> importData(TsmpAuthorization tsmpAuthorization, InputStream inputStream) throws Exception {
		List<DgrWebSocketMapping> excelDgrWebSocketMappingList = new ArrayList<DgrWebSocketMapping>();
		try (Workbook workbook = new XSSFWorkbook(inputStream);){
	
			 Sheet sheet = workbook.getSheetAt(0);
			 boolean isFirst = true;
			 DataFormatter formatter = new DataFormatter();   
		     Map<String, DgrWebSocketMapping> dgrWebSocketMappingMap = new HashMap<String, DgrWebSocketMapping>();
		   //checkmarx, Unchecked Input for Loop Condition
		     int lastRowNum = sheet.getLastRowNum();
		     final int maxRowsLimit = 1000000;
		     if (lastRowNum <= 0) {
	             throw TsmpDpAaRtnCode._1559.throwing("Invalid row number: " + lastRowNum);
	         }
	         
	         if (lastRowNum > maxRowsLimit) {
	             throw TsmpDpAaRtnCode._1559.throwing("Row count exceeds limit. Max allowed: " + maxRowsLimit + ", found: " + lastRowNum);
	         }
		     final int safeLastRowNum = Math.min(lastRowNum, maxRowsLimit);
		     for(int rowIndex = 0; rowIndex <= safeLastRowNum; rowIndex++) {
		    	 Row row = sheet.getRow(rowIndex);
		    	 if(row == null) {
		    		 continue;
		    	 }
		    	 if(isFirst) {
		    		 isFirst = false;
		    		 
		    		 if(!"SITE_NAME".equalsIgnoreCase(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"TARGET_WS".equalsIgnoreCase(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"MEMO".equalsIgnoreCase(formatter.formatCellValue(row.getCell(2)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    		 if(!"AUTH".equalsIgnoreCase(formatter.formatCellValue(row.getCell(3)))) {
		    			 throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		    		 }
		    	 }else {
		    		 //空白列不處理
		    		 if(!StringUtils.hasText(formatter.formatCellValue(row.getCell(0))) && 
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(1))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(2))) &&
		    			!StringUtils.hasText(formatter.formatCellValue(row.getCell(3)))) {
		    			 continue;
		    		 }
		    		 
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(0)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("SITE_NAME");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(1)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("TARGET_WS");
		    		 }
		    		 if(!StringUtils.hasLength(formatter.formatCellValue(row.getCell(3)))) {
		    			 throw TsmpDpAaRtnCode._1350.throwing("AUTH");
		    		 }
		    		 DgrWebSocketMapping vo = parseRowToEntity(row);
		    		 excelDgrWebSocketMappingList.add(vo);
		    	 }
		     }
		     
		     List<DgrWebSocketMapping> dgrWebSocketMappingList = getDgrWebSocketMappingDao().findAll();
		     for(DgrWebSocketMapping dgrWebSocketMapping : dgrWebSocketMappingList) {
		    	 dgrWebSocketMappingMap.put(dgrWebSocketMapping.getSiteName(), dgrWebSocketMapping);
		     }
		     
		     for (DgrWebSocketMapping importWebSocket : excelDgrWebSocketMappingList) {		    			    	
	            if(dgrWebSocketMappingMap.containsKey(importWebSocket.getSiteName())) {
	            	DgrWebSocketMapping dgrWebSocketMapping =  dgrWebSocketMappingMap.get(importWebSocket.getSiteName());
	            	dgrWebSocketMapping.setTargetWs(importWebSocket.getTargetWs());
	            	dgrWebSocketMapping.setMemo(importWebSocket.getMemo());
	            	dgrWebSocketMapping.setAuth(importWebSocket.getAuth());
	            	dgrWebSocketMapping.setUpdateDateTime(DateTimeUtil.now());
	            	dgrWebSocketMapping.setUpdateUser(tsmpAuthorization.getUserName());
	            	getDgrWebSocketMappingDao().save(dgrWebSocketMapping);
	            }else {
	            	importWebSocket.setCreateDateTime(DateTimeUtil.now());
	            	importWebSocket.setCreateUser(tsmpAuthorization.getUserName());
	            	getDgrWebSocketMappingDao().save(importWebSocket);
	            }
		     }
		     
		     return excelDgrWebSocketMappingList;
		}
	}
	
	public void checkParam(String fileName) {
		
		int fileNameIndex = fileName.lastIndexOf(".");
		if(fileNameIndex == -1) {
			throw TsmpDpAaRtnCode._1352.throwing("{{message.file}}");
		}
		
		String fileExtension = fileName.substring(fileNameIndex + 1);
		if(!"xlsx".equalsIgnoreCase(fileExtension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
	}
	
	private DgrWebSocketMapping parseRowToEntity(Row row) {
		DgrWebSocketMapping dgrWebSocketMapping = new DgrWebSocketMapping();
		DataFormatter formatter = new DataFormatter();
		dgrWebSocketMapping.setSiteName(formatter.formatCellValue(row.getCell(0)));
		dgrWebSocketMapping.setTargetWs(formatter.formatCellValue(row.getCell(1)));
		dgrWebSocketMapping.setMemo(formatter.formatCellValue(row.getCell(2)));
		dgrWebSocketMapping.setAuth(formatter.formatCellValue(row.getCell(3)));
		return dgrWebSocketMapping;
	}
	
	public boolean isMemory() {
		return getDigiRunnerGtwDeployProperties().isMemoryRole();
	}
	
	public void deleteNotInImport(List<DgrWebSocketMapping> importWebSocketList) {
		List<String> siteNameList = importWebSocketList.stream().map(DgrWebSocketMapping::getSiteName).collect(Collectors.toList());
		dgrWebSocketMappingDao.deleteBySiteNameNotIn(siteNameList);
	}

	protected DgrWebSocketMappingDao getDgrWebSocketMappingDao() {
		return dgrWebSocketMappingDao;
	}
	
	protected DigiRunnerGtwDeployProperties getDigiRunnerGtwDeployProperties() {
		return digiRunnerGtwDeployProperties;
	}
}
